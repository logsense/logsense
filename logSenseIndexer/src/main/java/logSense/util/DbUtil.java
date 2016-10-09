/**
 * 
 */
package logSense.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

/**
 * 
 *  @author amukherjee
 *
 */
public class DbUtil
{
	private static Logger logger = Logger.getLogger(DbUtil.class);
	public static DbUtil o = new DbUtil();
	protected DbUtil() {}
	
	public void close(Statement stmt) {
        try {
            if (stmt != null)
                stmt.close();
        } catch (SQLException e) {}
    }
    
    public void close(Connection con) {
        try {
            if (con != null)
                con.close();
        } catch (SQLException e) {}
    }

    public void close(ResultSet rs) {
        try {
            if (rs != null)
                rs.close();
        } catch (SQLException e) {}
    }

    public void close(Statement stmt, Connection con) {
        close(stmt);
        close(con);
    }

    public void close(ResultSet rs, Statement stmt, Connection con) {
        close(rs);
        close(stmt);
        close(con);
    }
    
    public void close(ResultSet rs, Statement stmt) {
        close(rs);
        close(stmt);
    }
    
    public void rollback(Connection con) {
        try {
            if (con != null) {
            	con.rollback();
            }
        } catch (SQLException e) {}
    }
    
    /**
     * Returns a new  connection
     * 
     */
    
    public Connection getConnection(String url, String userName, String password)
	{
		try {
			Connection c = 
			    DriverManager.getConnection(url, userName, password);
			c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			return c;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}
    
    public String getUrlFromHostName(String hostName, String dbName)
    {
    	/*
    	 * e.g.,
    	 * "jdbc:mysql://logsense.yourcompany.com:3306/yourDb"
    	 * 
    	 * 3306 is the default mysql port
    	 * you can change that or make it a variable if necessary.
    	 */
    	return "jdbc:mysql://" + hostName.trim() + ":3306/" + dbName.trim();
    }
	

}
