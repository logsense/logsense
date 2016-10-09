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
public class SessionIDParserWrapperTest 
{

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception 
	{
	}

	/**
	 * Test method for {@link logSense.BaseFieldParserImpl#match(java.lang.String,logSense.FileParams.LogType)}.
	 */
	@Test
	public void testMatch() 
	{
		// jetty server log
		String s1 = "[WARNING][Sun 2011/01/02 13:13:18:067 PST][jefferson.SomeClass/aMethod] <MsgText: blah blah - took 29ms> <ThreadID: 123> <SessionID: abc> <RequestID: ohio_123>";
		
		String s2 = "[SEVERE][Sun 2011/01/02 13:20:51:244 PST][jefferson.aPackage.SomeClass/anotherMethod] <MsgText:blah blah blah: {0}";		
		String s3 = "[WARNING][Sun 2011/01/02 15:00:52:885 PST][jefferson.aPackage.SomeClass/anotherMethod] <MsgText: REQUEST URI - /foo/cartCmd.do> <ThreadID: 123> <SessionID: abcd> <RequestID: oregon_5>";
		
		String s4 = "JETTY INSTANCE: Store={a123} SessionID={01234abcd} url={https://www.yourcompany.com/foo/cart.do?null} serialized 100 bytes in 0 commited in 4 millis <RequestID: oregon_55>";
		
		// backend server log
		String s5 = "2011-01-02 23:00:00:461 - blah, 123 -> DAL Log:DAL INSTANCE: {hostName= ohio-www12, Session= 123, stuff } -> millis = 1 <SessionID: abc> <RequestID: ohio_123>";
		String s6 = "2011-01-02 23:00:00:465 - blah-835, 456 -> DAL Log:ROUTER INSTANCE: blah,SQL=[{ call some_package.a_method(?,?) }],Session=[123] -> millis = 0 elapsed <SessionID: abcd> <RequestID: ohio_123>";
		String s7 = "2011-10-02 23:00:00:459 - blah-420, 789 -> DAL Log:ROUTER INSTANCE: blah,SQL=[{ call  a_package.another_method(?,?)}],Session=[456] -> millis = 0 elapsed <SessionID: def> <RequestID: oregon_123>";
		
		BaseFieldParserImpl parser = new SessionIDParserWrapper();
		parser.init();
		
		testMatchTomcatData(s1, parser, 1, SolrField.SESSION_ID, "abc");
		testMatchTomcatData(s2, parser, 0, SolrField.SESSION_ID, null);
		testMatchTomcatData(s3, parser, 1, SolrField.SESSION_ID, "abcd");
		testMatchTomcatData(s4, parser, 1, SolrField.SESSION_ID, "01234abcd");
		
		testMatchDalServerData(s5, parser, 2,  "abc", "123");
		testMatchDalServerData(s6, parser, 2,  "abcd", "123");
		testMatchDalServerData(s7, parser, 2,  "def", "456");
		
	}
	
	private void testMatchTomcatData(String s, BaseFieldParserImpl parser
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
	
	private void testMatchDalServerData(String s, BaseFieldParserImpl parser
			, int numMatches,  String tomcatSessionId,  String dalSessionId)
	{
		ParserResult result = parser.match(s, LogType.backend1);
		
		assertNotNull(result);
		
		if (numMatches == 1)
		{
			if (result.nvPairs.size() != numMatches)
				System.out.println("breakpoint");
			assertEquals(result.nvPairs.size(), numMatches);
			NameValue nv = result.nvPairs.get(0);
			
			if (tomcatSessionId != null)
			{
				assertEquals(nv.name, SolrField.SESSION_ID);
				assertEquals(nv.value, tomcatSessionId);
			}
			
			else if (dalSessionId != null)
			{
				assertEquals(nv.name, SolrField.SESSION);
				assertEquals(nv.value, dalSessionId);
			}
			
			else
			{
				// error
				
			}

		}
		else if (numMatches == 2)
		{
			assertEquals(result.nvPairs.size(), numMatches);
			NameValue nv0 = result.nvPairs.get(0);
			NameValue nv1 = result.nvPairs.get(1);
			int xyz=1;
			if (tomcatSessionId != null)
			{
				assertEquals(nv0.name, SolrField.SESSION_ID);
				assertEquals(nv0.value, tomcatSessionId);
			}
			
			if (dalSessionId != null)
			{
				assertEquals(nv1.name, SolrField.SESSION);
				assertEquals(nv1.value, dalSessionId);
			}

		}
		else
			assertEquals(result.nvPairs == null || result.nvPairs.isEmpty(), true);
		
	}

}
