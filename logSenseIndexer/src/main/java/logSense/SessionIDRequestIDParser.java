package logSense;

import java.util.regex.Matcher;

import logSense.FileParams.LogType;
import logSense.SolrConstants.SolrField;

import org.apache.log4j.Logger;

/**
 * 
 * @author am
 *
 */
public class SessionIDRequestIDParser extends BaseFieldParserImpl
{
	
	private static Logger logger = Logger.getLogger(SessionIDRequestIDParser.class);
	
	public SessionIDRequestIDParser()
	{
		super("sessionID-requestID"); // kludge
		
	} 
	public BaseFieldParserImpl init()
	{
		
		ownRegexStr = new String[1];
		
		/*
		 	<SessionID: abc01234> 
			<RequestID: xyz_0a1b-5678>
		 */
		ownRegexStr[0] = "<(Session|Request)ID:\\s+(\\w+)>";
			
		// init the Patterns for this class
		initOwnPatterns();
		
		// we cannot quit after matching the class; need to look for the method
		multiValue = true;
		
		return this;
		
	}
	
	
	
	public ParserResult match(String record, LogType logType)
	{
		
		if (ownPatterns == null)
			initOwnPatterns();
		
		if (CommonUtil.o.isEmpty(record))
			return null;
		
		String record2Use = record;
		
		/*
		 * if at least one match, and erase==true, we will erase matching stuff from record and set it as
		 * remainder
		 */
		
		ParserResult result = new ParserResult();
		
		// at least one match for whole string
		boolean atLeastOne = false;
		
		// check line against several different date regex patterns
		for (int i=0; i < ownPatterns.length; i++)
		{
			if (atLeastOne && !multiValue)
				break;
			
			Matcher matcher = ownPatterns[i].matcher(record2Use);
			
			// at least one match with a given pattern
			boolean atLeastOneThisMatch = false;
			int index = 1;
			while (matcher.find())
			{
				if (atLeastOneThisMatch && !multiValue)
					break;
				
				// skip group 0,that's the outside group. start with group 1
				for (int groupNum = 1; groupNum <= matcher.groupCount(); groupNum += 2) 
				{			    
					String what = matcher.group(groupNum);
					String matched = matcher.group(groupNum+1);
					
					if (what != null )
					{
						if (what.equalsIgnoreCase("session"))
							what = SolrField.SESSION_ID;
						else if (what.equalsIgnoreCase("request"))
							what = SolrField.REQUEST_ID;
						else
							continue;
					}
					
					if (!CommonUtil.o.isEmpty(matched))
					{
		
						// result.add(fieldName, matched);
						result.add(what, matched);
						atLeastOneThisMatch = true;
						atLeastOne = true;
						
						// if only one match is sufficient, break
						if (!multiValue)
							break;
					}
					
				}
				
				
			}
			
			// if at least one match, then process "erase"
			if (atLeastOneThisMatch && erase)
			{
				// replace all matches with space
				record2Use = matcher.replaceAll(" ");
			}
		}
		
		result.remainder = record2Use;
		
		return result;
	}
}
