package com.zeroturnaround.alvor.cache;

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
	private DatabaseHelper db;
	
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
				" from projects p" +
				" join files f on f.project_id = p.id" +
				" where p.name = ?" +
				" and f.batch_no < (select max(batch_no) from project_patterns where project_id = p.id)",
				projectName);
		
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
		
		int projectId = getProjectId(projectName);
		db.execute("delete from project_patterns where project_id = ?", projectId);
		
		for (HotspotPattern pattern : patterns) {
			int patternId = getOrCreatePatternId(pattern);
			db.execute(" insert into project_patterns " +
					" (project_id, pattern_id, batch_no, source)" +
					" values (?, ?, ?, ?)", 
					projectId, patternId, 0, "configuration"); 
		}
	}
	
	private int getProjectId(String projectName) {
		return db.queryInt("select id from projects where name = ?", projectName);
	}

	private int getOrCreatePatternId(HotspotPattern pattern) {
		// if exist then return else create
		return -1;
	}

	public List<PatternRecord> getProjectPatterns(String projectName) {
		return null;
	}

	public List<PatternRecord> getNewProjectPatterns(String projectName) {
		return null;
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

	public void clearProject(String name) {
		int projectId = getProjectId(name);
		db.execute("delete from abstract_strings where file_id in (select id from files where project_id = ?)", projectId); 
		// ... or delete abstract strings using triggers??
		db.execute("delete from files where project_id = ?", projectId); 
		db.execute("delete from project_patterns where project_id = ?", projectId); 
	}

	public void addFiles(String projectName, List<String> compilationUnitNames) {
		// TODO: may be more efficient to insert in batches
		for (String name : compilationUnitNames) {
			addFile(projectName, name);
		}
	}
}

