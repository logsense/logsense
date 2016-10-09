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
public class DalFileParamsParser extends BaseFileParamsParser
{
	public static DalFileParamsParser o = new DalFileParamsParser();
	
	protected DalFileParamsParser() {}

	public FileParams getFileParams(Arguments arguments, String fileName) 
	{
		String ownName = fileName;
		File file = null;

		// remove path info from file name before parsing
		if (CommonUtil.o.isEmpty(ownName))
		{
			file= new File(arguments.fileName);
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

		/*
		 * file name will be something like this:
		 * backend-0.log.ohio-v2-backend100.yourcompany.com.1234.gz
		 */
		
		String regex = "backend\\-0\\.log\\.((\\w+)\\-[\\w\\-]+)\\.";

		Pattern p = Pattern.compile(regex);
		Matcher matcher = p.matcher(ownName);
		while (matcher.find()) 
		{
			params.logType = LogType.backend1;
			
			// skip group 0,that's the outside group. start with group 1
			for (int groupNum = 1; groupNum <= matcher.groupCount(); groupNum++) 
			{
				String matched = matcher.group(groupNum);

				if (!CommonUtil.o.isEmpty(matched))
				{
					if (groupNum == 1) 
					{
						if (params.backendServer == null)
							params.backendServer = matched;
					}

					else if (groupNum == 2) 
					{
						if (params.dataCenter == null)
							params.dataCenter = matched;
						
					}

					
				}

			}
		} // while matcher.find()
		
		
		
		params.fileName = ownName;
		
		return params;

	}

}
