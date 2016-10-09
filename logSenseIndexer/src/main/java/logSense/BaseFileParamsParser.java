package logSense;

import org.apache.log4j.Logger;

/**
 * 
 * @author am
 *
 */

abstract public class BaseFileParamsParser 
{
	protected Logger logger = Logger.getLogger(BaseFileParamsParser.class);

	protected BaseFileParamsParser() {}

	abstract public FileParams getFileParams(Arguments arguments, String fileName);
	
	// we will add future stuff that may be common to FileParamsParser hierarchy here.

}
