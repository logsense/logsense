/**
 * 
 */
package logSense;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import logSense.FileParams.LogType;
import logSense.ParserResult.NameValue;
import logSense.RecordSeparator.RecordSepInfo;
import logSense.SolrConstants.SolrField;
import logSense.db.DbManager;
import logSense.db.FileDao;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.common.SolrInputDocument;

/**
 * Indexes a log file.
 * 
 * @author am
 *
 */
public class LogIndexer 
{
	private Logger logger = Logger.getLogger(getClass().getName());
	public static LogIndexer o = new LogIndexer();
	private LogIndexer() {}

	// url will be of the form "http://localhost:8983/solr" with localhost replaced 
	// with your server name
	private String uri = "/solr";
	private SolrServer server;
	
	// private ParserManager parserManager;
	
	private long numRecordsIndexedSoFar;
	
	private int indexingBatchSize = SolrConstants.INDEXING_BATCH_SIZE;
	
	private Long testMaxRecords = null; // if this is non null, process this many records 
	
	  /*
	    HttpSolrServer is thread-safe and if you are using the following constructor,
	    you *MUST* re-use the same instance for all requests.  If instances are created on
	    the fly, it can cause a connection leak. The recommended practice is to keep a
	    static instance of HttpSolrServer per solr server url and share it for all requests.
	    See https://issues.apache.org/jira/browse/SOLR-861 for more details
	  */
	
	public void initServer()
	{
		// default server is localhost
		initServer("http://localhost:8983");
	}
	
	public void initServer(String httpServer)
	{
		StringBuilder buf = new StringBuilder();
		// if httpServer has a trailing / remove it
		if (CommonUtil.o.isEmpty(httpServer))
		{
			logger.fatal("please provide the address of the LogSense Server");
			throw new RuntimeException("empty httpServer");
		}
		String s = httpServer.trim();
		if (s.endsWith("/"))
			buf.append(s.substring(0, s.length() -1));
		else
			buf.append(s);
		
		buf.append(uri);
		String url = buf.toString();
		server = new HttpSolrServer( url );
		HttpSolrServer httpSolrServer = (HttpSolrServer) server;
		httpSolrServer.setMaxRetries(1); // defaults to 0.  > 1 not recommended.
		httpSolrServer.setConnectionTimeout(5000); 
		
		/*
		 * 5 seconds to establish TCP
		 * Setting the XML response parser is only required for cross
		 * version compatibility and only when one side is 1.4.1 or
		 * earlier and the other side is 3.1 or later.
		 * 
		 */
		httpSolrServer.setParser(new XMLResponseParser()); 
		
		/*
		 * binary parser is used by default
		 * 
		 * The following settings are provided here for completeness.
		 * They will not normally be required, and should only be used 
		 * after consulting javadocs to know whether they are truly required.
		 * 
		 */
		httpSolrServer.setSoTimeout(1000);  // socket read timeout
		httpSolrServer.setDefaultMaxConnectionsPerHost(100);
		httpSolrServer.setMaxTotalConnections(100);
		httpSolrServer.setFollowRedirects(false);  // defaults to false
		 
		/*
		 * allowCompression defaults to false.
		 * Server side must support gzip or deflate for this to have any effect.
		 * 
		 */
		 httpSolrServer.setAllowCompression(true);
		 
	}
	
	private void initParserMangager()
	{
		// init this only if not already initialized
		ParserManagerMap.o.initialize();
		
		/*
		if (parserManager == null)
		{
			parserManager = new ParserManager();
			parserManager.register((new CategoryParser()).init());
			parserManager.register((new StoredProcParser()).init());
			parserManager.register((new ClassMethodParser()).init());
			parserManager.register((new SessionIDParserWrapper()).init());
			parserManager.register((new RequestIDParser()).init());
			parserManager.register((new ImpParser()).init());
			parserManager.register((new MsecParser()).init());
			parserManager.register((new DalServerParser()).init());
			parserManager.register((new ExceptionParser()).init());
			parserManager.register((new DalPoolParser()).init());
			
			// parserManager.register((new UrlParser()).init());  // called directly if this is a TOMCAT BENCH
		
		}
		else
		{
			// since we have an LRUMap for holding DAL Session -> SessionID mappings, 
			// no need to clear it
			// parserManager.resetAllParsers();
		}
		*/
	}
	
