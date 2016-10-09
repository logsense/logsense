/**
 * 
 */
package logSense.db;

import java.sql.Connection;
import java.sql.PreparedStatement;

import logSense.CommonUtil;
import logSense.util.DbUtil;

import org.apache.log4j.Logger;

/**
 * @author am
 *
 */
public class FileDao  
{
	public static Logger logger = Logger.getLogger(FileDao.class);
	
	public static FileDao o = new FileDao();

	
	
	public int updateEndIndexTime(String fileName) 
	{
		if (CommonUtil.o.isEmpty(fileName))
		{
			return -1;
		}
		int count = 0;
		Connection con = null;
		PreparedStatement pstmt = null;
		try
		{
			/*
			 * update end_index_time
			 */
			String sql = "update file set end_index_time=now() where file_name=? " ;
			
			con = DbManager.o.getConnection();
			pstmt = con.prepareStatement(sql);
			
			con.setAutoCommit(false);
	
			int i=1;
			pstmt.setString(i++, fileName.trim());
			
			count = pstmt.executeUpdate();
			
			if (count > 0) 
				con.commit();
			
		
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			DbUtil.o.rollback(con);
			count = -1;
		} finally {
			DbUtil.o.close(pstmt, con);
		}
		
		return count;
	}
	
	
}
