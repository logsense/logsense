/**
 * 
 */
package logSense;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import logSense.BaseFieldParserImpl;
import logSense.ClassMethodParser;
import logSense.ParserResult;
import logSense.FileParams.LogType;
import logSense.ParserResult.NameValue;
import logSense.SolrConstants.SolrField;

import org.junit.Before;
import org.junit.Test;

/**
 * @author am
 *
 */
public class ClassMethodParserTest 
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
		String s1 = "WARN [Sun 2011/01/02 13:13:18:067 PST][jefferson.stuff.SomeClass/someMethod] <MsgText: Notification - Timer: Backend Webservice call took 1ms> <SessionID: abcde> <RequestID: ohio_1234>";
		
		String s2 = "SEVERE [Sun 2011/01/02 13:20:51:244 PST][jefferson.foo.bar.AnotherClass/aMethod] <MsgText: Getting default shipping and billing for return customer: {0}";		
		String s3 = "INFO [Sun 2011/01/02 15:00:52:885 PST][jefferson.foo.bar.AnotherClass/aMethod] <MsgText: REQUEST URI - /foo/bar.do> <ThreadID: 96> <SessionID: abcde> <RequestID: ohio_1234>";
		
		String s4 = "SESSION INSTANCE: Store={SSL205} SessionID={1234 url={https://www.jefferson.com/foo/bar.do?null}  1234 bytes in 0 stuff in 2 millis <RequestID: ohio_1234>";
		
		
		BaseFieldParserImpl parser = new ClassMethodParser();
		parser.init();
		
		
		testMatch(s1, parser, 2, "jefferson.stuff.SomeClass", "someMethod");
		testMatch(s2, parser, 2, "jefferson.foo.bar.AnotherClass", "aMethod");
		testMatch(s3, parser, 2, "jefferson.foo.bar.AnotherClass", "aMethod");
		
		testMatch(s4, parser, 0, null, null);
		
		
	}
	
	private void testMatch(String s, BaseFieldParserImpl parser
			, int numMatches, String classValue, String methodValue)
	{
		ParserResult result = parser.match(s, LogType.tomcat_wwwssl);
		
		assertNotNull(result);
		
		if (numMatches > 0)
		{
			assertEquals(result.nvPairs.size(), numMatches);
			
			for (NameValue nv : result.nvPairs)
			{
				if (nv.name.equals(SolrField.CLASS))
					assertEquals(nv.value, classValue);
				
				else if (nv.name.equals(SolrField.METHOD))
					assertEquals(nv.value, methodValue);
				
				else
					fail("unknown field name :" + nv.name);
			}
		}
		else
			assertEquals(result.nvPairs == null || result.nvPairs.isEmpty(), true);
		
	}

}