	public void add(Collection<SolrInputDocument> docs) throws IOException, SolrServerException
	{
		if (docs == null || docs.size() <= 0)
			return;
		
		// the server may time out, so try several times
		for (int numTries = 0; numTries < 10; numTries++)
		{
			boolean status = tryAdding(docs);
			if (status)
				break;
			
			// else wait a little
			System.out.println(" retrying after some time");
			// wait for a min 5 secs before retrying. increase by 1 sec each time this fails
			CommonUtil.o.sleepNoException(5000L + 1000L * numTries);
			
		}
		
	}
	
	private boolean tryAdding(Collection<SolrInputDocument> docs) throws IOException 
	{
		boolean status = false;
		try
		{
			server.add(docs);
			
			if (SolrConstants.HARD_COMMIT)
				server.commit();
			
			status = true;
		
			// progress info for debugging purposes
			numRecordsIndexedSoFar += docs.size();
		
			if (numRecordsIndexedSoFar % SolrConstants.COMMIT_SIZE == 0) {
				printDiagnostics();
				status = softCommit();
			}
			
			
		} catch (SolrServerException e) {
			logger.info(e.getMessage(), e);
			status = false;
		}
		
		return status;
		
	}
	
	private boolean softCommit()
	{
		boolean status = true;
		try
		{
			if (!SolrConstants.HARD_COMMIT)
			{
				// server.commit(waitFlush, waitSearcher, softCommit)
				server.commit(false, false, true);
			}
		} catch (IOException e) {
			// logger.error(e.getMessage(), e);
			status = false;
		} catch (SolrServerException e) {
			// logger.error(e.getMessage(), e);
			status = false;
		}
		
		return status;
	}
	
	private void printDiagnostics ()
	{
		System.out.print("... " + numRecordsIndexedSoFar);
		if (numRecordsIndexedSoFar %1000 == 0)
			System.out.println();
		
	}
	
	static final String SEP = " ";
	
	/*
	 * parse a log file and add its records to Solr
	 * this has gotten a big ugly because we are trying both directories and indiv files.
	 * 
	 * for directories, we use the File object so we have the full path to the file. 
	 * TODO clean this up.
	 */
	public void index(File file
					, String logFile2Use
					, String dataCenter
					, String website
					, Boolean ssl
					, Integer machineNum
					, String env
					, LogType logType
					, String dalOrL2Server) 
					throws FileNotFoundException, IOException, SolrServerException
	{
		BufferedReader rd = null;

		String fullyQualifiedName = file != null? file.getAbsolutePath() : logFile2Use;
		
		if (logFile2Use.toLowerCase().endsWith(".gz"))
		{
			rd = CommonUtil.o.createReaderForGz(fullyQualifiedName);
			
		}
		else {
			rd = new BufferedReader(new FileReader(fullyQualifiedName));
		}
		
		index(rd, logFile2Use, dataCenter, website,ssl, machineNum, env, logType, dalOrL2Server)	;
	}
	
	public static class DuplicatePattern
	{
		/*
		 * for an exception, we attempt to generate a duplicate detection pattern.
		 * if there is a stack trace, this is the first line of the stack trace (has line number)
		 * if there is no stack trace, take fully qualified class + method (no line number)
		 */
		
		String stackTraceTopLine;
		String className;
		String method;
		
		/*
		 * for some exceptions, e.g., for Oracle PL/SQL errors, 
		 * there may be a string pattern that identifies the error uniquely
		 */
		String otherSignature;
		
		boolean hasException;
		
		String cachedPattern;
		
		public String getDuplicatePattern()
		{
			if (cachedPattern != null)
				return cachedPattern;
			
			if (stackTraceTopLine != null) {
				cachedPattern = stackTraceTopLine;
				return cachedPattern;
			}
			
			if (hasException)
			{
				if (!CommonUtil.o.isEmpty(otherSignature))
				{
					cachedPattern = otherSignature;
				}
				
				else if (className != null)
				{
					if (method != null)
						cachedPattern = className + "/" + method;
					else
						cachedPattern = className;
				}
			
			}
			else
				cachedPattern = null;
			
			return cachedPattern;
		}
		
		public boolean hasDuplicatePattern()
		{
			getDuplicatePattern();
			
			return cachedPattern != null;
		}
		
		
	}
	
