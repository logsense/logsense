/**
 * 
 */
package logSense;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import logSense.FileParams.LogType;
import logSense.ParserResult.NameValue;
import logSense.SolrConstants.SolrField;

import org.junit.Before;
import org.junit.Test;

/**
 * @author am
 *
 */
public class UrlParserTest 
{

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception 
	{
	}

	/**
	 * Test method for {@link logSense.BaseFieldParserImpl#match(java.lang.String)}.
	 */
	@Test
	public void testMatch() 
	{
		String s1 = "JETTY INSTANCE: /bar/search.do?param1=1&q=blah+blah&cat_id=123 104 elapsed Sun Jan 2 15:05:06 PST 2011 SessionID: asdfg RequestID: oregon_axdw1 blah blah ";
		
		String s2 = "TOMCAT INSTANCE: /foo/cart.do 15 elapsed Sun Jan 02 15:00:10 PST 2011 <SessionID: asdf> <RequestID: ohio_123>";
		
		String s3 = "[WARNING][Sun 2011/01/02 15:00:52:885 PST][jefferson.somePackage.SomeClass/aMethod] <MsgText: REQUEST URI - /foo/cartCmd.do> <ThreadID: 2> <SessionID: abcd> <RequestID: oregon_asdf123>";
		
		BaseFieldParserImpl parser = new UrlParser();
		parser.init();
		
		testMatch(s1, parser, 1, SolrField.URL, "/bar/search.do"); 
		testMatch(s2, parser, 1, SolrField.URL, "/foo/cart.do");
		testMatch(s3, parser, 1, SolrField.URL, "/foo/cartCmd.do>");
		
	}
	
	private void testMatch(String s, BaseFieldParserImpl parser
			, int numMatches, String fieldName, String value)
	{
		ParserResult result = parser.match(s, LogType.tomcat_wwwssl);
		
		assertNotNull(result);
		
		if (numMatches > 0)
		{
			assertEquals(result.nvPairs.size(), numMatches);
			NameValue nv = result.nvPairs.get(0);
			assertEquals(nv.name, fieldName);
			assertEquals(nv.value, value);
			
		}
		else
			assertEquals(result.nvPairs == null || result.nvPairs.isEmpty(), true);
		
	}

}
