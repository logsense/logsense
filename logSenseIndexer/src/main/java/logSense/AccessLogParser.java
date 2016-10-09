package logSense;

import org.apache.log4j.Logger;

/**
 * 
 * @author am
 *
 */
public class AccessLogParser extends BaseFieldParserImpl
{
	
	private static Logger logger = Logger.getLogger(AccessLogParser.class);
	/*
	 * acess log parser
	 *  
	 *  
	 */
	

	public AccessLogParser()
	{
		super("access-log-fields"); // kludge
		
	} 
	public BaseFieldParserImpl init()
	{
		
		/*
		 * 

			must match date
			fields of interest: 
			date
			IP
			method: POST/GET
			url
			status
			sizeIn 
			sizeOut
			requestId == clientId (later)

		 */
		ownRegexStr = new String[1];
		
		
		ownRegexStr[0] = 
"^([\\w|\\.]+)\\s+\\[[^\\]]+\\]\\s+\\W(\\w+)\\s+(\\S+)\\s+\\S+\\s+(\\d+)\\s+([\\d\\-]+)\\s+([\\d\\-]+)[^\\(]+\\((\\w+)\\)";
			
		// init the Patterns for this class
		initOwnPatterns();
		
		// we cannot quit after matching the class; need to look for the method
		multiValue = true;
		
		return this;
		
	}
	
	// field positions correspond to the regex above
	public static enum AccessLogFields
	{
		ip, httpMethod, url, status, sizeIn, sizeOut
	}
	
	private static AccessLogFields[] accessLogFieldValues = AccessLogFields.values();
	// private static int URL_INDEX = 2; // cached
	
	/*
	 * index starts at 1
	 */
	protected void add2Result(ParserResult result, String matched, int index)
	{
		// skip data that matches -
		if (CommonUtil.o.isEmpty(matched) || matched.trim().equals("-"))
			return;
		
		// for url, strip out the query string
		// index starts at 1. ordinal() at 0
		if (index == AccessLogFields.url.ordinal() + 1)
		{
			int i = matched.indexOf("?");
			String path = i >= 0? matched.substring(0, i) : matched;
			result.add(accessLogFieldValues[index-1].name(), path);
		}
		else if (index >= 1 && index <= accessLogFieldValues.length)
			result.add(accessLogFieldValues[index-1].name(), matched);
		
		else {
			logger.info("index = " + index + ", expecting between 1 and " + accessLogFieldValues.length);
		}
	}
	
	public static void main(String[] args) {
		try
		{
			AccessLogParser p = new AccessLogParser();
			p.init();
			// System.out.println("breakpoint");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
