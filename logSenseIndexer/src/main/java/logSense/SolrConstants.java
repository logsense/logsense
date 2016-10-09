/**
 * 
 */
package logSense;

/**
 * @author am
 *
 */
public class SolrConstants
{

	/*
	 * these fields are also defined in Solr's schema.xml
	 */
	public static class SolrField
	{
		public static final String ID = "id";
		
		/*
			we split ndc-wwwsssl25 into the tuples (ndc, www, ssl, 25) 
	   		i.e., (dataCenter, app, ssl, machineNum) == (ndc, www, ssl, 25) 
	   		the original is kept in the field machine. it needs to be text_en_splitting_tight 
	   		so the entire phrase stays together
		 */
		public static final String MACHINE = "machine";
		
		// dataCenter is ndc etc
		public static final String DATA_CENTER = "dataCenter";
		
		// env is E4, PR, etc.
		public static final String ENV = "env";
		
		
		// app is www etc
		public static final String APP = "app";
		
		// ssl is a boolean field in Solr. add a boolean value for it
		public static final String SSL = "ssl"; 
		
		// the machine number
		public static final String MACHINE_NUM = "machineNum";
		
		/*
		 * category
		  	e.g., 
		  			TOMCAT REQ or TOMCAT BENCH or DAL SERVER BENCH or 
		  			WEB.WWW.SEVERE.30000 or DB PROXY etc 
   			the words parts are treated separately, i.e., wi fi will match wi-fi
		 */
		public static final String CAT = "cat";
		
		/*
		 * non-tokenized version of category to make it easier to sort or group
		 * results by category.  copied from "cat" via copyField
		 */
		public static final String CAT_EXACT = "catExact";
		
		/*
		 * DAL BENCH -> DAL PROXY
		 */
		public static final String SUB_CAT = "sub_cat";
		public static final String SUB_CAT_EXACT = "subCatExact";
		
		// for certain records such as tomcat bench, amount of time in milliseconds is logged.
		public static final String MSEC = "msec";
		
		public static final String LINE_NUM = "lineNum";
		public static final String RECORD_NUM = "recordNum";
		public static final String FILE_NAME = "fileName";
		public static final String LOG_TYPE = "logType"; // tomcat, api, rtcc etc
		
		public static final String STACK_TRACE = "stackTrace";
		public static final String EXCEPTION = "exception";
		
		// unique pattern for a set of exceptions. e.g., NullPointerException may come from several places in the code. Each is marked 
		// with a string pattern that is unique to it. will help remove duplicates
		public static final String EXCEPTION_CLASS = "exceptionClass";
		
		public static final String CLASS = "class";
		
		/*
		 * non-tokenized version of class to make it easier to sort or group
		 * results by class.  copied from "class" via copyField
		 */
		public static final String CLASS_EXACT = "classExact";
		
		public static final String METHOD = "method";
		public static final String URL = "url";
		
		public static final String FACET_URL = "facetUrl";
		public static final String IMP = "imp";
		
		public static final String RECORD = "record";
		public static final String DATE = "date";
		
		public static final String SESSION_ID = "sessionId";
		public static final String REQUEST_ID = "requestId";
		
		public static final String PARENT_ID = "parentId";
		public static final String CHILD_ID = "childId";
		
		public static final String SESSION = "session"; // DAL session
		public static final String APP_SERVER_SESSION_ID = "appServerSessionId";
		
		public static final String STORED_PROC = "storedProc";
		public static final String DAL_SERVER = "dalServer";
		public static final String DAL_POOL = "dalPool";
		
		public static final String VERSION = "_version_";
		
		// not used when adding to index. default search field
		// public static final String TEXT = "text";
		
		// num docs to index at a time
		public static final int DEFAULT_INDEXING_BATCH_SIZE =100;
		
	}
	
	public static final int INDEXING_BATCH_SIZE = 10;
	public static final boolean HARD_COMMIT = false;
	public static final int COMMIT_SIZE = 10000;
}
