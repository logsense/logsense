/**
 * 
 */
package logSense;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import logSense.BaseFieldParserImpl;
import logSense.ParserResult;
import logSense.StoredProcParser;
import logSense.FileParams.LogType;
import logSense.ParserResult.NameValue;
import logSense.SolrConstants.SolrField;

import org.junit.Before;
import org.junit.Test;

/**
 * @author am
 *
 */
public class StoredProcParserTest 
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
		String s1 = "DB SUB: { call some_package.a_method(?,?,?) } - millis = 10 elapsed Sun Jan 02 01:01:43 PST 2011 Params: {1=-1, 2=234, 3=' OUT: INTEGER '} SessionID: abcd RequestID: oregon_123 ";
		String s2 = "JETTY INSTANCE: /foo/cart.do 675 elapsed Sun Jan 02 15:00:10 PST 2011 <SessionID: asdf> <RequestID: arizona_ghjk>";
		
		String s3 = "DB INSTANCE: { call another_package.another_method (?,?,?,?,?,?)} - millis = 5 elapsed Sun Jan 2 01:01:56 PST 2011 Params: {} SessionID: 1234abc RequestID: oregon_abc ";
		
		String s4 = "2011-01-02 22:08:09:564 - blah, 12 - L2 Client Log:BLAH INSTANCE: {CALL a_package.a_method(12,Blah)} - millis = 23 SessionID: 56defg RequestID: abc-oregon_efc_123";
		
		BaseFieldParserImpl parser = new StoredProcParser();
		parser.init();
		
		testMatch(s1, parser, 1, SolrField.STORED_PROC, "some_package.a_method");
		testMatch(s2, parser, 0, SolrField.STORED_PROC, "");
		testMatch(s3, parser, 1, SolrField.STORED_PROC, "another_package.another_method");
		testMatch(s4, parser, 1, SolrField.STORED_PROC, "a_package.a_method");
		
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
			assertEquals(nv.value, value.toLowerCase());
			
		}
		else
			assertEquals(result.nvPairs == null || result.nvPairs.isEmpty(), true);
		
	}

}
