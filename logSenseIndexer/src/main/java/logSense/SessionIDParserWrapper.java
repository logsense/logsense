package logSense;

import java.util.List;

import logSense.FileParams.LogType;
import logSense.ParserResult.NameValue;
import logSense.SolrConstants.SolrField;

import org.apache.commons.collections.map.LRUMap;

/**
 * SessionIDParserWrapper: for regular logs, call SessionIDParser, and that's all.
 * for a different backend server, call SessionIDParser and SessionParser, and keep mapping between
 * them in a map.
 * 
 * The map is a ThreadLocal (in case we have multiple threads indexing data in parallel later).
 * 
 * 
 * 
 * @author am
 *
 */
public class SessionIDParserWrapper extends BaseFieldParserImpl
{
	
	
	BaseFieldParserImpl sessionIdParser;
	BaseFieldParserImpl sessionParser;
	
	/*
	 * map between Session in backend (a Long) vs a SessionID in Tomcat
	 * 
	 * TODO later, if running concurrent indexers, have this as a ThreadLocal.
	 * 
	 * With or without ThreadLocal, clear it out after a backend log file is processed... in the interests of space. if data loss becomes
	 * an issue (when one backend log ends and another starts),  can think of caching mechanisms across logs. perhaps keep some, delete others
	 * 
	 */
	LRUMap backendSession2TomcatSessionId = new LRUMap(100000, 0.75f);

	public SessionIDParserWrapper()
	{
		super("sessionId-wrapper");
	} 
	public BaseFieldParserImpl init()
	{
		
		sessionIdParser = (new SessionIDParser()).init();
		sessionParser = (new SessionParser()).init();
		
		// multiValue is false
		multiValue = true;
		
		return this;
		
	}
	
