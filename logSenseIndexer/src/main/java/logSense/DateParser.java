/**
 * 
 */
package logSense;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import logSense.FileParams.LogType;
import logSense.SolrConstants.SolrField;

import org.apache.log4j.Logger;

/**
 * @author am
 *
 */
public class DateParser extends BaseFieldParserImpl 
{

	private static Logger logger = Logger.getLogger(DateParser.class);
	
	public DateParser()
	{
		super(SolrField.DATE);
		
	} 
	
	/*
	 * http://docs.oracle.com/javase/6/docs/api/index.html?java/text/SimpleDateFormat.html
	 */
	protected String[] simpleDateFormatterPattern;
	
	// any valid date must be >= MIN_YEAR and <= MAX_YEAR
	protected int MIN_YEAR = 2011;
		
	// set max year to current year + delta. we are using delta == 2
	protected int MAX_YEAR = Calendar.getInstance().get(Calendar.YEAR) + 2;
	
	/*
	 * if the line matches this pattern, skip (return false on hasDate()).
	 * currently, we have only one example TIMESTAMP: Thu Oct 3 23:46:28 PDT 2013
	 * TODO: these patterns should be read from a file at run time.
	 */
	protected String[] negativePatternStrings;
	
	protected Pattern[] negativePatterns;
	
	protected Pattern measurePattern; 
	
	
	
	public BaseFieldParserImpl init()
	{
		
		ownRegexStr = new String[4];
		
		simpleDateFormatterPattern = new String[4];
		
		// 2013/08/07 15:58:17:088
		ownRegexStr[0] = "\\b(\\d{4}\\/\\d{2}\\/\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}:\\d{1,3})\\b";
		
		simpleDateFormatterPattern[0]= "yyyy/MM/d HH:mm:ss:SSS";
		
			
		// Thu Aug 08 10:00:00 PDT 2013  - ignore Thu and PDT
		ownRegexStr[1] = "\\b(\\w{3}\\s+\\d{1,2}\\s+\\d{2}:\\d{2}:\\d{2}[\\s|\\w]+\\d{4})\\b";
		
		// The captured string is "Aug 07 15:00:10 PDT 2013"
		simpleDateFormatterPattern[1]= "MMM d HH:mm:ss zzz yyyy";
				
		// 2013-08-08 10:00:01:122
		ownRegexStr[2] = "\\b(\\d{4}[-\\/]\\d{2}[-\\/]\\d{2}\\s+\\d{1,2}:\\d{2}:\\d{2}:\\d{1,3})\\b";
		
		simpleDateFormatterPattern[2]= "yyyy-MM-d HH:mm:ss:SSS";
		
		// 26/Nov/2013:14:00:52 -- for access logs
		ownRegexStr[3] = "\\b(\\d{1,2}[-\\/]\\w{3}[-\\/]\\d{4}:\\d{1,2}:\\d{2}:\\d{2})\\b";
		
		simpleDateFormatterPattern[3]= "d/MMM/yyyy:HH:mm:ss";
		
		// init the Patterns for this class
		initOwnPatterns();
		
		
		
		
		measurePattern = Pattern.compile("(BENCH|PROXY|MEASURE|CYCLE)");
		
		this.erase = false;
		this.multiValue = false;
		
		initNegativePatterns();
		
		return this;
		
	}
	
	/*
	 * over ride super because we need to generate the TZ formatted date, for which we would 
	 * be using the SimpleDateFormatter.
	 * 
	 * check if record matches a pattern of interest. if so, return the first value matched.
	 * 
	 * for date, we do not erase matched string from the record
	 */
	public ParserResult match(String record, LogType logType)
	{
		/*
		if (record.indexOf("Created Date") >= 0) 
		{
			int i=1; // breakpoint
		}
		*/
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
		
		// skip negative patterns if there is a measure pattern
		// otherwise, check for negative patterns
		if (!hasMeasure(record2Use) && negativePatterns != null && negativePatterns.length > 0)
		{
			for (int k=0; k < negativePatterns.length; k++)
			{
				Matcher negative = negativePatterns[k].matcher(record2Use);
				if (negative.find())
				{
					result.remainder = record2Use;
					return result;
				}
			}
		}
		
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
						String tzDate = toTZFormat(matched, i);
						
						if (tzDate != null)
						{
							result.add(this.fieldName, tzDate);
							
							atLeastOneThisMatch = true;
							atLeastOne = true;
							
							// if only one match is sufficient, break
							if (!multiValue)
								break;
						}
				
						
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
	
	protected boolean hasMeasure (String record2Use)
	{
		// if record2Use is null, or we don't want to enforce measure pattern (== null)
		if (CommonUtil.o.isEmpty(record2Use) || measurePattern == null)
			return false;
		
		Matcher matcher = measurePattern.matcher(record2Use);
		return (matcher.find());
	}
	
	
	/*
	 * sdfIndex identifies the pattern to use for SimpleDateFormat
	 */
	protected String toTZFormat(String dateStr, int sdfIndex)
	{
		if (sdfIndex < 0 || sdfIndex > simpleDateFormatterPattern.length)
		{
			logger.error("sdfIndex=" + sdfIndex);
			return null;
		}
		
		
		
		String formattedDate = null;
		
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat(simpleDateFormatterPattern[sdfIndex]);
			Date d = sdf.parse(dateStr);
			
			// MIN_YEAR <= d.year <= MAX_YEAR
			int year = 1900 + d.getYear(); // getYear() is num years since 1900
			if ( year >= MIN_YEAR && year <= MAX_YEAR)
			{
			
				// truncate milliseconds in date because the logger often has 00.123 in the prev hour
				// SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");    // "yyyy-MM-dd");
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); 
				formattedDate = formatter.format(d);
			
			}
			else
				formattedDate = null;
			
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
			formattedDate = null;
		}
		return formattedDate;
	}
	
	protected void initNegativePatterns()
	{
		/*
		negativePatternStrings = new String[1];
		
		negativePatternStrings[0] =
				"(\\bTIMESTAMP\\:|\\bModified\\s+Date\\s*\\=|\\bCreated\\s+Date\\s*\\=|\\=DynamicItem)";
		*/
		
		// init negative pattern
		negativePatternStrings = new String[1];
		// negativePatternStrings[0] = "\\bTIMESTAMP:";
		// created : Tue Nov 19 02:50:01 PST 2013
		negativePatternStrings[0] 
				= "(\\=DynamicItem|\\bTIMESTAMP\\:|\\bCreated\\s+Date\\s*\\=|\\bModified\\s+Date\\s*\\=|\\bcreated\\s+\\:|\\btimestamp\\s*=|20132013|\\bLastPurch\\b|\\b\\w+HAS_ACCEPTED_EULA\\b|\\bModDate\\b|\\bSignupDtm\\b)"; // VUDU_HAS_ACCEPTED_EULA, etc
		/*
		   Created Date =
		   Modified Date =
		   =DynamicItem
		   TIMESTAMP:
		   
	    */
		
		if (negativePatternStrings == null || negativePatternStrings.length <= 0)
			return;
		
		negativePatterns = new Pattern[negativePatternStrings.length];
		for (int i = 0; i < negativePatternStrings.length; i++)
			negativePatterns[i] = Pattern.compile(negativePatternStrings[i]);
	}

}
