package com.zeroturnaround.alvor.cache;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.zeroturnaround.alvor.common.HotspotPattern;
import com.zeroturnaround.alvor.common.NodeDescriptor;
import com.zeroturnaround.alvor.common.UnsupportedNodeDescriptor;
import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.string.AbstractStringCollection;
import com.zeroturnaround.alvor.string.IAbstractString;

public class Cache {
	
	private final static ILog LOG = Logs.getLog(ICacheService.class);
	private static Cache INSTANCE = null;
	private DatabaseHelper db;
	
	private Cache() {
		try {
			Connection conn = this.connect();
			this.db = new DatabaseHelper(conn);
			this.checkCreateTables();
		} catch (SQLException e) {
			LOG.exception(e);
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			LOG.exception(e);
			throw new RuntimeException(e);
		}
	}
	
	public void removeFile(String fileName) {
		invalidateFile(fileName);
		db.execute("delete from files where name = ?", fileName);
	}
	
	public void addFile(String projectName, String fileName) {
		db.execute("insert into files (name, project_id, batch_no) " +
				" values (?, (select id from projects where name = ?), 0)", 
				fileName, projectName);
	}
	
	public void invalidateFile(String fileName) {
		db.execute("delete from abstract_strings where file_id = " +
				" (select id from files where name = ?)", fileName);
	}
	
	public List<FileRecord> getFilesToUpdate(String projectName) {
		ResultSet rs = db.query (
				" select f.name, f.batch_no" +
				" from files f" +
				" where f.name like ? || '/%'" +
				" and f.batch_no < (select max(batch_no) from project_patterns where project_name = ?)",
				projectName, projectName);
		
		List<FileRecord> records = new ArrayList<FileRecord>();
		try {
			while (rs.next()) {
				records.add(new FileRecord(rs.getString("name"), rs.getInt("batch_no")));
			}
			return records;
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
//	public Collection<NodeDescriptor> getPrimaryHotspotDescriptors(String projectName) {
//		return null;
//	}

	public void setProjectPrimaryPatterns(String projectName,
			Collection<HotspotPattern> patterns) {
		
//		int projectId = getProjectId(projectName);
		
		// FIXME delete only primary patterns. Or clean everything???
		db.execute("delete from project_patterns where project_name = ?", projectName);
		
		for (HotspotPattern pattern : patterns) {
			int patternId = getOrCreatePatternId(pattern);
			db.execute (
					" insert into project_patterns " +
					" (project_name, pattern_id, batch_no, source)" +
					" values (?, ?, ?, ?)", 
					projectName, patternId, 0, "configuration"); 
		}
	}
	
//	private int getProjectId(String projectName) {
//		return db.queryInt("select id from projects where name = ?", projectName);
//	}

	private int getOrCreatePatternId(HotspotPattern pattern) {

		Integer id = db.queryMaybeInteger (
				" select id from hotspot_patterns where" +
				" class_name = ? and method_name = ? and arg_index = ?", 
				pattern.getClassName(), pattern.getMethodName(), pattern.getArgumentIndex());

		if (id != null) {
			return id;
		}
		else {
			return db.insertAndGetId(
					" insert into hotspot_patterns " +
					" (class_name, method_name, arg_index) values (?, ?, ?)",
					pattern.getClassName(), pattern.getMethodName(), pattern.getArgumentIndex());
		}
	}

//	public List<PatternRecord> getProjectPatterns(String projectName) {
//		return null;
//	}

	public List<PatternRecord> getNewProjectPatterns(String projectName) {
		int minFileBatchNo = db.queryInt(
				" select coalesce(min(batch_no),0)" +
				" from files where name like ? || '/%'", projectName);
		
		ResultSet rs = db.query(
				" select p.class_name," +
				" 		 p.method_name," +
				"        p.arg_index," +
				"        pp.batch_no" +
				" from project_patterns pp" +
				" join patterns p on p.id = pp.pattern_id" +
				" where pp.project_name = ?" +
				" and batch_no > ?", 
				projectName, minFileBatchNo);
		
		List<PatternRecord> result = new ArrayList<PatternRecord>();
		try {
			while (rs.next()) {
				result.add(new PatternRecord(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getInt(4)));
			}
			return result;
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public List<IAbstractString> getUncheckedHotspots(String projectName) {
		return null;
	}
	
	public void markHotspotsAsChecked(Collection<IAbstractString> hotspots) {
		
	}
	
	public void addHotspot(PatternRecord pattern, NodeDescriptor desc) {
		// TODO most complex thing
		// desc should be a choice (???)
		// creates a choice with information about pattern
	}
	
	/**
	 * Marks this primary branch as unchecked  
	 */
	private void invalidateRespectivePrimaryHotspot() {
		
	}
	
	private void addUnsupported(UnsupportedNodeDescriptor desc) {
		
	}
	
	private void addString(IAbstractString str) {
		
	}

	private void addCollection(AbstractStringCollection str) {
		
	}

	
	public void cleanup() {
		removeOrphanedSecondaryPatterns();
	}
	
	private void removeOrphanedSecondaryPatterns() {
	}

	public void clearProject(String projectName) {
		//int projectId = getProjectId(projectName);
		db.execute("delete from abstract_strings where file_id in " +
				" (select id from files where name like (? || '/%'))", projectName); 
		// ... or delete abstract strings using triggers??
		db.execute("delete from files where name like (? || '/%')", projectName); 
		db.execute("delete from project_patterns where project_name = ?", projectName); 
	}

	public void addFiles(String projectName, List<String> compilationUnitNames) {
		// TODO: may be more efficient to insert in batches
		for (String name : compilationUnitNames) {
			addFile(projectName, name);
		}
	}
	
	private Connection connect() throws SQLException, ClassNotFoundException {
		Class.forName("org.hsqldb.jdbc.JDBCDriver");
		String path = AlvorCachePlugin.getDefault().getStateLocation().append("/cache2").toPortableString();
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
	
	private void checkCreateTables() throws SQLException {
		assert this.db != null;
		ResultSet res = db.getConnection().getMetaData().getTables(null, null, "FILES", null);
		if (!res.next()) {
			InputStream script = CreateDBHSQL.class.getClassLoader().getResourceAsStream("db/cache_setup.sql");
			db.runScript(script);
		}
	}
	
	public static Cache getInstance() {
		if (Cache.INSTANCE == null) {
			Cache.INSTANCE = new Cache(); 
		}
		return Cache.INSTANCE;
		
	}
	
	private void shutdown() {
		try {
			this.db.getConnection().close(); // TODO
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} 
	}
	
	public static void shutdownIfAlive() {
		if (INSTANCE != null) {
			INSTANCE.shutdown();
		}
	}

}

