package logSense;

import logSense.SolrConstants.SolrField;

/**
 * 
 * @author am
 *
 */
public class CategoryParser extends BaseFieldParserImpl
{
	
	/*
	 * search for 
	 * 	MEASURE:
	 * 	
	 * one match sufficient
	 * 
	 * value is name of the MEASURE
	 *  
	 *  
	 */
	
	

	public CategoryParser()
	{
		super(SolrField.CAT);
		
	} 
	public BaseFieldParserImpl init()
	{
		
		ownRegexStr = new String[3];
		
		// first preference to a phrase + numbers + / that end with MEASURE or INSTANCE:
		ownRegexStr[0] = "\\b([A-Z][A-Z\\d\\s/]+MEASURE)\\s*:";
		ownRegexStr[1] = "\\b([A-Z][A-Z\\d\\s/]+INSTANCE)\\s*:";
		
		
		// DB SUB
		ownRegexStr[2] = "\\b(DB\\s+SUB):";
			
		// init the Patterns for this class
		initOwnPatterns();
		
		// multiValue is false
		multiValue = false;
		
		return this;
	}
}
