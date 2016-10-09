package logSense;

import logSense.SolrConstants.SolrField;

/**
 * 
 * @author am
 *
 */
public class DalPoolParser extends BaseFieldParserImpl
{
	
	/*
	 * search for 
	 *  Pool= dalpool_poolName
	 *  
	 */
	

	public DalPoolParser()
	{
		super(SolrField.DAL_POOL);
		
	} 
	public BaseFieldParserImpl init()
	{
		
		ownRegexStr = new String[1];
		
		// Pool= dalpool_poolName
		ownRegexStr[0] = "\\bPool\\s*=\\s*dalpool_(\\w+)\\b";
		
		// init the Patterns for this class
		initOwnPatterns();
		
		// multiValue is false
		multiValue = false;
		
		return this;
		
	}
}
