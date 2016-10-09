/**
 * 
 */
package logSense;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import logSense.BaseFieldParserImpl;
import logSense.DateParser;
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
public class DateParserTest 
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
		String s1 = "JETTY INSTANCE: Store={a100} SessionID={abcde url={https://www.yourcompany.com/foo/cart.do?null} serialized 500 bytes in 0 commited in 8 millis <RequestID: ohio_1234>";
		
		String s2 = "TOMCAT INSTANCE: /foo/cart.do 65 elapsed Sun Jan 02 15:00:10 PST 2011 <SessionID: uvwxy> <RequestID: arizona_1234>";
		
		String s3 = "[WARNING][Sun 2011/01/02 15:00:52:885 PST][jefferson.foo.AClass/aMethod] <MsgText: REQUEST URI - /foo/cartCmd.do> <ThreadID: 96> <SessionID: abcd3> <RequestID: oregon_123>";
		
		String s4 = "2011-01-02 00:21:10:51 - stuff, 1234 - TOTAL DAL INSTANCE : Requests=[6],Server=[oregon-v10-dal999.jefferson.com],Session=[1234] invalidated - millis = 9 elapsed SessionID: uvw RequestID: oregon_abc123";
		
		// TIMESTAMP: is ruled out, so this should match the negative pattern, and not return a date
		String s5 = "TIMESTAMP: Sun Jan 2 23:46:28 PST 2011";
		
		String s6 = "Modified Date = Sun Jan 2 02:53:38 PST 2011";
		String s7 = "Created Date = Sun Jan 2 02:39:46 PST 2011";
		String s8 = "=DynamicItem to String Foo{id=17397878, timestamp=Sun Jan 2 02:53:28 PST 2011, bar=0, ab=1000, blah=''} X=3, bla";
		
		String s9 = " created : Sun Jan 2  02:50:01 PST 2011";
		String s10 = " timestamp= Sun Jan  2 02:50:01 PST 2011";
		
		String s11 = "127.0.0.1 [2/Jan/2011:01:29:17 -0800] \"POST /foo/bar HTTP/1.1\" 200 123 456 \"-\" \"Java/1.8 " +
				" \"HTTP/1.1 ohio100 (ohio_123)\"";
		
		
		String s12="Stuff - Sun Jan 02 16:00:00 PST 2011 \n" 
					+ "LastModBy - \n"
					+ "ModDate - 2011-01-03";
		
		String s13 = "BLAH - Thu Jan 06 22:31:32 PST 2005\n"
					+ "Stuff - 1\n"
					+ "Foo - Bar";
		
		
		
		String s15="2011-01-02 23:50:17:917 - blah, 123 -> DAL SERVER INSTANCE:�";
		
		// this is a string where the log has an error -- 20112011 has no space. we want to skip these records.
		String s16="hello.SomeClass - Blah Blah [cache - jefferson.RamObjectCache{ name=x, namespace={ y[jefferson.Namespace], z[catalog], stuff[jdbcpool_catalog]}] Fri Jan 01 20:47:06 PST 20112011-01-01 20:47:06:497 - foo, 123 - DAL SERVER INSTANCE: { Server= ohio-dal123.yourcompany.com, Session= 123,  Pool= dalpool_cart, Sql=[{ call apackage.aMethod(?,?,?) }] } - millis = 1 SessionID: ";
		
		String s17="Stuff - Wed Dec 31 16:00:00 PST 1969 "
				+ " LastModBy - "
				+ " ModDate - 2011-01-23 " ;
		BaseFieldParserImpl parser = new DateParser();
		parser.init();
		
		testMatch(s9, parser, 0, SolrField.DATE, "");
		
		
		
		
		testMatch(s1, parser, 0, SolrField.DATE, "");
		
		// Sun Jan 02 15:00:10 PST 2011
		testMatch(s2, parser, 1, SolrField.DATE, "2011-01-02T15:00:10Z");
		
		// Sun 2011/01/02 15:00:52:885 PST
		testMatch(s3, parser, 1, SolrField.DATE, "2011-01-02T15:00:52Z");
		
		// 2011-01-02 00:21:10:51
		testMatch(s4, parser, 1, SolrField.DATE, "2011-01-02T00:21:10Z");
		
		// we want to skip dates that match a so called negative pattern. 
		testMatch(s5, parser, 0, SolrField.DATE, "");
		testMatch(s6, parser, 0, SolrField.DATE, "");
		testMatch(s7, parser, 0, SolrField.DATE, "");
		testMatch(s8, parser, 0, SolrField.DATE, "");
		testMatch(s9, parser, 0, SolrField.DATE, "");
		testMatch(s10, parser, 0, SolrField.DATE, "");
		
		// 2/Jan/2011:01:29:17 
		testMatch(s11, parser, 1, SolrField.DATE, "2011-01-02T01:29:17Z");
		
		testMatch(s12, parser, 0, SolrField.DATE, "");
		testMatch(s13, parser, 0, SolrField.DATE, "");

		//2011-01-02 23:50:17:917
		testMatch(s15, parser, 1, SolrField.DATE, "2011-01-02T23:50:17Z");
		
		testMatch(s16, parser, 0, SolrField.DATE, "");
		testMatch(s17, parser, 0, SolrField.DATE, "");
		
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
		else {
			if (result.nvPairs != null && !result.nvPairs.isEmpty()) {
				System.out.println("breakpoint");
			}
			assertEquals(result.nvPairs == null || result.nvPairs.isEmpty(), true);
		}
		
	}

}