	public void index(BufferedReader rd
					, String logFile2Use
					, String dataCenter
					, String website
					, Boolean ssl
					, Integer machineNum
					, String env
					, LogType logType
					, String dalOrL2Server) 
					throws FileNotFoundException, IOException, SolrServerException
	{
		
		long startTime=System.currentTimeMillis();
		numRecordsIndexedSoFar = 0L; // for statistics purposes. 
		
		/* let's keep the file name as ending with .gz 
		 * earlier, we were removing the .gz -- however, we need the same file name in Solr
		 * as the real file so we can go back to the file if user desires.	
		 *
		 */
		String logFileName = logFile2Use;
		
		if (o.processedFilesWriter != null)
		{
			o.processedFilesWriter.println(logFileName);
			o.processedFilesWriter.flush();
		}
		
		boolean isSsl = ssl != null && ssl.booleanValue()? true : false;
		
		long lineNum = 0L;
		long recordLineNum = 0L; // line number of the first line of the record
		long recordNum = 0L; // current record number. the variable numRecordsIndexedSoFar lags this
		String line;
		
		Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
		
		final String machine;
		
		if (!CommonUtil.o.isEmpty(dalOrL2Server))
		{
			machine = dalOrL2Server;
		}
		else
		{
			StringBuilder machineBuf = new StringBuilder();
			
			// ndc wwwssl 121
			boolean first = true;
			if (dataCenter != null) {
				machineBuf.append(dataCenter);
				first = false;
			}
			if (env != null)
			{
				if (!first)
					machineBuf.append(SEP);
				first = false;
				machineBuf.append(env);
				
			}
			if (website != null)
			{
				if (!first)
					machineBuf.append(SEP);
				first = false;
				machineBuf.append(website);
				
			}
				
			
			/* ssl is now part of website
			if (isSsl)
				machineBuf.append("ssl");
				
			*/
			
			
			if (machineNum != null)
			{
				if (!first)
					machineBuf.append(SEP);
				first = false;
				machineBuf.append(machineNum);
			}
			
			machine =  machineBuf.toString() ;
		
		}
		
		List<NameValue> commonFields = new ArrayList<NameValue>();
		if (dataCenter != null)
			commonFields.add(new NameValue(SolrField.DATA_CENTER, dataCenter));
		
		if (machineNum != null)
			commonFields.add(new NameValue(SolrField.MACHINE_NUM, String.valueOf(machineNum)));
		
		commonFields.add(new NameValue(SolrField.SSL, isSsl? "1" : "0"));
		commonFields.add(new NameValue(SolrField.APP, website));
		commonFields.add(new NameValue(SolrField.MACHINE, machine));
		commonFields.add(new NameValue(SolrField.FILE_NAME, logFileName));
		commonFields.add(new NameValue(SolrField.LOG_TYPE, logType.name()));
		
		if (env != null)
			commonFields.add(new NameValue(SolrField.ENV, env));
		
		initParserMangager();
		
		
		// TODO cache the list of field/value pairs that are common to the log file
		
		
		// prevFirstLine holds a first line of a record until we know for sure the record has ended
		String prevFirstLine = null;
		NameValue prevLineDate = null; 
		StringBuilder buf = null;
		// ParserResult parsedDate = null;
		BaseFieldParserImpl stackTraceParser = (new StackTraceParser()).init();
		
		boolean hasStackTrace = false;
		
		DuplicatePattern dupPattern = new DuplicatePattern();
		
		/*
		 * for an exception, we attempt to generate a duplicate detection pattern.
		 * if there is a stack trace, this is the first line of the stack trace.
		 * if there is no stack trace, take fully qualified method/class
		 */
		while ( (line = rd.readLine() ) != null)
		{
			lineNum++;
			
			// if line is empty or starts with SESSION BENCH, skip
			// the reason we skip for SESSION BENCH is that it has no date, and is definitely not a continuation line
			if (CommonUtil.o.isEmpty(line) || skipLine(line) ||  line.startsWith("SESSION BENCH:"))
			{
				continue;
			}
			
			// parsedDate = hasDate(line, logType);
			RecordSepInfo recordSep = RecordSeparator.o.getRecordSepInfo(line, logType);
			
			// if this is a continuation line, there is no date in the String
			if (! recordSep.isSeparator())
			{
				// if there is no prevFirstLine, a record has not been started. so skip this line
				if (prevFirstLine == null)
					continue;
				
				else 
				{
					/*
					 * there is a prevLine. however, buf will only be started on demand
					 * i.e., once we see a continuation line
					 */
					if (buf == null) 
					{
						buf = new StringBuilder();
						buf.append(prevFirstLine);
					}
					
					// add this continuation line to buf
					buf.append("\n").append(line);
					
					// check if this is a stack trace
					if (!hasStackTrace)
					{
						ParserResult stackTraceResult = stackTraceParser.match(line, logType);
						if (stackTraceResult != null && stackTraceResult.hasMatch())
						{
							hasStackTrace = true;
							if (dupPattern == null)
								dupPattern = new DuplicatePattern();
							dupPattern.stackTraceTopLine = stackTraceResult.nvPairs.get(0).value;
						}
					}
					/*
					 * was:
					 * if (!hasStackTrace && stackTraceParser.hasMatch(line, logType))
					 * 		hasStackTrace = true;
					 */
						
					
				}
			} // ! recordSep.isSeparator()
			else
			{
				/*
				 * line matches a date. so start of a new record.
				 * 
				 * save prev record
				 * 
				 * for next record,
				 * set recordLineNum to lineNum, prevFirstLine to line, and buf to null
				 */
				
				if (prevFirstLine != null || (buf != null && buf.length() > 0))
				{
					recordNum++;
					indexRecord(prevFirstLine
								, buf
								, recordLineNum
								, recordNum
								, commonFields
								, dupPattern
								, prevLineDate
								, hasStackTrace
								, logFileName
								, logType
								, docs);
				}

				prevFirstLine = line; // parsedDate.remainder;
				
				/*
				 * for date for the record, we only pick it from the first line of the record
				 * 
				 * otherwise, let the date be picked up from the previous record.
				 * sometimes, a record has no date. if we leave it as that, then
				 * we cannot clean up with a delete query based on date
				 */
				if (recordSep != null && recordSep.parsedDate != null && recordSep.parsedDate.hasMatch())
					prevLineDate = recordSep.parsedDate.nvPairs.get(0);
				//was: else
					// prevLineDate = null;
				recordLineNum = lineNum;
				buf = null;
				hasStackTrace = false;
				dupPattern = new DuplicatePattern();
				
			} // else -- hasDate() == true
			
			// if max number of records is reached (testing mode)
			if (testMaxRecords != null && numRecordsIndexedSoFar > testMaxRecords.longValue())
				break;
			
		} // while -- file read, line at a time
		
		// index the last remaining record, if prevFirstLine is not null.
		recordNum++;
		indexRecord(prevFirstLine
					, buf
					, recordLineNum
					,  recordNum
					, commonFields
					, dupPattern
					, prevLineDate
					, hasStackTrace
					, logFileName
					, logType
					, docs);
		
		// index any remaining docs
		if (!docs.isEmpty())
		{
			add(docs);
			
			
			/*
			 * print diagnostics and issue a softCommit if necessary.
			 */
			if ( docs.size() > 0 && (numRecordsIndexedSoFar % SolrConstants.COMMIT_SIZE != 0) )
			{
				printDiagnostics();
				System.out.println();
				// softCommit();
				server.commit();
			}
			
			docs.clear();
			
		}
		
		if (rd != null)
			rd.close();
		
		long endTime = System.currentTimeMillis();
		
		double timeTaken = endTime - startTime;
		if (numRecordsIndexedSoFar > 0)
			System.out.println(" time to process file: " + timeTaken/numRecordsIndexedSoFar + " msec/record, numRecords=" + numRecordsIndexedSoFar + ", totalTime=" + timeTaken/60000 + " minutes");
		
	}
	
