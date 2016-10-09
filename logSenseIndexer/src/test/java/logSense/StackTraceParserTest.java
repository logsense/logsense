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
public class StackTraceParserTest 
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
		String s1 = "          at jefferson.abc.def.SomeClass.someMethod(SomeClass.java:69)   ";
		String s2 = "JETTY INSTANCE: /foo/cart.do 1234 elapsed Sun Jan 02 15:00:10 PDT 2011 <SessionID: sessionabcd> <RequestID: ohio_123>";
		
		String s3 = "[WARNING][Sun 2011/01/02 15:00:52:885 PST][jefferson.foo.bar.Whatever/aMethod] <MsgText: REQUEST URI - /foo/cartCmd.do> <ThreadID: 1> <SessionID: blahabcd> <RequestID: arizona_3>";
		
		BaseFieldParserImpl parser = new StackTraceParser();
		parser.init();
		
		testMatch(s1, parser, 1, SolrField.STACK_TRACE, "jefferson.abc.def.SomeClass.someMethod(SomeClass.java:69");
		testMatch(s2, parser, 0, SolrField.STACK_TRACE, "");
		testMatch(s3, parser, 0, SolrField.STACK_TRACE, "");
		
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
