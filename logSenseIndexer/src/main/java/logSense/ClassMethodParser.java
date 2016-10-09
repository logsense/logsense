package logSense;

import logSense.SolrConstants.SolrField;

import org.apache.log4j.Logger;

/**
 * 
 * @author am
 *
 */
public class ClassMethodParser extends BaseFieldParserImpl
{
	
	private static Logger logger = Logger.getLogger(ClassMethodParser.class);
	/* 
	 *  handle both class and method
	 *  
	 */
	

	public ClassMethodParser()
	{
		super("class-method"); // kludge
		
	} 
	public BaseFieldParserImpl init()
	{
		
		ownRegexStr = new String[1];
		
		// [jefferson.blah.blah.SomeClass/someMethod] 
		ownRegexStr[0] = "\\[(jefferson\\.[\\w|\\.]+)\\/(\\w+)\\b";
			
		// init the Patterns for this class
		initOwnPatterns();
		
		// we cannot quit after matching the class; need to look for the method
		multiValue = true;
		
		return this;
		
	}
	
	/*
	 * kludge
	 * we put this in a method so it can be overridden.
	 * for class/method, we have two Solr fields being matched by the same parser
	 * 
	 * index == 1 for class, 2 for method
	 */
	protected void add2Result(ParserResult result, String matched, int index)
	{
		if (index == 1)
			result.add(SolrField.CLASS, matched);
		else if (index == 2)
			result.add(SolrField.METHOD, matched);
		else {
			// there is rare in the data. let's skip it
			// logger.info("index = " + index + ", expecting 1 or 2");
		}
	}
}
