package logSense;

import logSense.SolrConstants.SolrField;

import org.apache.log4j.Logger;

/**
 * 
 * @author am
 *
 */
public class ExceptionParser extends BaseFieldParserImpl
{
	
	private static Logger logger = Logger.getLogger(ExceptionParser.class);
	

	public ExceptionParser()
	{
		super(SolrField.EXCEPTION); 
		
	} 
	public BaseFieldParserImpl init()
	{
		
		ownRegexStr = new String[1];
		
		// foo.bar.BlahException: error message
		ownRegexStr[0] = "\\b([\\w|\\.]+Exception)\\b";
			
		// init the Patterns for this class
		initOwnPatterns();
		
		// we cannot quit after matching the class; need to look for the method
		multiValue = true;
		
		return this;
		
	}
	
	/*
	 * we may see the same exception multiple times in a record. if duplicate, have only one
	 */
	protected void add2Result(ParserResult result, String matched, int index)
	{
		result.addIfUniqueValue(SolrField.EXCEPTION, matched);
		
	}
}
