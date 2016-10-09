/**
 * 
 */
package logSense;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import logSense.AccessLogParser.AccessLogFields;
import logSense.FileParams.LogType;
import logSense.ParserResult.NameValue;

import org.junit.Before;
import org.junit.Test;

/**
 * @author am
 *
 */
public class AccessLogParserTest 
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
		String s1 = "127.0.0.1 [2/Jan/2011:14:29:17 -0800] \"POST /foo/anEndPoint HTTP/1.1\" 200 43 1554 \"-\" \"Java/1.6 " +
				".0_45\" \"-\" \"-\" \"-\" \"-\" \"MOD DEFLATE: result: - in: - out: - ratio: -%\" \"HTTP/1.1 oregon100 (oregon100_123)\"";
		
		
		String s2 = "10.100.100.100 [2/Jan/2011:14:44:34 -0800] \"GET /bar/anotherEndPoint.gsp?report=report HTTP/1.0\" 302 - 1000 \"-\" \"Wget/1.10.2\" \"-\" \"-\" \"-\" \"adminAccess_cookie=\" \"MOD DEFLATE: result: - in: - out: - ratio: -%\" \"HTTP/1.0 az200 (az200_234)\" ";
		
		BaseFieldParserImpl parser = new AccessLogParser();
		parser.init();
		
		testMatch(s1, parser, 6
				, "127.0.0.1"
				, "POST"
				, "/foo/anEndPoint"
				, "200"
				, "43"
				, "1554");
		
		testMatch(s2, parser, 5
				, "10.100.100.100"
				, "GET"
				, "/bar/anotherEndPoint.gsp"
				, "302"
				, null
				, "1000");
		
		
		
	}
	
	private void testMatch(String s, BaseFieldParserImpl parser
			, int numMatches, String ip, String httpMethod, String url, String status, String sizeIn, String sizeOut)
	{
		ParserResult result = parser.match(s, LogType.tomcat_wwwssl);
		
		assertNotNull(result);
		
		if (numMatches > 0)
		{
			assertEquals(result.nvPairs.size(), numMatches);
			
			for (NameValue nv : result.nvPairs)
			{
				if (nv.name.equals(AccessLogFields.ip.name()))
					assertEquals(nv.value, ip);
				
				else if (nv.name.equals(AccessLogFields.httpMethod.name()))
					assertEquals(nv.value, httpMethod);
				
				else if (nv.name.equals(AccessLogFields.url.name()))
					assertEquals(nv.value, url);
				
				else if (nv.name.equals(AccessLogFields.status.name()))
					assertEquals(nv.value, status);
				
				else if (nv.name.equals(AccessLogFields.sizeIn.name()))
					assertEquals(nv.value, sizeIn);
				else if (nv.name.equals(AccessLogFields.sizeOut.name()))
					assertEquals(nv.value, sizeOut);
				
				else
					fail("unknown field name :" + nv.name);
			}
		}
		else
			assertEquals(result.nvPairs == null || result.nvPairs.isEmpty(), true);
		
	}

}
