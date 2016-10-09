/**
 * 
 */
package logSense;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import logSense.FileParams.LogType;

/**
 * @author am
 *
 */
abstract public class BaseFieldParserImpl implements FieldParser 
{


	protected String fieldName;
	protected String[]  ownRegexStr;
	protected boolean erase;
	protected boolean multiValue;
	
	protected Pattern[] ownPatterns;
	protected Integer[] flags; // if present, Pattern.compile(ownPatterns[i], flags[i])
	
	/*
	 * search for patterns specified in ownRegexStr
	 *
	 *  
	 * capture the text pieces that are matched.
	 * 
	 * if erase is true, remove the matched values, replacing them with a space
	 * 
	 * if (multiValue is true) keep looking for matches even after a match has been found.
	 *  
	 *  
	 */
	
	public BaseFieldParserImpl(String fieldName )
	{
		this(fieldName, true, true);
		
	}
	
	public BaseFieldParserImpl(String fieldName
							, boolean erase // should the parser erase stuff that is matched
							, boolean multiValue // should the parser look for additional matches once there is a match? default = true
							)
	{
		this.fieldName = fieldName;
		this.erase = erase;
		this.multiValue = multiValue;
	}
		
	// init ownPatterns with one or more regex patterns
	protected void initOwnPatterns()
	{

		ownPatterns = new Pattern[ownRegexStr.length];
		for (int i = 0; i < ownRegexStr.length; i++	)
		{
			if( flags == null || flags.length < i+1 || flags[i] == null)	
				ownPatterns[i] = Pattern.compile(ownRegexStr[i]);
			else
				ownPatterns[i] = Pattern.compile(ownRegexStr[i], flags[i].intValue());
		}
	}

	
	/*
	 * check if record matches a pattern of interest. if so, return the values matched
	 * for future processing.
	 * 
	 * if erase is true, remove the matched string from the record
	 * 
	 * if multiValue is true, match them all. This is the default. For date (used as field sep), 
	 * one match is sufficient
	 */
	public ParserResult match(String record, LogType logType)
	{
		
		if (ownPatterns == null)
			initOwnPatterns();
		
		if (CommonUtil.o.isEmpty(record))
			return null;
		
		String record2Use = record;
		
		/*
		 * if at least one match, and erase==true, we will erase matching stuff from record and set it as
		 * remainder
		 */
		
		ParserResult result = new ParserResult();
		
		// at least one match for whole string
		boolean atLeastOne = false;
		
		// check line against several different date regex patterns
		for (int i=0; i < ownPatterns.length; i++)
		{
			if (atLeastOne && !multiValue)
				break;
			
			Matcher matcher = ownPatterns[i].matcher(record2Use);
			
			// at least one match with a given pattern
			boolean atLeastOneThisMatch = false;
			int index = 1;
			while (matcher.find())
			{
				if (atLeastOneThisMatch && !multiValue)
					break;
				
				// skip group 0,that's the outside group. start with group 1
				for (int groupNum = 1; groupNum <= matcher.groupCount(); groupNum++) 
				{			    
					String matched = matcher.group(groupNum);
					
					if (!CommonUtil.o.isEmpty(matched))
					{
		
						// result.add(fieldName, matched);
						add2Result(result, matched, index++);
						atLeastOneThisMatch = true;
						atLeastOne = true;
						
						// if only one match is sufficient, break
						if (!multiValue)
							break;
					}
					
				}
				
				
			}
			
			// if at least one match, then process "erase"
			if (atLeastOneThisMatch && erase)
			{
				// replace all matches with space
				record2Use = matcher.replaceAll(" ");
			}
		}
		
		result.remainder = record2Use;
		
		return result;
	}

	/*
	 * kludge
	 * we put this in a method so it can be overridden.
	 * for class/method, we have two Solr fields being matched by the same parser
	 * 
	 * index is ignored in the default case... all matches are for the same Solr field
	 */
	protected void add2Result(ParserResult result, String matched, int index)
	{
		result.add(fieldName, matched.trim());
	}
	
	public String getFieldName()
	{
		return fieldName;
	}
	
	public boolean hasMatch (String record, LogType logType)
	{
		ParserResult p = match(record, logType);
		if (p != null && p.hasMatch())
			return true;
		else
			return false;
	}
	
	public void reset()
	{
		// no op for the general case. override as needed.
	}
	
	abstract public BaseFieldParserImpl init();

}
