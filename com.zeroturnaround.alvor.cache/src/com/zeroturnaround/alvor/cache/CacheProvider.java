package com.zeroturnaround.alvor.cache;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;

public class CacheProvider {
	private static Map<String, Cache> caches = new HashMap<String, Cache>();
	private static boolean USE_SERVER = true; 
	
	private final static ILog LOG = Logs.getLog(CacheProvider.class);
	
	public static Cache getCache(String projectName) {
		Cache cache = caches.get(projectName);
		if (cache == null) {
			try {
				Connection conn;
				conn = CacheProvider.connect(projectName);
				DatabaseHelper db = new DatabaseHelper(conn);
				CacheProvider.checkCreateTables(db);
				cache = new Cache(db, projectName); 
				caches.put(projectName, cache);
			} catch (SQLException e) {
				LOG.exception(e);
				throw new RuntimeException(e);
			} catch (ClassNotFoundException e) {
				LOG.exception(e);
				throw new RuntimeException(e);
			}
		}
		
		return cache;
	}
	
//	private static Connection connectToHSQLDB() throws SQLException, ClassNotFoundException {
//		Class.forName("org.hsqldb.jdbc.JDBCDriver");
//		String path = AlvorCachePlugin.getDefault().getStateLocation().append("/cache_hsqldb").toPortableString();
//		String fileUrl = "jdbc:hsqldb:file:" + path + ";shutdown=true;hsqldb.log_data=false;hsqldb.default_table_type=cached";
//		
//		String serverUrl = "jdbc:hsqldb:hsql://localhost/xdb";
//		
//		// if db is locked, then assume that server is running and connect in server mode (this is debugging mode)
//		if (new File(path + ".lck").exists()) {
//			try {
//				return DriverManager.getConnection(serverUrl, "SA", "");
//			} catch (SQLException e) {
//				// Seems that server is not running after all, probably the lock is leftover from a crash
//				// HSQL is now supposed to do some repair on connect
//				return DriverManager.getConnection(fileUrl, "SA", "");
//			}
//		}
//		else {
//			return DriverManager.getConnection(fileUrl, "SA", "");
//		}
//	}
	
	private static Connection connect(String projectName) throws SQLException, ClassNotFoundException {
		System.setProperty("h2.serverCachedObjects", "1000"); 
		Class.forName("org.h2.Driver");
		String path = AlvorCachePlugin.getDefault().getStateLocation().append(projectName).toPortableString();
		String url;
		
		if (USE_SERVER) {
			url = "jdbc:h2:tcp://localhost/" + path;
		}
		else {
			url = "jdbc:h2:" + path + ";LOG=0;CACHE_SIZE=25536;LOCK_MODE=0;UNDO_LOG=0";
		}
		return DriverManager.getConnection(url, "SA", "");
	}
	
	private static void checkCreateTables(DatabaseHelper db) throws SQLException {
		ResultSet res = db.getConnection().getMetaData().getTables(null, null, "FILES", null);
		if (!res.next()) {
			String scriptName = "db/cache_setup.sql";
			InputStream script = CacheProvider.class.getClassLoader().getResourceAsStream(scriptName);
			db.runScript(script);
		}
	}
	
	public static void shutdownCaches() {
		for (Cache cache : caches.values()) {
			cache.shutdown();
		}
	}
}
