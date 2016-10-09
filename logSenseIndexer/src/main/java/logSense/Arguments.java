
package logSense;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import logSense.FileParams.LogType;


/**
 * parses command line arguments.
 * 
 * @author am
 *
 */
public class Arguments
{
	String solrServerUrl = null; // if none specified, default value used is http://localhost:8983
	
	// if stdin is true, we will take input from stdin
	// in such a case, -d (directory) and -f file are ignored
	boolean stdin = false;
	
	
	
	// directory. if dir is provided, all files under it, recursively, will be processed. other fields below will be ignored
	String directory = null;
	
	// log file
	String fileName = null;
	Boolean ssl = null;
	Integer machineNum = null;
	String dataCenter = null; // edc/ndc
	String env = null; // e4 etc in QA, or testing4086 for atg
	String ignoreLogs = null;
	String processedLogs = null; // this file holds the names of files we have indexed
	
	private boolean error = false;
	
	Integer indexingBatchSize = null;
	
	// for testing purposes, stop after this many records
	Long testMaxRecords = null;
	
	String simulatedFileName; // for testing purposes, when stdin is true, we can read from this file
	
	LogType logType;
	
	// mysql credentials
	String mysqlUserName = null;
	String mysqlPassword = null;
		
		
	String mysqlHostName = null;
	
	public Arguments() {}

	public static Arguments newInstance(String[] args) 
    {
		Arguments result = new Arguments();
		
		if (args == null || args.length == 0) {
			result.showHelp();
			System.exit(0);
		}
    	
    	
    	int index = 0;
        while (index < args.length) 
        {
        	boolean match = false;
        	String nextArg = args[index].trim().toLowerCase();
        	
        	if (nextArg.equalsIgnoreCase("--mysql-user")) {
        		match = true;
        		result.mysqlUserName = args[index+1];
        		index+=2;
        		continue; 
        	}

        	if (nextArg.equalsIgnoreCase("--mysql-password")) {
        		match = true;
        		result.mysqlPassword = args[index+1];
        		index+=2;
        		continue;
        	}
        	
        	if (nextArg.equalsIgnoreCase("--mysql-hostname")) {
        		match = true;
        		result.mysqlHostName = args[index+1];
        		index+=2;
        		continue;
        	}
        	
        	// e.g.,  for live data where we read from stdin
        	if (nextArg.startsWith("-stdin")) {
        		match = true;
        		result.stdin = true;
        		index+=1;
        		continue; 
        	}
        	
        	// simulated stdin during testing
        	/*
        	 * System.setIn(new FileInputStream(filename));
        	 */
        	
        	if (nextArg.startsWith("-simulate")) {
        		match = true;
        		result.simulatedFileName = args[index+1];
        		result.fileName = result.simulatedFileName;
        		index+=2;
        		continue; 
        	}
        	
        	if (nextArg.startsWith("-d")) {
        		match = true;
        		result.directory = args[index+1];
        		index+=2;
        		continue; // still need to check for solr server
        	}

        	if (nextArg.startsWith("-f")) {
        		match = true;
        		result.fileName = args[index+1];
        		index+=2;
        		continue;
        	}
        	
        	if (nextArg.startsWith("-u")) {
        		match = true;
        		result.solrServerUrl = args[index+1];
        		index+=2;
        		continue;
        	}
        	
        	if (nextArg.startsWith("-s")) {
        		match = true;
        		result.ssl = CommonUtil.o.parseBoolean(args[index+1]);
        		index+=2;
        		continue;
        	}
        	
        	if (nextArg.startsWith("-c")) {
        		match = true;
        		result.dataCenter = args[index+1];
        		index+=2;
        		continue;
        	}
        	
        	if (nextArg.startsWith("-n")) {
        		match = true;
        		result.machineNum = CommonUtil.o.parseInt(args[index+1]);
        		index+=2;
        		continue;
        	}
        	
        	// ignore these log files
        	if (nextArg.startsWith("-i")) {
        		match = true;
        		result.ignoreLogs = args[index+1];
        		index+=2;
        		continue;
        	}
        	
        	// append processed logs here
        	if (nextArg.startsWith("-p")) {
        		match = true;
        		result.processedLogs = args[index+1];
        		index+=2;
        		continue;
        	}
        	
        	if (nextArg.startsWith("-b")) {
        		match = true;
        		result.indexingBatchSize = new Integer(CommonUtil.o.parseInt(args[index+1]));
        		index+=2;
        		continue;
        	}
        	
        	if (nextArg.startsWith("-t")) {
        		match = true;
        		result.testMaxRecords = new Long(CommonUtil.o.parseInt(args[index+1]));
        		index+=2;
        		continue;
        	}
        	
        	if (nextArg.startsWith("-h")) {
        		match = true;
        		index++;
        		result.showHelp(); // still need to check for solr server
        		System.exit(0);
        	}
        	
        	// env
        	if (nextArg.startsWith("-e")) {
        		match = true;
        		result.env = args[index+1];
        		index+=2;
        		continue;
        	}
        	
        	if (nextArg.startsWith("-l")) {
        		match = true;
        		String logTypeStr = args[index+1];
        		result.logType = getLogType(logTypeStr);
        		if (result.logType == null)
        		{
        			System.err.println("unknown logType " + logTypeStr);
        			System.exit(0);
        		}
        		index+=2;
        		continue;
        	}

        	if (!match) {
        		result.showHelp();
        		System.exit(0);
        		// System.err.println("Invalid argument: " + args[index]);
        		// result.usage();
        	}
        	
        } /* while */
        
        if (CommonUtil.o.isEmpty (result.fileName) && CommonUtil.o.isEmpty (result.directory)) {
        	result.usage("need either directory or fileName");
        	result.usage();
        }
       
        
        return result;	
    }
    
