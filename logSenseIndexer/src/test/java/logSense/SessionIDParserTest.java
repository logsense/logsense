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
public class SessionIDParserTest 
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
		
		String s1 = "[WARNING][Sun 2011/01/02 13:13:18:067 PST][jefferson.SomeClass/aMethod] <MsgText: blah blah - took 29ms> <ThreadID: 123> <SessionID: abc> <RequestID: ohio_123>";
		
		String s2 = "[SEVERE][Sun 2011/01/02 13:20:51:244 PST][jefferson.aPackage.SomeClass/anotherMethod] <MsgText:blah blah blah: {0}";		
		String s3 = "[WARNING][Sun 2011/01/02 15:00:52:885 PST][jefferson.aPackage.SomeClass/anotherMethod] <MsgText: REQUEST URI - /foo/cartCmd.do> <ThreadID: 123> <SessionID: abcd> <RequestID: oregon_5>";
		
		String s4 = "JETTY INSTANCE: Store={a123} SessionID={01234abcd} url={https://www.yourcompany.com/foo/cart.do?null} serialized 100 bytes in 0 commited in 4 millis <RequestID: oregon_55>";
		
		BaseFieldParserImpl parser = new SessionIDParser();
		parser.init();
		
		testMatch(s1, parser, 1, SolrField.SESSION_ID, "abc");
		testMatch(s2, parser, 0, SolrField.SESSION_ID, null);
		testMatch(s3, parser, 1, SolrField.SESSION_ID, "abcd");
		testMatch(s4, parser, 1, SolrField.SESSION_ID, "01234abcd");
		
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
