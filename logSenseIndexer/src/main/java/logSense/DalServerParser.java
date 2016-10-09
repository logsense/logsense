package logSense;

import java.util.regex.Pattern;

import logSense.SolrConstants.SolrField;

/**
 * 
 * @author am
 *
 */
public class DalServerParser extends BaseFieldParserImpl
{
	
	/*
	 * search for
	 *    Server= foo-bar.xyz.com, ...
	 *  
	 */
	

	public DalServerParser()
	{
		super(SolrField.DAL_SERVER);
		
	} 
	public BaseFieldParserImpl init()
	{
		
		ownRegexStr = new String[1];
		
		// Server= foo-bar.xyz.com 
		ownRegexStr[0] = "[^\\-]*\\bServer\\s*=\\s*([\\.\\w\\-]+)";
		
		// case insensitve
		flags = new Integer[1];
		flags[0] = new Integer(Pattern.CASE_INSENSITIVE);
		
			
		// init the Patterns for this class
		initOwnPatterns();
		
		// multiValue is false
		multiValue = false;
		
		return this;
		
	}
}
