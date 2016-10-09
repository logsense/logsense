package logSense;

import logSense.SolrConstants.SolrField;

/**
 * 
 * @author am
 *
 */
public class MsecParser extends BaseFieldParserImpl
{
	
	/*
	 * search for 
	 *  24 millis 
	 *  225 elapsed
	 *  
	 *  millis=24
	 *  
	 */
	

	public MsecParser()
	{
		super(SolrField.MSEC);
		
	} 
	public BaseFieldParserImpl init()
	{
		
		ownRegexStr = new String[3];
		
		// millis=24
		ownRegexStr[0] = "\\bmillis\\s*=\\s*(\\d+)\\b";
		
		// 24 millis or 32 msec etc
		// ownRegexStr[0] = "[\\s|\\S]*\\b(\\d+)\\s+(millis|msec|elapsed)\\b";
		ownRegexStr[1] = "\\b(\\d+)\\s+(millis|msec|elapsed)\\b";
		
		
		ownRegexStr[2] = "\\b(\\d+)ms\\b";
			
		// init the Patterns for this class
		initOwnPatterns();
		
		// multiValue is false
		multiValue = false;
		
		return this;
		
	}
}
