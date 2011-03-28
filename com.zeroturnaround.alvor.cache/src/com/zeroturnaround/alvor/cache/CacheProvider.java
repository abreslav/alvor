package com.zeroturnaround.alvor.cache;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;

public class CacheProvider {
	private static Cache INSTANCE = null;
	private static boolean USE_H2 = false;
	
	private final static ILog LOG = Logs.getLog(CacheProvider.class);
	
	public static Cache getCache() {
		if (INSTANCE == null) {
			try {
				Connection conn;
				if (USE_H2) {
					conn = CacheProvider.connectToH2();
				}
				else {
					conn = CacheProvider.connectToHSQLDB();
				}
				DatabaseHelper db = new DatabaseHelper(conn);
				CacheProvider.checkCreateTables(db);
				INSTANCE = new Cache(db); 
			} catch (SQLException e) {
				LOG.exception(e);
				throw new RuntimeException(e);
			} catch (ClassNotFoundException e) {
				LOG.exception(e);
				throw new RuntimeException(e);
			}
		}
		
		return INSTANCE;
	}
	
	private static Connection connectToHSQLDB() throws SQLException, ClassNotFoundException {
		Class.forName("org.hsqldb.jdbc.JDBCDriver");
		String path = AlvorCachePlugin.getDefault().getStateLocation().append("/cache_hsqldb").toPortableString();
		String fileUrl = "jdbc:hsqldb:file:" + path + ";shutdown=true";
		String serverUrl = "jdbc:hsqldb:hsql://localhost/xdb";
		
		// if db is locked, then assume that server is running and connect in server mode (this is debugging mode)
		if (new File(path + ".lck").exists()) {
			try {
				return DriverManager.getConnection(serverUrl, "SA", "");
			} catch (SQLException e) {
				// Seems that server is not running after all, probably the lock is leftover from a crash
				// HSQL is now supposed to do some repair on connect
				return DriverManager.getConnection(fileUrl, "SA", "");
			}
		}
		else {
			return DriverManager.getConnection(fileUrl, "SA", "");
		}
	}
	
	private static Connection connectToH2() throws SQLException, ClassNotFoundException {
		System.setProperty("h2.serverCachedObjects", "20000");
		Class.forName("org.h2.Driver");
		String path = AlvorCachePlugin.getDefault().getStateLocation().append("/cache_h2").toPortableString();
		String url = "jdbc:h2:" + path + ";CACHE_SIZE=500000";
		return DriverManager.getConnection(url, "SA", "");
	}
	
//	private static Connection connectToH2_old() throws SQLException, ClassNotFoundException {
//		System.setProperty("h2.serverCachedObjects", "20000");
//		Class.forName("org.h2.Driver");
//		String path = AlvorCachePlugin.getDefault().getStateLocation().append("/cache_h2").toPortableString();
//		String fileUrl = "jdbc:h2:" + path + ";TRACE_LEVEL_FILE=3";
//		String serverUrl = "jdbc:h2:tcp://localhost/" + path;
//		
//		// if db is locked, then assume that server is running and connect in server mode (this is debugging mode)
//		if (new File(path + ".lock.db").exists()) {
//			try {
//				return DriverManager.getConnection(serverUrl, "SA", "");
//			} catch (SQLException e) {
//				// Seems that server is not running after all, probably the lock is leftover from a crash
//				return DriverManager.getConnection(fileUrl, "SA", "");
//			}
//		}
//		else {
//			return DriverManager.getConnection(fileUrl, "SA", "");
//		}
//	}
	
	private static void checkCreateTables(DatabaseHelper db) throws SQLException {
		ResultSet res = db.getConnection().getMetaData().getTables(null, null, "FILES", null);
		if (!res.next()) {
			String scriptName = "db/cache_setup.sql";
			if (USE_H2) {
				scriptName = "db/cache_setup_h2.sql";
			}
			InputStream script = CacheProvider.class.getClassLoader().getResourceAsStream(scriptName);
			db.runScript(script);
		}
	}
	
	public static void shutdownCache() {
		if (INSTANCE != null) {
			INSTANCE.shutdown();
		}
	}
}
