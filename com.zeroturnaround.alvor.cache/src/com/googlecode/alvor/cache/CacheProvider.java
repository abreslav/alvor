package com.googlecode.alvor.cache;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;

public class CacheProvider {
	private static final Map<String, Cache> caches = new HashMap<String, Cache>();
	private static final boolean USE_SERVER = false; 
	
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
	
	
	public static void tryDeleteCache(String projectName) {
		if (!USE_SERVER) {
			deleteCache(projectName);
		}
		else {
			getCache(projectName).clearProject();
		}
	}
	
	public static void deleteCache(String projectName) {
		// deletes and recreates database tables
		// can be useful when database gets corrupted
		
		if (USE_SERVER) {
			throw new IllegalStateException("Database can't be rebuilt in server mode");
		}
		
		if (caches.get(projectName) != null) {
			Cache cache = getCache(projectName);
			cache.shutdown();
		}
		
		File h2 = new File(getBasePath(projectName) + ".h2.db");
		File trace = new File(getBasePath(projectName) + ".trace.db");
		
		boolean success;
		if (h2.exists()) {
			success = h2.delete();
			assert success;
		}
		if (trace.exists()) {
			success = trace.delete();
			assert success;
		}
		
		caches.remove(projectName);
	}
	
	private static String getBasePath(String projectName) {
		return AlvorCachePlugin.getDefault().getStateLocation().append(projectName).toPortableString();
	}
	
	private static Connection connect(String projectName) throws SQLException, ClassNotFoundException {
		System.setProperty("h2.serverCachedObjects", "1000"); 
		Class.forName("org.h2.Driver");
		String url;
		
		if (USE_SERVER) {
			url = "jdbc:h2:tcp://localhost/" + getBasePath(projectName);
		}
		else {
			url = "jdbc:h2:" + getBasePath(projectName) + ";LOG=0;CACHE_SIZE=25536;LOCK_MODE=0;UNDO_LOG=0";
		}
		return DriverManager.getConnection(url, "SA", "");
	}
	
	private static void checkCreateTables(DatabaseHelper db) throws SQLException {
		if (shouldRecreateDatabase(db.getConnection())) {
			String scriptName = "db/cache_setup.sql";
			InputStream script = CacheProvider.class.getClassLoader().getResourceAsStream(scriptName);
			db.runScript(script);
		}
	}
	
	private static boolean shouldRecreateDatabase(Connection conn) throws SQLException {
		ResultSet rs1 = conn.getMetaData().getTables(null, null, "FILES", null);
		if (!rs1.next()) {
			return true;
		}
		
		// version check:
		PreparedStatement stmt = null;
		ResultSet rs2 = null;
		try {
			stmt = conn.prepareStatement("select * from hotspots");
			rs2 = stmt.executeQuery();
			try {
				rs2.findColumn("conn_descriptor"); // a new field
				return false;
			}
			catch (SQLException e) { // if conn_descriptor doesn't exist
				return true;
			}
		}			
		finally {
			if (stmt != null) {
				stmt.close();
			}
			if (rs1 != null) {
				rs1.close();
			}
			if (rs2 != null) {
				rs2.close();
			}
		}
	}
	
	public static void shutdownCaches() {
		for (Cache cache : caches.values()) {
			cache.shutdown();
		}
	}
}
