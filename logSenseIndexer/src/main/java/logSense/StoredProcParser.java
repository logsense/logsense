package logSense;

import java.util.regex.Pattern;

import logSense.SolrConstants.SolrField;

/**
 * 
 * @author am
 *
 */
public class StoredProcParser extends BaseFieldParserImpl
{
	
	/*
	 * { call some_pkg.some_method( ?, ?, ?, ?, ?, ...,?) }
	 * 
	 *  
	 */
	

	public StoredProcParser()
	{
		super(SolrField.STORED_PROC);
		
	} 
	public BaseFieldParserImpl init()
	{
		
		ownRegexStr = new String[1];
		
		// any number of periods 
		// limitation is that we have 3 chars min incl period
		ownRegexStr[0] = "\\bcall\\s+(\\w[\\.|\\w]+\\w)\\b\\s*\\([^\\)]*\\)";
		
		// case insensitive.
		flags = new Integer[1];
		flags[0] = new Integer(Pattern.CASE_INSENSITIVE ); 
			
		// init the Patterns for this class
		initOwnPatterns();
		
		// multiValue is false
		multiValue = false;
		
		return this;
		
	}
	
	// add the stored proc as lower case
	protected void add2Result(ParserResult result, String matched, int index)
	{
		result.add(fieldName, matched.toLowerCase());
	}
}
