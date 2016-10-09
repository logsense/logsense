package logSense;

import logSense.SolrConstants.SolrField;

/**
 * 
 * @author am
 *
 */
public class StackTraceParser extends BaseFieldParserImpl
{
	
	/*
	 * search for a stack trace
	 *  
	 */
	

	public StackTraceParser()
	{
		super(SolrField.STACK_TRACE);
		
	} 
	public BaseFieldParserImpl init()
	{
		
		ownRegexStr = new String[1];
		
		// at foo.bar.ClassName.methodName(ClassName.java:200)
		
		ownRegexStr[0] = "\\bat\\s+([\\w\\.]+\\(\\w+\\.java:\\d+)\\b";
			
		// init the Patterns for this class
		initOwnPatterns();
		
		// multiValue is false
		multiValue = false;
		erase = false;
		
		return this;
		
	}
}
