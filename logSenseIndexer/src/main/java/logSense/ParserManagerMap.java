package logSense;

import java.util.HashMap;

import logSense.FileParams.LogType;

/**
 * A Map of ParserManagers. 
 * 
 * some types of logs will use one parser manager, another will
 * use another. Keeping them separate will mean applying fewer
 * regex parsers on a line, speeding things up.
 * 
 * @author am
 *
 */
public class ParserManagerMap extends HashMap 
{

	public static ParserManagerMap o = new ParserManagerMap();
	private ParserManagerMap() {}

	
	boolean initialized;
	
	public synchronized void initialize() 
	{
		if (!initialized)
		{
			// initialize parsers for Tomcat logs (currently www, wwwssl, wsapi, L2, dal)
			ParserManager normalParserMgr = new ParserManager();
			normalParserMgr.register((new CategoryParser()).init());
			normalParserMgr.register((new StoredProcParser()).init());
			normalParserMgr.register((new ClassMethodParser()).init());
			normalParserMgr.register((new SessionIDParserWrapper()).init());
			normalParserMgr.register((new RequestIDParser()).init());
			normalParserMgr.register((new ImpParser()).init());
			normalParserMgr.register((new MsecParser()).init());
			normalParserMgr.register((new DalServerParser()).init());
			normalParserMgr.register((new ExceptionParser()).init());
			normalParserMgr.register((new DalPoolParser()).init());
			
			this.put(LogType.tomcat_wwwssl, normalParserMgr);
			this.put(LogType.tomcat_www, normalParserMgr);
			this.put(LogType.wsapi, normalParserMgr);
			this.put(LogType.avocado, normalParserMgr);
			this.put(LogType.backend1, normalParserMgr);
			this.put(LogType.L2, normalParserMgr);
			
			
			// initialize parser for Access logs
			ParserManager accessLogParserMgr = new ParserManager();
			accessLogParserMgr.register((new AccessLogParser()).init());
			this.put(LogType.access_wwwssl, accessLogParserMgr);
			this.put(LogType.access_www, accessLogParserMgr);
			this.put(LogType.access_wsapi, accessLogParserMgr);
			this.put(LogType.access_avocado, accessLogParserMgr);
			
			
			
			
			// add others as needed, e.g., TaxNameParser, TaxRateParser, TaxAmountParser, etc
			
			
			// init date parsers 
			
			defaultDateParser = (new DateParser()).init(); 
			

		}
		
		initialized = true;
	}
	
	// date parsers; will use a Map when this gets larger. for smaller numbers, this is faster
	FieldParser atgDateParser;
	FieldParser defaultDateParser;
	
	public FieldParser getDateParser(LogType logType)
	{
		if (logType == LogType.atg)
			return atgDateParser;
		else
			return defaultDateParser;
	}
}
