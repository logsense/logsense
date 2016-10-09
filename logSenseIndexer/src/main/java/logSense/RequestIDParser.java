package logSense;

import logSense.SolrConstants.SolrField;

/**
 * 
 * @author am
 *
 */
public class RequestIDParser extends BaseFieldParserImpl
{
	

	public RequestIDParser()
	{
		super(SolrField.REQUEST_ID);
		
	} 
	public BaseFieldParserImpl init()
	{
		ownRegexStr = new String[1];
		
		// <RequestID: abc_01234-5678>   -- may also have - (minus)
		ownRegexStr[0] = "\\bRequestID[:=]\\W+([\\w\\-]+)\\b";
		
			
		// init the Patterns for this class
		initOwnPatterns();
		
		// multiValue is false
		multiValue = false;
		
		return this;
		
	}
	
	/*
	 * if we were to use requestId to route to a shard. currently,we are routing an entire session to the same shard
	 *
	protected void add2Result(ParserResult result, String matched, int index)
	{
		super.add2Result(result, matched, index);
		
		if (!CommonUtil.o.isEmpty(matched))
			result.shardRouteKey = matched.toLowerCase().trim();
	}
	*/
}
