/**
 * 
 */
package logSense;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logSense.FileParams.LogType;
import logSense.ParserResult.NameValue;
import logSense.SolrConstants.SolrField;

import org.apache.log4j.Logger;

/**
 * 
 * Parses a record for various fields. The fields of interest are defined in SolrConstants.SolrField
 * @author amukherjee
 *
 */
public class ParserManager 
{
	private static Logger logger = Logger.getLogger(ParserManager.class);
	
	private List<FieldParser> parsers;
	
	// kludge - UrlParser is applied only if TOMCAT BENCH... this should be  made hierarchical.
	private FieldParser urlParser;
	
	public ParserManager() {
		urlParser = (new UrlParser()).init();
	}
	
	
	public void register(FieldParser fieldParser)
	{
		if (parsers == null)
			parsers = new ArrayList<FieldParser>();
		
		parsers.add(fieldParser);
	}
	
	public void resetAllParsers()
	{
		for (FieldParser parser : parsers)
			parser.reset();
	}
	
	public ParserResult parse(String record, LogType logType)
	{
		if (parsers == null)
			return null;
		
		ParserResult result = new ParserResult();
		
		
		// for parsing the indiv fields, use recordMinusDate
		String str2Use = record;
		
		for (FieldParser parser : parsers)
		{
			ParserResult parserResult = parser.match(str2Use, logType);
			if (parserResult != null && parserResult.hasMatch())
			{
				result.addAll(parserResult.nvPairs);
				result.addAllDiscovered(parserResult.discovered);
				if (result.shardRouteKey == null && !CommonUtil.o.isEmpty(parserResult.shardRouteKey))
					result.shardRouteKey = parserResult.shardRouteKey;
				
				str2Use = parserResult.remainder;
				
				// if requestId, save it for childId / parentId
				String fieldName = parserResult.nvPairs.get(0).name;
				String fieldValue = parserResult.nvPairs.get(0).value;
				if (fieldName.equalsIgnoreCase(SolrField.REQUEST_ID))
					result.requestId = fieldValue;
				
				// if TOMCAT BENCH, apply the url parser
				//
				else if (fieldName.equalsIgnoreCase(SolrField.CAT) &&
						fieldValue.equalsIgnoreCase("TOMCAT BENCH") )
				{
					result.isParent = true;
					parserResult = urlParser.match(record, logType);
					if (parserResult != null && parserResult.hasMatch())
					{
						result.addAll(parserResult.nvPairs); 
						// parserResult.discovered has no value for urlParser
						
						// for api, also set the Solr Field facetUrl
						if (logType == LogType.wsapi)
						{
							for (NameValue nv : parserResult.nvPairs)
							{
								result.add(SolrField.FACET_URL, nv.value);
							}
						}
					}
					
				}
				
				if (CommonUtil.o.isEmpty(str2Use))
					break;
			}
		}
		
		if (!CommonUtil.o.isEmpty(result.requestId)) {
			if (result.isParent)
				result.add(SolrField.PARENT_ID, result.requestId);
			else
				result.add(SolrField.CHILD_ID, result.requestId);
		}
		
		return result;
	}
	

}