	static final String IGNORE_STR = "(Killed by signal|Could not chdir to home directory)";
	static final Pattern IGNORE_PATTERN= Pattern.compile(IGNORE_STR);
	private boolean skipLine(String line)
	{
		if (line == null)
			return true;
		
		line = line.trim();
		Matcher matcher = IGNORE_PATTERN.matcher(line);
		if (matcher.matches())
			return true;
		
		return false;
	}
	
	long lowSkipTime;
	long highSkipTime;
	private void initSkipTime()
	{
		Date d = new Date();
		d.setYear(2013);
		d.setMonth(11);
		d.setDate(13);
		d.setHours(0);
		d.setMinutes(25);
		d.setSeconds(0);
		
		lowSkipTime = d.getTime();
		
		d = new Date();
		d.setYear(2013);
		d.setMonth(11);
		d.setDate(13);
		d.setHours(1);
		d.setMinutes(0);
		d.setSeconds(0);
		
		highSkipTime = d.getTime();
	}
	private boolean skipDateRange (String tzDate)
	{
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			
			Date d = sdf.parse(tzDate);
			// System.out.print(d);
			long time = d.getTime();
			if (time >= lowSkipTime && time <= highSkipTime)
				return true;
			
		} catch (java.text.ParseException e ){
			logger.info(e.getMessage());
		}
		
