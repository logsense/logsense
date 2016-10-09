/**
 * 
 */
package logSense;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import logSense.BaseFieldParserImpl;
import logSense.DalServerParser;
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
public class DalServerParserTest 
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
		String s1 = "Sun Jan 2 01:01:43 PST 2011  { call a_package.a_method(?,?,?) } - millis = 100 elapsed  Params: {1=-1, 2=12, 3=15} Server= foo-dal-bar.com, Session= 12345 RequestID: ohio_123 ";
		
		BaseFieldParserImpl parser = new DalServerParser();
		parser.init();
		
		testMatch(s1, parser, 1, SolrField.DAL_SERVER, "foo-dal-bar.com");
		
		
		
	}
	
	private void testMatch(String s, BaseFieldParserImpl parser
			, int numMatches, String fieldName, String value)
	{
		ParserResult result = parser.match(s, LogType.wsapi);
		
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
