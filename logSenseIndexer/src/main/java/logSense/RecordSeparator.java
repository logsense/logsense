package logSense;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import logSense.FileParams.LogType;

import org.apache.log4j.Logger;

/**
 * 
 * @author am
 *
 */
public class RecordSeparator
{
	private static Logger logger = Logger.getLogger(RecordSeparator.class);
	
	public static RecordSeparator o = new RecordSeparator();
	
	protected Pattern measurePattern;
	private RecordSeparator() 
	{
		measurePattern = Pattern.compile("(BENCH|PROXY|CYCLE|MEASURE)");
	}
	
	public RecordSepInfo getRecordSepInfo(String line, LogType logType)
	{
		RecordSepInfo result = new RecordSepInfo();
		
		// for logmon, every line is a separate record
		if (logType == LogType.logmon_www)
		{
			result.status = true;
			return result;
		}
		
		boolean measureCheck = hasMeasure(line);
		
		ParserResult parsedDate = hasDate(line, logType);
		
		result.parsedDate = parsedDate;
		
		// status is true if there is a MEAUSURE or a date (with no negative date patterns)
		result.status = measureCheck || (parsedDate != null && parsedDate.hasMatch());
		
		return result;
	}
	
	protected boolean hasMeasure (String line)
	{
		if (CommonUtil.o.isEmpty(line))
			return false;
		
		Matcher matcher = measurePattern.matcher(line);
		return (matcher.find());
	}
	
	
	protected ParserResult hasDate(String line, LogType logType)
	{
		if (CommonUtil.o.isEmpty(line))
			return null;
		
		FieldParser dateParser = ParserManagerMap.o.getDateParser(logType);
		
		ParserResult result = dateParser.match(line, logType);
		return result;
	}
	
	public static class RecordSepInfo
	{
		boolean status; // true if this is a separator
		ParserResult parsedDate;
		
		public boolean isSeparator() {
			return status;
		}
		
		
		public ParserResult getParsedDate() {
			return parsedDate;
		}
		
	}
}