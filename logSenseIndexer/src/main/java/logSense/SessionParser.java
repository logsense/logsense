package logSense;

import logSense.SolrConstants.SolrField;

/**
 * e.g., a session in a different backend system
 * @author am
 *
 */
public class SessionParser extends BaseFieldParserImpl
{
	
	/*
	 * ,Session=[1234]
	 */
	

	public SessionParser()
	{
		super(SolrField.SESSION);
		
	} 
	public BaseFieldParserImpl init()
	{
		
		ownRegexStr = new String[1];
		
		// ,Session=[1234] 
		ownRegexStr[0] = "\\bSession[:=]\\W+(\\d+)\\b";
		
			
		// init the Patterns for this class
		initOwnPatterns();
		
		// multiValue is false
		multiValue = false;
		
		return this;
		
	}
}
