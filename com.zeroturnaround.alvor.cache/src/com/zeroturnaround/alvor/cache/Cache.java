package com.zeroturnaround.alvor.cache;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zeroturnaround.alvor.common.HotspotPattern;
import com.zeroturnaround.alvor.common.NodeDescriptor;
import com.zeroturnaround.alvor.common.UnsupportedNodeDescriptor;
import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.string.AbstractStringCollection;
import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.IPosition;

public class Cache {
	
	private final static ILog LOG = Logs.getLog(ICacheService.class);
	
	private final static int PATTERN_KIND_HOTSPOT = 1;
	private final static int PATTERN_KIND_STRING_METHOD = 2;
	
	private final static int KIND_CONSTANT    = 1;
	private final static int KIND_CHARSET	    = 2;
	private final static int KIND_SEQUENCE    = 3;
	private final static int KIND_CHOICE      = 4;
	private final static int KIND_APPLICATION = 5;
	private final static int KIND_REPETITION  = 6;
	private final static int KIND_UNSUPPORTED = 7;
	private final static int KIND_PARAMETER   = 8;
	
	private DatabaseHelper db;
	
	private Map<String, Integer> fileIDs = new HashMap<String, Integer>();
	
	/*package*/ Cache(DatabaseHelper db) {
		this.db = db;
	}
	
	public void removeFile(String fileName) {
		invalidateFile(fileName);
		db.execute("delete from files where name = ?", fileName);
	}
	
	public void addFile(String projectName, String fileName) {
		db.execute("insert into files (name, batch_no) values (?, 0)", 
				fileName);
	}
	
	public void invalidateFile(String fileName) {
		db.execute("delete from abstract_strings where file_id = " +
				" (select id from files where name = ?)", fileName);
	}
	
	public List<FileRecord> getFilesToUpdate(String projectName) {
		ResultSet rs = db.query (
				" select f.name, f.batch_no" +
				" from files f" +
				" where f.name like '/' || ? || '/%'" +
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
					" (project_name, pattern_id, batch_no, source_id)" +
					" values (?, ?, 1, null)", 
					projectName, patternId); 
		}
	}
	
	private int getOrCreatePatternId(HotspotPattern pattern) {

		Integer id = db.queryMaybeInteger (
				" select id from patterns where" +
				" class_name = ? and method_name = ? and argument_index = ?", 
				pattern.getClassName(), pattern.getMethodName(), pattern.getArgumentNo());

		if (id != null) {
			return id;
		}
		else {
			// first create pattern record in abstract_strings, this gives pattern_id
			id = createEmptyChoice(null);
			
			// then add actual pattern description into patterns table
			db.execute (
					" insert into patterns " +
					" (id, class_name, method_name, argument_index, kind) values (?, ?, ?, ?, " +
					PATTERN_KIND_HOTSPOT + ")",
					id, pattern.getClassName(), pattern.getMethodName(), pattern.getArgumentNo());
			
			return id;
		}
	}
	
	private int createEmptyChoice(IPosition pos) {
		if (pos == null) {
			return db.insertAndGetId("insert into abstract_strings (kind) " +
					" values (" + KIND_CHOICE + ")");
		}
		else {
			return db.insertAndGetId("insert into abstract_strings (kind, file_id, start, length) " +
					" values (" + KIND_CHOICE + ", ?, ?, ?)",
					getFileId(pos.getPath()), pos.getStart(), pos.getLength());
		}
	}

//	public List<PatternRecord> getProjectPatterns(String projectName) {
//		return null;
//	}

	public List<PatternRecord> getNewProjectPatterns(String projectName) {
		int minFileBatchNo = db.queryInt(
				" select coalesce(min(batch_no),0)" +
				" from files where name like '/' || ? || '/%'", projectName);
		
		ResultSet rs = db.query(
				" select p.class_name," +
				" 		 p.method_name," +
				"        p.argument_index," +
				"        pp.batch_no" +
				" from project_patterns pp" +
				" join patterns p on p.id = pp.pattern_id" +
				" where pp.project_name = ?" +
				" and batch_no > ?", 
				projectName, minFileBatchNo);
		
		List<PatternRecord> result = new ArrayList<PatternRecord>();
		try {
			while (rs.next()) {
				HotspotPattern pattern = new HotspotPattern(rs.getString(1), rs.getString(2), rs.getInt(3));
				result.add(new PatternRecord(pattern, rs.getInt(4)));
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
	
	private int createAbstractString(IAbstractString str) {
		return -1;
	}
	
	/**
	 * Marks this primary branch as unchecked  
	 */
	private void invalidateRespectivePrimaryHotspot() {
		
	}
	
	private void addUnsupported(UnsupportedNodeDescriptor desc) {
		
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
				" (select id from files where name like ('/' || ? || '/%'))", projectName); 
		// ... or delete abstract strings using triggers??
		db.execute("delete from files where name like ('/' || ? || '/%')", projectName); 
		db.execute("delete from project_patterns where project_name = ?", projectName); 
	}

	public void addFiles(String projectName, List<String> compilationUnitNames) {
		// TODO: may be more efficient to insert in batches
		for (String name : compilationUnitNames) {
			addFile(projectName, name);
		}
	}
	
	private int getFileId(String name) {
		// first try faster cache
		Integer id = fileIDs.get(name);
		if (id == null) {
			id = db.queryInt("select from files where name = ?", name);
			assert id != null; // TODO maybe should just create if doesn't exist ??
			fileIDs.put(name, id);
		}
		return id;
	}

	/* package */ void shutdown() {
		try {
			db.getConnection().close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}