    private void usage() 
    {
    	error = true;
    	System.out.println("Usage: java " + LogIndexer.class.getName() + " [-u solrServerUrl] [-d directory] [-f fileName] [-c dataCenter] [-n machineNum ] [-s ssl] ");
    }
    
    private void showHelp()
    {
    	try
    	{
	    	InputStream in = this.getClass().getClassLoader().getResourceAsStream("help.txt");
	    	BufferedReader rd = new BufferedReader(new InputStreamReader(in));
	    	String line = null;
	    	while(rd != null && ((line = rd.readLine()) != null) )
	    	{
	    		System.out.println(line);
	    	}
	    	
    	} catch (Exception e) {
    		error = true;
    		usage();
    	}
    }
    
    private void usage(String s) {
    	error = true;
    	System.out.println(s);
    }
    
    // test pad
    public static void main(String[] args)
    {
    	Arguments a = Arguments.newInstance(args);
    	if (a.directory != null)
    		System.out.println("directory=" + a.directory);
    	else if (a.stdin && CommonUtil.o.isEmpty(a.fileName))
    		System.out.println("for reading from stdin, specify name of file for index-id");
    	else
    	{
    		System.out.println("fileName=" + a.fileName);
        	System.out.println("\t data center=" + a.dataCenter);
        	System.out.println("\t ssl=" + a.ssl);
        	System.out.println("\t machineNum=" + a.machineNum);
    	}
    	
    	System.out.println("\t solrServerUrl=" + a.solrServerUrl);
    }

	public String getFileName() {
		return fileName;
	}

	public Boolean getSsl() {
		return ssl;
	}

	public Integer getMachineNum() {
		return machineNum;
	}

	public String getDataCenter() {
		return dataCenter;
	}

	public String getSolrServerUrl() {
		return solrServerUrl;
	}

	public boolean isError() {
		checkError();
		return error;
	}
	
	private void checkError() {
		if (stdin && CommonUtil.o.isEmpty(fileName))
			error = true;
	}
	
	/**
	 * return the LogType from logTypeStr
	 * 
	 * this method is not likely to be called except during Arguments processing
	 * so ok for it to be a loop
	 * 
	 * @param logTypeStr
	 * @return
	 */
	private static LogType getLogType (String logTypeStr)
	{
		LogType[] all = LogType.values();
		for (int i=0; i < all.length; i++)
		{
			if (logTypeStr.equalsIgnoreCase(all[i].name()))
				return all[i];
		}
		
		return null;
	}

}