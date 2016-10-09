/**
 * 
 */
package logSense;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import logSense.BaseFieldParserImpl;
import logSense.ExceptionParser;
import logSense.MsecParser;
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
public class ExceptionParserTest 
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
String s1 = "java.sql.SQLException: you have an error in your SQL statement blah blah";
		
		String s2 = "foo bar [foo.bar.SomeClass/someMethod] xy.uv.FirstException " 
				+   " and a second exception foo.bar.SecondException "
				+   " repeat the first exception xy.uv.FirstException "
				+   " and a third exception foo.bar.ThirdException "
				;
		
		
		BaseFieldParserImpl parser = new ExceptionParser();
		parser.init();
		
		String[] result1 =  {"java.sql.SQLException" };
		String[] result2 =  {"xy.uv.FirstException" , "foo.bar.SecondException" , "foo.bar.ThirdException"};
		testMatch(s1, parser, 1, SolrField.EXCEPTION, result1);
		testMatch(s2, parser, 3, SolrField.EXCEPTION, result2);
		
		
		
	}
	
	private void testMatch(String s, BaseFieldParserImpl parser
			, int numMatches, String fieldName, String[] values)
	{
		ParserResult result = parser.match(s, LogType.tomcat_wwwssl);
		
		assertNotNull(result);
		
		if (numMatches > 0)
		{
			assertEquals(result.nvPairs.size(), numMatches);
			
			int i = 0;
			for (NameValue nv: result.getNvPairs())
			{
				assertEquals(nv.getName(), fieldName);
				assertEquals(nv.getValue(), values[i]);
				
				i++;
			}
			
			
		}
		else
			assertEquals(result.nvPairs == null || result.nvPairs.isEmpty(), true);
		
	}

}
