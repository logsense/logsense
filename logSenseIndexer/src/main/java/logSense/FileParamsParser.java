package logSense;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import logSense.FileParams.LogType;

/**
 * 
 * @author am
 *
 */
public class FileParamsParser extends BaseFileParamsParser
{
	public static FileParamsParser o = new FileParamsParser();
	
	protected FileParamsParser() {}

	public FileParams getFileParams(Arguments arguments, String fileName) 
	{
		String ownName = fileName;
		File file = null;

		// remove path info from file name before parsing
		if (CommonUtil.o.isEmpty(ownName))
		{
			file = new File(arguments.fileName);
			ownName = file.getName();
		}
		
		if (CommonUtil.o.isEmpty(ownName))
		{
			// error
			logger.error("empty fileName");
			return null;
		}
		
		// use values from arguments if they are provided. else, use data from file name
		FileParams params = new FileParams(arguments);
		if (file != null)
			params.file = file;

		// // jetty.log.ohio-mcssl27.jefferson.com
		
		String regex = "tomcat\\-0\\.log\\.(\\w+)\\-([A-Za-z]+)(\\d+)([^\\.]*)\\.jefferson";

		Pattern p = Pattern.compile(regex);
		Matcher matcher = p.matcher(ownName);
		while (matcher.find()) 
		{
			// skip group 0,that's the outside group. start with group 1
			for (int groupNum = 1; groupNum <= matcher.groupCount(); groupNum++) 
			{
				String matched = matcher.group(groupNum);

				if (!CommonUtil.o.isEmpty(matched))
				{
					if (groupNum == 1) 
					{
						if (params.dataCenter == null)
							params.dataCenter = matched;
					}

					else if (groupNum == 2) 
					{
						if (matched.toLowerCase().endsWith("ssl"))
						{
							if (params.ssl == null)
								params.ssl = Boolean.TRUE;
							
						}
						if (matched.toLowerCase().contains("wwwssl"))
						{
							params.logType = LogType.tomcat_wwwssl;
						}
						else if (matched.toLowerCase().contains("www"))
						{
							params.logType = LogType.tomcat_www;
						}
						if (matched.toLowerCase().contains("api"))
						{
							params.logType = LogType.wsapi;
						}
						if (matched.toLowerCase().contains("avo"))
						{
							params.logType = LogType.avocado;
						}
						
						// if (params.website == null)
						params.website = matched;
						
					}

					else  if (groupNum == 3)  
					{
						if (params.machineNum == null) {
							params.machineNum = CommonUtil.o.parseInt(matched);
						}
					}
					else // groupNum == 4.
					{
						if (params.env == null) {
							params.env = matched;
						}
					}
					
				}

			}
		} // while matcher.find()
		
		if (params.website == null)
			params.website = params.ssl != null && params.ssl.booleanValue() ? "wwwssl" : "na";
		
		if (params.machineNum == null || CommonUtil.o.isEmpty(params.dataCenter))
        {
        	logger.info("empty machineNum and dataCenter. fileName=" + ownName + "  , proceeding with data available ");
        	
        }
		
		params.fileName = ownName;
		
		return params;

	}

}
