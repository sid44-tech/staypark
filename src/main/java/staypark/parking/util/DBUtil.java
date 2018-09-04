package staypark.parking.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.mysql.cj.core.conf.PropertyDefinitions;
import com.mysql.cj.jdbc.MysqlDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DBUtil {
	//TEST
//	private final static String USER_NAME = "";
//	private final static String PASSWORD = "";
//	private final static String DATABASE_URL = "";

	//LIVE
	private final static String USER_NAME = "";
	private final static String PASSWORD = "";
	private final static String DATABASE_URL = "";
	
	public static Connection getConnection() {
		Connection conn = null;
		MysqlDataSource dataSource = new MysqlDataSource();
		dataSource.setUser(USER_NAME);
		dataSource.setPassword(PASSWORD);
		dataSource.setServerName(DATABASE_URL);
		dataSource.getModifiableProperty(PropertyDefinitions.PNAME_serverTimezone).setValue("US/Central");

		try {
			conn = dataSource.getConnection();
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}
		
		return conn;
	}
	
	public static void closeResources(final Statement s) {
		if (s == null) {
			return;
		}
		try {
			s.close();
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public static void closeResources(final ResultSet rs) {
		if (rs == null) {
			return;
		}
		try {
			rs.close();
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public static void closeResources(final Connection conn) {
		if (conn == null) {
			return;
		}
		try {
			conn.close();
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public static void closeResources(final Connection conn, final Statement s, final ResultSet rs) {
		closeResources(rs);
		closeResources(s);
		closeResources(conn);
	}
	
	public static void rollback(final Connection conn)  {
		try {
			conn.rollback();
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public static void setAutoCommit(final Connection conn, final boolean autoCommit) {
		try {
			conn.setAutoCommit(autoCommit);
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}
	}
}
