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
public class CategoryParserTest 
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
		String s1 = "SESSION INSTANCE: Store={123} SessionID={abcde url={https://www.yourcompany.com/foo/bar.do?null} serialized 1000 bytes in 0 commited in 8 millis <RequestID: ohio_1234>";
		
		String s2 = "TOMCAT INSTANCE: /foo/bar.do 124 elapsed Sun Jan 02 15:00:10 PST 2011 <SessionID: abcde> <RequestID: ohio_1234>";
		
		String s3 = "[WARNING][Fri 2011/01/02 15:00:52:885 PST][yourcompany.weblib.cookies.Stuff/callFilter] <MsgText: REQUEST URI - /foo/foo.do> <ThreadID: 6> <SessionID: abcde> <RequestID: ohio_1234>";
		
		String s4 = "2011-01-02 15:08:09:564 - J-Stuff, 1234 - Backend Client Log:Backend/DB INSTANCE: {CALL some_package.some_method(1234,stuff1,stuff2,stuff3)} - millis = 123 SessionID: abcde RequestID:ohio_1234 ";
		
		String s5 = "DB SUB: { call my_package.some_proc( ?,?,? ) } - millis = 1 elapsed Sun Jan 02 15:46:26 PST 2011 Params: {1=1234, 3='X', 2=' stuff '} SessionID: abcde RequestID: oregon_1234 ";
		
		String s6 = "[WARN][Thu 2011/01/02 23:47:54:490 PDT][yourcompany.apps.SomeClass/someMethod] MsgText: SomeMessage - Random warning message \n" 
				+ " bypassFoo 0 /bypassFoo \n"
				+ " items \n"
				+ " item \n"
				+ " id 1234 /id \n"
				+ " address \n"
				+ " state CA /state \n ";
		
		// TOTAL DAL INSTANCE : has a space before colon
		String s7 = "2011-01-02 15:00:49:508 - J-Stuff, 1234 - TOTAL DAL INSTANCE : Requests=[1],Server=[ohio-backend400.yourcompany.com],Session=[123]  - millis = 1 elapsed SessionID: abcde RequestID: oregon_1234 ";
		BaseFieldParserImpl parser = new CategoryParser();
		parser.init();
		
		testMatch(s1, parser, 1, SolrField.CAT, "SESSION INSTANCE");
		testMatch(s2, parser, 1, SolrField.CAT, "TOMCAT INSTANCE");
		testMatch(s3, parser, 0, SolrField.CAT, "");
		testMatch(s4, parser, 1, SolrField.CAT, "DB INSTANCE");
		testMatch(s5, parser, 1, SolrField.CAT, "DB SUB");
		testMatch(s6, parser, 0, SolrField.CAT, "");
		testMatch(s7, parser, 1, SolrField.CAT, "TOTAL DAL INSTANCE");
		
		
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
