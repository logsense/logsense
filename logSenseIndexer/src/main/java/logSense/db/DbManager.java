/**
 * 
 */
package logSense.db;

import java.sql.Connection;

import logSense.util.DbUtil;

import org.apache.log4j.Logger;

/**
 * @author am
 *
 */
public class DbManager  
{
	public static Logger logger = Logger.getLogger(DbManager.class);
	
	public static DbManager o = new DbManager();
	
	private String url;
	private String userName;
	private String password;
	String defaultUrl = "jdbc:mysql://localhost:3306/sessionCrossRef"; // TODO get this from a config file
	
	private DbManager() {}
	
	public void initialize(String userName, String password)
	{
		// jdbc:mysql://[host][,failoverhost...][:port]/[database]...
		initialize(defaultUrl, userName, password);
	}
	
	public void initialize(String url, String userName, String password)
	{
		this.url = url;
		this.userName = userName;
		this.password = password;
	}
	
	/*
	 * return a new connection. In this simple version, there is no connection caching
	 * or pooling.
	 */
	public Connection getConnection()
	{
		return DbUtil.o.getConnection(url, userName, password);
		
	}
	
	
}
