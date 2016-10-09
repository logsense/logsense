/**
 * 
 */
package logSense;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.log4j.Logger;

/**
 * @author am
 *
 */
public class CommonUtil 
{
	private static Logger logger = Logger.getLogger(CommonUtil.class);
	public static CommonUtil o = new CommonUtil();
	private CommonUtil() {}

	public boolean isEmpty(String s)
	{
		if (s == null || s.trim().length() == 0)
			return true;
		else
			return false;
	}
	
	public boolean isEmpty(Collection c)
	{
		if (c == null || c.isEmpty())
			return true;
		else
			return false;
	}
	
	/*
	 * logs will be available as .gz files
	 * 
	 * This method will return a BufferedReader for a gzipped input file
	 */
	public BufferedReader createReaderForGz(String gzFileName) throws IOException
	{
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(gzFileName));
		GzipCompressorInputStream gzIn = new GzipCompressorInputStream(in);
		BufferedReader rd = new BufferedReader(new InputStreamReader(gzIn));
		
		/*
		 * test
		String line = null;
		int lineNum = 0;
		while ( (line = rd.readLine()) != null) 
		{
			System.out.println(lineNum++ + " : " + line);
		}
		*/
		
		return rd;

	}
	
	public void sleepNoException(long millis)
	{
		try
		{
			Thread.sleep(millis);
		} catch (InterruptedException e) {}
	}
	
	public Boolean parseBoolean (String s)
	{
		Boolean result = null;
		try {
			result = new Boolean (Boolean.parseBoolean(s));
		} catch (NumberFormatException e) {
			result = null;
		}
		
		return result;
	}
	
	public Integer parseInt (String s)
	{
		Integer result = null;
		try {
			result = new Integer(Integer.parseInt(s));
		} catch (NumberFormatException e) {
			result = null;
		}
		
		return result;
	}
	
	public Long parseLong (String s)
	{
		Long result = null;
		try {
			result = new Long(Long.parseLong(s));
		} catch (NumberFormatException e) {
			result = null;
		}
		
		return result;
	}
	
	public Double parseDouble (String s)
	{
		Double result = null;
		try {
			result = new Double(Double.parseDouble(s));
		} catch (NumberFormatException e) {
			result = null;
		}
		
		return result;
	}
	
	public boolean isNumeric(String s)
	{
		boolean returnVal = false;
		try
		{
			Double.parseDouble(s);
			returnVal = true;
		} catch (NumberFormatException e) {
			// returnVal is false
		}
		
		return returnVal;
	}
	
	/*
	 * return all files in a dir. ignore sub directories
	 */
	public List<File> getFiles(String dir)
	{
		List<File> result = null;
		File folder = new File(dir);
		if (folder.exists() && folder.isDirectory())
		{
			File[] listOfFiles = folder.listFiles(); 
			  
			result = new ArrayList<File>();
			for (File file : listOfFiles)
			{
				if (file.isFile())
					result.add(file);
			}
		}
		else
		{
			logger.error("check to make sure that " + dir + " exists and you have permission to read its contents");
		}
		
		return result;
	}
	
	public void showStackTrace(String message)
    {
    	try {
    		throw new RuntimeException(message);
    	}
    	catch (Exception e) {
    		logger.error(e.getMessage(), e);
    	}
    }
}