		return false;
	}
	
	private void indexRecord(String prevFirstLine
									, StringBuilder buf
									, long recordLineNum // current line number in file for this record
									, long recordNum // current record number in this file
									, List<NameValue> commonFields
									, DuplicatePattern dupPattern // for identifying identical exceptions
									, NameValue prevLineDate
									, boolean hasStackTrace
									, String logFile
									, LogType logType
									, Collection<SolrInputDocument> docs)
			throws SolrServerException, IOException
	{
		// skip record if there is no date. else we cannot delete old records based on date
		// for logmon though, date will be parsed by the parser that we invoke below
		if (prevLineDate == null && logType != LogType.logmon_www)
			return;
		
		if (Switch.o.SKIP_DATE && prevLineDate != null && skipDateRange(prevLineDate.value))
		{
			// skip this line
			return;
		}
		SolrInputDocument doc = parseRecord(prevFirstLine
											, buf
											, recordLineNum
											, recordNum
											, commonFields
											, dupPattern
											, prevLineDate
											, hasStackTrace
											, logFile
											, logType);
		docs.add(doc);
		
		// if docs exceeds a threshold, write it out and clear docs
		
		if (docs.size() >= indexingBatchSize) // SolrConstants.INDEXING_BATCH_SIZE 
		{
			add(docs);
			docs.clear();
		}
		
	}
	
	/*
	 * save prev record
	 * if buf has data, then use buf. else use prevFirstLine
	 */
	private SolrInputDocument parseRecord(String prevFirstLine
											, StringBuilder buf
											, long recordLineNum
											, long recordNum
											, List<NameValue> commonFields
											, DuplicatePattern dupPattern
											, NameValue prevLineDate
											, boolean hasStackTrace
											, String logFile
											, LogType logType)
											throws SolrServerException, IOException
	{
		final SolrInputDocument doc;
		if (buf != null && buf.length() > 0)
			doc = parseRecord(buf.toString()
								, recordLineNum
								, recordNum
								, commonFields
								, dupPattern
								, prevLineDate
								, hasStackTrace
								, logFile
								, logType);
			
		else if (prevFirstLine != null)
			doc = parseRecord(prevFirstLine
								, recordLineNum
								, recordNum
								, commonFields
								, dupPattern
								, prevLineDate
								, hasStackTrace
								, logFile
								, logType);
		else
			doc = null;
		return doc;

		
	}
	
	private SolrInputDocument parseRecord(String record
										, long recordLineNum
										, long recordNum
										, List<NameValue> commonFields
										, DuplicatePattern dupPattern
										, NameValue prevLineDate
										, boolean hasStackTrace
										, String fileName
										, LogType logType)
	{
		SolrInputDocument result = new SolrInputDocument();
		
		
		
		result.addField(SolrField.LINE_NUM, new Long(recordLineNum));
		
		result.addField(SolrField.RECORD_NUM, new Long(recordNum));
		
		
		
		// add date. date is in TZ format
		if (prevLineDate != null)
		{
			result.addField(prevLineDate.name, prevLineDate.value);
		}
		
		if (hasStackTrace)
		{
			result.addField(SolrField.STACK_TRACE, "1");
		}
		
		// add machine info -- common across all records in a log file
		if (!CommonUtil.o.isEmpty(commonFields))
		{
			for (NameValue nv : commonFields)
			{
				result.addField(nv.name, nv.value);
			}
		}
		
		String record2Use = record;
		
		
		// fileName + "-" + recordLineNum;
		
		StringBuilder idBuf = new StringBuilder();
		idBuf.append(fileName).append("-").append(recordLineNum);
		
		// we will prepend requestId! to idBuf if we find a requestId
		// this will ensure all requestIds are routed to the same shard
		
		ParserManager parserManager = (ParserManager) ParserManagerMap.o.get(logType);
		ParserResult parsedFields = parserManager.parse(record, logType);
		if (parsedFields != null && parsedFields.hasMatch())
		{
			for (NameValue nv : parsedFields.nvPairs)
			{
				result.addField(nv.name, nv.value);
				
				// if nv.name == class or method, update dupPattern
				if (nv.name.equalsIgnoreCase(SolrField.CLASS))
					dupPattern.className = nv.value;
				else if (nv.name.equalsIgnoreCase(SolrField.METHOD))
					dupPattern.method = nv.value;
				else if (nv.name.equalsIgnoreCase(SolrField.EXCEPTION))
					dupPattern.hasException = true;
			}
			
			/*
			 * if there is an exception, and no stacktrace was found, see if there
			 * is an error signature -- for jagLogs
			 */
			
			if (!CommonUtil.o.isEmpty(parsedFields.discovered))
			{
				StringBuilder buf = new StringBuilder();
				buf.append(record2Use);
				
				for (String s : parsedFields.discovered)
				{
					buf.append(" ").append(s);
				}
				
				record2Use = buf.toString();
				
			}
			
			if (!CommonUtil.o.isEmpty(parsedFields.shardRouteKey))
			{
				// prepend parsedFields.shardRouteKey! to idBuf 
				idBuf.insert(0, "!");
				idBuf.insert(0, parsedFields.shardRouteKey.hashCode());
			}
		}
		
		// add record2Use as a field
		result.addField(SolrField.RECORD, record2Use.replaceAll("[<|>]", " "));
		
		// check if there is a dupPattern. if so add
		String duplicatePatternStr = dupPattern.getDuplicatePattern();
		if (!CommonUtil.o.isEmpty(duplicatePatternStr))
			result.addField(SolrField.EXCEPTION_CLASS, duplicatePatternStr);
		
		/*
		 * the id (unique field) is fileName + recordLineNum. this will let us find
		 * the right place in the file with before and after lines for displaying to user
		 * 
		 * Solr join does not cross shards, so if we want to join, we want to route all related fields to the same shard.
		 * In our case, we may want to join by requestId, that can be used for routing.
		 * 
		 * The format is:
		 	requestId!id
		 	
		 * 
		 */
		 
		// result.addField(SolrField.ID, fileName + "-" + recordLineNum);
		result.addField(SolrField.ID, idBuf.toString());

		return result;
	}
	
	/*
	 * ignore the following. 
	 	#
		#  Say States S0, S1. 
		#  D = date, ND = no-date
		#  M = matching word. NM = !M
		#
		#  initially S0
		#
		#  when in S0:
		#		ND -> S0
		#       D and NM -> S0
		#       D and M -> S1
		#
		#  when in S1:
		#       ND -> S1
		#       D and M -> S1
		#       D and NM -> S0
		#
		#
	 */

	
	
	private Set<String> ignoreLogs = new HashSet<String>();
	
	// append processed log file names here
	PrintWriter processedFilesWriter = null;
	
	private void initIgnoreLogs (String ignoreLogsInThisFile) throws Exception
	{
		BufferedReader rd = null;
		try
		{
			rd = new BufferedReader(new FileReader(ignoreLogsInThisFile));
		} catch (Exception e) {
			logger.warn(e.getMessage() + " skipping...");
			// logger.error(e.getMessage(), e);
			return;
		}
		
		String line;
		while ( (line = rd.readLine()) != null)
		{
			
			// comments
			int index = line.indexOf("#");
			if (index >= 0)
				line = line.substring(0, index);
			
			line = line.trim();
			if (line.length() > 0)
				ignoreLogs.add(line);
		}
		
		rd.close();
	}
	
	public static void main(String[] args)
	{
		try
		{
			Arguments arguments = Arguments.newInstance(args);
			if (arguments == null  || arguments.isError())
				return;
			
			// skip from 00:25hrs to 01:00hrs
			// 
			if (Switch.o.SKIP_DATE)
				o.initSkipTime();
			
			// ignore these log files if present
			if (!CommonUtil.o.isEmpty(arguments.ignoreLogs))
			{
				o.initIgnoreLogs(arguments.ignoreLogs);
			}
			
			// open processLogs file if present. append names of log files processed this round.
			
			
			if (!CommonUtil.o.isEmpty(arguments.processedLogs))
			{
				o.processedFilesWriter = new PrintWriter(new FileWriter(arguments.processedLogs, true)); // true == append
			}
			
			if (arguments.indexingBatchSize != null && arguments.indexingBatchSize.intValue() > 0)
				o.indexingBatchSize = arguments.indexingBatchSize.intValue();
			
			if (arguments.testMaxRecords != null && arguments.testMaxRecords.longValue() > 0)
				o.testMaxRecords = arguments.testMaxRecords.longValue();
			
			// init mysql if mysqlHostname is supplied
			if (!CommonUtil.o.isEmpty(arguments.mysqlHostName))
			{
				o.initDb(arguments.mysqlHostName, arguments.mysqlUserName, arguments.mysqlPassword);
			}
			
			if (arguments.stdin) // read from stdin
			{
				// in debug mode, we will read from this file
				if (!CommonUtil.o.isEmpty(arguments.simulatedFileName))
				{
					System.setIn(new FileInputStream(arguments.simulatedFileName));
				}
				o.indexFile(arguments);
			}
			// if this is a directory
			else if (! CommonUtil.o.isEmpty(arguments.directory))
			{
				o.index(arguments.directory
						, arguments.solrServerUrl
						, arguments.env
						, arguments.logType
						, ! CommonUtil.o.isEmpty(arguments.mysqlHostName) // if this evaluates to true, update to mysql at end
						);
			}
			else // this is a file
			{
				// this is a single file
				o.indexFile(arguments);
			}
			
			/*
			if (o.processedFilesWriter != null)
			{
				o.processedFilesWriter.flush();
				o.processedFilesWriter.close();
			}
			*/
			
			/* was
			Boolean ssl = arguments.getSsl();
			String website = ssl != null && ssl.booleanValue() ? "wwwssl" : "www";
			o.index( arguments.getFileName()
					, arguments.getSolrServerUrl()
					, arguments.getDataCenter()
					, website
					, ssl
					, arguments.getMachineNum());
					
			*/
		} catch (Exception e) {
			o.logger.error(e.getMessage(), e);
		} finally {
			o.closeFiles();
		}
	}
	
	private void initDb(String mysqlHostname, String mysqlUsername, String mysqlPassword)
	{
		if (!CommonUtil.o.isEmpty(mysqlHostname))
		{
			// format: "jdbc:mysql://logsense.yourcompany.com:3306/sessionCrossRef"
			StringBuilder buf = new StringBuilder();
			buf.append("jdbc:mysql://").append(mysqlHostname.trim()).append("/sessionCrossRef");
			String mysqlUrl = buf.toString();
			DbManager.o.initialize(mysqlUrl, mysqlUsername, mysqlPassword);
		}
	}
	
	private void closeFiles()
	{
		try
		{
			if (o.processedFilesWriter != null)
			{
				o.processedFilesWriter.flush();
				o.processedFilesWriter.close();
			}
			
			o.processedFilesWriter = null;
		} catch (Exception e) {}
	}
	/*
	 * initial testing
	public static void ignoreMe(String[] args)
	{
		try
		{
			String file = "jetty-0.log.ohio-www100.yourcompany.com.1234"; 
			// or file = "jetty-0.log.ohio-www100.yourcompany.com.1234.gz"; 
			int server = 100; 
			if (args.length > 0)
				file = args[1];
			o.test( file, "ohio", "www", false, server);
		} catch (Exception e) {
			o.logger.error(e.getMessage(), e);
		}
	}
	
	
	private void index(String fileName, String solrServerUrl, String dataCenter, String website, Boolean ssl, Integer machineNum) throws Exception
	{
		if (!CommonUtil.o.isEmpty(solrServerUrl))
			initServer(solrServerUrl);
		else
			initServer(); // use default Solr server if none specified
		
		index(fileName, dataCenter, website, ssl, machineNum);
	}
	
	*/
	
	// index a directory
	private void index(String dir
						, String solrServerUrl
						, String env
						, LogType logType
						, boolean updateMysqlAtEnd // update file index status 
						) throws Exception
	{
		if (!CommonUtil.o.isEmpty(solrServerUrl))
			initServer(solrServerUrl);
		else
			initServer(); // use default Solr server if none specified
		
		List<File> files = CommonUtil.o.getFiles(dir);
		if (CommonUtil.o.isEmpty(files))
		{
			logger.fatal("no files found in " + dir);
			return;
		}
		for (File file : files)
		{
			String fName = file.getName();
			if (ignoreLogs.contains(fName))
			{
				System.out.println("skipping " + fName );
				continue;
			}
			
			FileParams fParams = getFileParamsParser(file, true).getFileParams(null /* arguments */ , fName);
			
			// currently, only processing www and wwwssl files
			if (!fParams.isValid())
				continue;
			
			
			fParams.file = file; // need full path to open the file
			
			if (env != null)
				fParams.env = env;
			
			if (logType != null)
				fParams.logType = logType;
			
			System.out.print("indexing " + fParams.fileName + " ");
			
			
			index(fParams.file, fParams.fileName, fParams.dataCenter, fParams.website, fParams.ssl, fParams.machineNum, fParams.env, fParams.logType, fParams.backendServer);
			
			if (updateMysqlAtEnd)
			{
				FileDao.o.updateEndIndexTime(fParams.fileName);
			}
			System.out.println("done");
		}
	}
	
	private void indexFile(Arguments arguments) throws Exception
	{
		String solrServerUrl = arguments.getSolrServerUrl();
		if (!CommonUtil.o.isEmpty(solrServerUrl))
			initServer(solrServerUrl);
		else
			initServer(); // use default Solr server if none specified
		
		// if stdin
		if (arguments.stdin)
		{
			// getFileParamsParser should not check for file's existence for stdin
			// we are using it to get machine and env info
			FileParams fParams = getFileParamsParser(arguments.fileName, false).getFileParams(arguments , null );
		
		
			BufferedReader rd = new BufferedReader(new InputStreamReader(System.in));
			String fileName2Use = fParams.fileName.trim(); // file name must be supplied. cannot be empty
			fileName2Use = fileName2Use.toLowerCase().endsWith("stream")? fileName2Use : fileName2Use + "-stream";
			index(rd
					, fileName2Use
					, fParams.dataCenter
					, fParams.website
					, fParams.ssl
					, fParams.machineNum
					, fParams.env
					, fParams.logType
					, fParams.backendServer);
		}
		else
		{
			FileParams fParams = getFileParamsParser(arguments.fileName, true).getFileParams(arguments , null );
			
			index(fParams.file
				, fParams.fileName
				, fParams.dataCenter
				, fParams.website
				, fParams.ssl
				, fParams.machineNum
				, fParams.env
				, fParams.logType
				, fParams.backendServer);
			
			if (!CommonUtil.o.isEmpty(arguments.mysqlHostName))
			{
				initDb(arguments.mysqlHostName, arguments.mysqlUserName, arguments.mysqlPassword);
				FileDao.o.updateEndIndexTime(fParams.fileName);
			}
			
		}
	}
	
	
	private BaseFileParamsParser getFileParamsParser(String fileName, boolean checkIfFileExists)
	{
		
		File file = new File(fileName.trim());
		
		if (checkIfFileExists)
			return getFileParamsParser(file, checkIfFileExists);
		else
			return getFileParamsParser2(fileName);
	}
	
	private BaseFileParamsParser getFileParamsParser(File file, boolean checkIfFileExists)
	{
		String fileName = null;
		
		if (file.exists() && file.isFile())
		{
			fileName= file.getName().toLowerCase();
		}
		else
		{
			logger.fatal("log file does not exist ");
			return null;
		}
		
		return getFileParamsParser2(fileName);
	}
	
	private BaseFileParamsParser getFileParamsParser2(String fileName)
	{
		if (fileName.startsWith("tomcat") || fileName.startsWith("jetty") )
			return FileParamsParser.o;
		else if (fileName.startsWith("dal"))
			return DalFileParamsParser.o;
		
		// add other file types here
		
		else 
		{
			logger.fatal("log file name must start with tomcat, jetty or dal. if you have added a new file type,  please fix me and restart");
			return null;
		}
	}
}
