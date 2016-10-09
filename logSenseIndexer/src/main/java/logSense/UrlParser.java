package logSense;

import logSense.SolrConstants.SolrField;

/**
 * 
 * @author am
 *
 */
public class UrlParser extends BaseFieldParserImpl
{
	
	/*
	 * search for 
	 *  /cart2/
	 *  etc.
	 *  
	 *  url={https://foo-bar.xyz.com/...} 
	 */
	

	public UrlParser()
	{
		super(SolrField.URL);
		
	} 
	public BaseFieldParserImpl init()
	{
		
		ownRegexStr = new String[2];
		
		// /cart2/...
		// /ip/ etc
		ownRegexStr[0] = "\\s(\\/[^\\/\\s]+[\\/|^\\s][^\\s]*)\\s+";
		
		ownRegexStr[1] = "\\burl=\\{([^\\}\\s]+)\\}";
			
		// init the Patterns for this class
		initOwnPatterns();
		
		// multiValue is false
		multiValue = false;
		erase=false;
		
		return this;
		
	}
	
	/**
	 * add only the url path in the url field, leave out the query string because we want to facet on something that
	 * is common search as /search/search-ng.do
	 * 
	 */
	protected void add2Result(ParserResult result, String matched, int index)
	{
		if (matched != null)
		{
			int i = matched.indexOf("?");
			String path = i >= 0? matched.substring(0, i) : matched;
			result.add(fieldName, path);
		}
		
	}
}
