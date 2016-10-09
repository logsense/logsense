/**
 * 
 */
package logSense;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import logSense.BaseFieldParserImpl;
import logSense.DalPoolParser;
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
public class DalPoolParserTest 
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
		
		String s1 = "2014-02-10 10:10:10:500 - ab-cd-foo, 12345 - Command=Bar, Pool= dalpool_yahoo, uvw";
		String s2 = "2014-02-10 10:10:10:500 - ab-cd-foo, 12345 - Command=Stuff, no pool info";
		
		
		BaseFieldParserImpl parser = new DalPoolParser();
		parser.init();
		
		testMatch(s1, parser, 1, SolrField.DAL_POOL, "yahoo");
		testMatch(s2, parser, 0, SolrField.DAL_POOL, "");
		
		
		
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
