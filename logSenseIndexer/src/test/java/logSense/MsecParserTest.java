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
public class MsecParserTest 
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
		String s1 = "JETTY INSTANCE: Store={12} SessionID={abcd url={https://www.yourcompany.com/foo/cart.do?null} serialized 1000 bytes in 0 commited in 6 millis <RequestID: ohio_123>";
		
		String s2 = "ANOTHER INSTANCE: /foo/cart.do 125 elapsed Sun Jan 02 15:00:10 PST 2011 <SessionID: efg> <RequestID: arizona_12345>";
		
		String s3 = "[WARN][Sun 2011/01/02 15:00:52:885 PST][jefferson.SomeClass/aMethod] <MsgText: REQUEST URI - /foo/cartCmd.do> <ThreadID: 6> <SessionID: abcdefg1> <RequestID: oregon_456>";
		
		String s4 ="[WARN][Sun 2011/01/02 04:02:59:601 PST][jefferson.aPackage.bPackage.AnotherClass/anotherMethod] MsgText: blah (more blah) - Timer: 1723ms ThreadID: 1 SessionID: defg RequestID: oregon_7";
		BaseFieldParserImpl parser = new MsecParser();
		parser.init();
		
		testMatch(s1, parser, 1, SolrField.MSEC, "6");
		testMatch(s2, parser, 1, SolrField.MSEC, "125");
		testMatch(s3, parser, 0, SolrField.MSEC, "");
		testMatch(s4, parser, 1, SolrField.MSEC, "1723");
		
		
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
