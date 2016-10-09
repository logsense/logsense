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
public class ImpParserTest 
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
		String s1 = "[WARNING][Sun 2011/01/02 15:00:52:885 PST][jefferson.somePackage.SomeClass/aMethod] <MsgText: IMP_FOO > <ThreadID: 7> <SessionID: abcd> <RequestID: oregon_as12>";
		
		String s2 = "[SEVERE][Sun 2011/01/02 15:00:52:885 PST][jefferson.anotherPackage.AnotherClass/aMethod] <MsgText: IMP_BAR 12> <ThreadID: 7> <SessionID: abcd> <RequestID: or_123>";
		String s3 = "[WARNING][Sun 2011/01/02 15:00:52:885 PST][jefferson.package3.Something/method3] <MsgText: REQUEST URI - /foo/cartCmd.do> <ThreadID: 7> <SessionID: abcd> <RequestID: az_123>";
		
		BaseFieldParserImpl parser = new ImpParser();
		parser.init();
		
		String[] expected1 = {"IMP_FOO" };
		String[] expected2 = {"SEVERE", "IMP_BAR" };
		testMatch(s1, parser, 1, SolrField.IMP, expected1 );
		testMatch(s2, parser, 2, SolrField.IMP, expected2);
		testMatch(s3, parser, 0, SolrField.IMP, null);
		
	}
	
	private void testMatch(String s, BaseFieldParserImpl parser
			, int numMatches, String fieldName, String[] values)
	{
		ParserResult result = parser.match(s, LogType.tomcat_wwwssl);
		
		assertNotNull(result);
		
		if (numMatches > 0)
		{
			
			assertEquals(result.nvPairs.size(), numMatches);
			int i=0;
			for (NameValue nv : result.nvPairs)
			{
				assertEquals(nv.name, fieldName);
				assertEquals(nv.value, values[i++]);
			}
			
			
		}
		else
			assertEquals(result.nvPairs == null || result.nvPairs.isEmpty(), true);
		
	}

}