	/*
	 * we may need to join data across a requestId or across a sessionId
	 * for join to work, this data must reside on the same shard.
	 * so we will route all data for a given session to the same shard.
	 * so far, all benches with a requestId also have a sessionId, so joins for
	 * requestId will also work.
	*/
	public ParserResult match(String record, LogType logType)
	{
		ParserResult p = match2(record, logType);
		if (p != null && p.hasMatch())
		{
			List<NameValue> nvPairs = p.nvPairs;
			for (NameValue nv : nvPairs)
			{
				if (nv.name.equalsIgnoreCase(SolrField.SESSION_ID))
				{
					p.shardRouteKey = nv.value;
				}
			}
		}
		
		return p;
	}
	
	
	/*
	 * if this is NOT a backend log, just let SessionID handle it.
	 * 
	 * else // logType is backend server
	 * 	look for SessionID and Session.
	 * 
	 * 	if SessionID is a string, and Session is non-null: store the mapping Session -> SessionID
	 * 	else if SessionID is number (no alpha chars), get the mapping from Session. if no mapping available, skip SessionID
	 *  
	 * remove the matched strings from the record
	 * 
	 */
	private ParserResult match2(String record, LogType logType)
	{
		
		if (CommonUtil.o.isEmpty(record))
			return null;
		
		String record2Use = record;
		
		if (logType != LogType.backend1)
		{
			return matchSessionIdOnly(record2Use, logType);
		}
		
		// else -- logType == backend1
		
		ParserResult sessionIdMatch = sessionIdParser.match(record2Use, logType);
		ParserResult backendSessionMatch = null;
		
		if (sessionIdMatch != null && sessionIdMatch.hasMatch())
		{
			NameValue tomcatSessionNVPair = ((NameValue) sessionIdMatch.nvPairs.get(0));
			String tomcatSessionId = tomcatSessionNVPair.value;
			
			backendSessionMatch = sessionParser.match(sessionIdMatch.remainder, logType);
			
			if (backendSessionMatch != null && backendSessionMatch.hasMatch())
			{
				/*
				 * if SessionID has alpha chars then save the mapping sessionNumeric -> sessionAlphaNumeric in threadlocal map
				 * else if SessionId is all numeric, replace it with data from Map. if no mapping exists, delete it from sessionIdParser.nvPairs
				 * add backendSessionMatch.nvPairs to sessionIdMatch.nvPairs
				 */
				
				// nvPair has data, because hasMatch() is true

				NameValue backendSessionNVPair = ((NameValue) backendSessionMatch.nvPairs.get(0));
				String backendSessionAsStr = backendSessionNVPair.value; int xyz=2;
				Double backendSessionAsNumber = CommonUtil.o.parseDouble(backendSessionAsStr);
				
				// if tomcat sessionId has alpha chars, we are done. save the mapping between backend session and tomcat sessionId
				if (!CommonUtil.o.isNumeric(tomcatSessionId))
				{
					if (backendSessionAsNumber != null)
						backendSession2TomcatSessionId.put(backendSessionAsNumber, tomcatSessionId);
				}
				
				else // numeric tomcat session. see if we have a mapping in backendSession2TomcatSessionId
				{
					// remove the current match from sessionIdMatch. we don't want a numeric tomcatSessionId 
					// (artifact of the backend server)
					sessionIdMatch.nvPairs.clear();
				
					if (backendSessionAsNumber != null)
					{
						String prevMatchedTomcatSessionId = (String) backendSession2TomcatSessionId.get(backendSessionAsNumber);
						
						if (!CommonUtil.o.isEmpty(prevMatchedTomcatSessionId))
						{
							// we have found the sessionId for this backend session
							// we can create a new NameValue, reusing the prev one is ugly, but saves a new object creation
							tomcatSessionNVPair.value = prevMatchedTomcatSessionId;
							sessionIdMatch.nvPairs.add(tomcatSessionNVPair);
							// sessionIdMatch.remainder += " AppServer SessionID= " + prevMatchedTomcatSessionId;
							// this will be appended to record in the Solr index. It's for information purposes only
							// sessionIdMatch.nvPairs.add(new NameValue(SolrField.APP_SERVER_SESSION_ID,  tomcatSessionId ));
							sessionIdMatch.addDiscovered(" AppServer SessionID= " + prevMatchedTomcatSessionId );
						}
					}
				
				}
				
				// add the backend session NameValue pair to sessionIdMatch
				sessionIdMatch.nvPairs.add(backendSessionNVPair);
				
			} // if backendSessionMatch has match
			else // there is a case when a backend session is not present, but a tomcat session id has the numeric value (same as the backend session id)
			{
				if (CommonUtil.o.isNumeric(tomcatSessionId))
				{
					// remove the current match from sessionIdMatch. we don't want a numeric tomcatSessionId 
					sessionIdMatch.nvPairs.clear();
					
					// tomcatSessionId is really the backend session id
					String prevMatchedTomcatSessionId = (String) backendSession2TomcatSessionId.get(CommonUtil.o.parseDouble(tomcatSessionId));
					if (!CommonUtil.o.isEmpty(prevMatchedTomcatSessionId))
					{
						// we have found the sessionId for this backend session
						tomcatSessionNVPair.value = prevMatchedTomcatSessionId;
						sessionIdMatch.nvPairs.add(tomcatSessionNVPair);
						// sessionIdMatch.remainder += " AppServer SessionID= " + prevMatchedTomcatSessionId;
						// this will be appended to record in the Solr index. It's for information purposes only
						// sessionIdMatch.nvPairs.add(new NameValue(SolrField.APP_SERVER_SESSION_ID,  " AppServer SessionID= " + tomcatSessionId ));
						sessionIdMatch.addDiscovered(" AppServer SessionID= " + prevMatchedTomcatSessionId );

					}
					// add tomcatSessionId as the backend session id (which it really is)
					sessionIdMatch.nvPairs.add(new NameValue(SolrField.SESSION,tomcatSessionId ));
				}
			}
			
		} // if sessionIdMatch has match
		
		else
		{
			
			// run backend session test on record2Use (unlike before, where we were using the remainder)
			backendSessionMatch = sessionParser.match(record2Use, logType);
			
			// we will return sessionIdMatch, so set it to backendSessionMatch
			sessionIdMatch = backendSessionMatch;
		}
		
		return sessionIdMatch;
	}
	
	private ParserResult matchSessionIdOnly(String record, LogType logType)
	{
		return sessionIdParser.match(record, logType);
	}
	
	
	
	public void reset()
	{
		// since we have an LRUMap now, no need to clear it
	}
}
