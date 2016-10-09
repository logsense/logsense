package logSense;

import logSense.FileParams.LogType;


/**
 * FieldParser looks for one field such as category
 * 
 * @author am
 *
 */
public interface FieldParser 
{
	ParserResult match(String record, LogType logType);
	String getFieldName();
	void reset();
}