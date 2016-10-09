package logSense;

import logSense.SolrConstants.SolrField;

/**
 * 
 * @author am
 *
 */
public class SessionIDParser extends BaseFieldParserImpl
{
	
	
	public SessionIDParser()
	{
		super(SolrField.SESSION_ID);
		
	} 
	public BaseFieldParserImpl init()
	{
		
		ownRegexStr = new String[1];
		
		// <SessionID: 0000000fe1234> 
		// ... SessionID={0000000fe1234 ...
		ownRegexStr[0] = "\\bSessionID[:=]\\W+(\\w+)\\b";
		
			
		// init the Patterns for this class
		initOwnPatterns();
		
		// multiValue is false
		multiValue = false;
		
		return this;
		
	}
}
