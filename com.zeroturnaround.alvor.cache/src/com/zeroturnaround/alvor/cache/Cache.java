package com.zeroturnaround.alvor.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zeroturnaround.alvor.common.FunctionPattern;
import com.zeroturnaround.alvor.common.FunctionPatternReference;
import com.zeroturnaround.alvor.common.HotspotPattern;
import com.zeroturnaround.alvor.common.HotspotPatternReference;
import com.zeroturnaround.alvor.common.NodeDescriptor;
import com.zeroturnaround.alvor.common.PatternReference;
import com.zeroturnaround.alvor.common.PositionUtil;
import com.zeroturnaround.alvor.common.StringNodeDescriptor;
import com.zeroturnaround.alvor.common.StringPattern;
import com.zeroturnaround.alvor.common.UnsupportedNodeDescriptor;
import com.zeroturnaround.alvor.common.UnsupportedStringOpEx;
import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.string.AbstractStringCollection;
import com.zeroturnaround.alvor.string.DummyPosition;
import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.IPosition;
import com.zeroturnaround.alvor.string.Position;
import com.zeroturnaround.alvor.string.StringCharacterSet;
import com.zeroturnaround.alvor.string.StringChoice;
import com.zeroturnaround.alvor.string.StringConstant;
import com.zeroturnaround.alvor.string.StringParameter;
import com.zeroturnaround.alvor.string.StringRepetition;
import com.zeroturnaround.alvor.string.StringSequence;

public class Cache {
	
	private final static ILog LOG = Logs.getLog(ICacheService.class);
	
	private final static int PATTERN_KIND_HOTSPOT = 1;
	private final static int PATTERN_KIND_FUNCTION = 2;
	
	private final static int PATTERN_ROLE_PRIMARY = 1;
	private final static int PATTERN_ROLE_SECONDARY = 2;
//	private final static int PATTERN_ROLE_FOREIGN = 3;
	
	private interface StringKind {
		public final static int CONSTANT     = 1;
		public final static int CHARSET	     = 2;
		public final static int SEQUENCE     = 3;
		public final static int CHOICE       = 4;
		public final static int FUNCTION_REF = 5;
		public final static int HOTSPOT_REF  = 6;
		public final static int REPETITION   = 7;
		public final static int PARAMETER    = 8;
		public final static int UNSUPPORTED  = 9;
	}	
	
	
	private DatabaseHelper db;
	private Map<String, Integer> fileIDs = new HashMap<String, Integer>();
	private int currentBatchNo;
	
	
	/*package*/ Cache(DatabaseHelper db) {
		this.db = db;
		this.currentBatchNo = db.queryInt("select coalesce(max(batch_no), 1) from project_patterns");
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
	
	public List<NodeDescriptor> getProjectHotspots(String projectName) {
		try {
			List<NodeDescriptor> result = new ArrayList<NodeDescriptor>();
			
			// query all strings from this project that are children 
			// of this project's primary patterns
			ResultSet rs = db.query(
					" select s.*, f.name as name" +
					" from files f" +
					" join abstract_strings s on s.file_id = f.id" +
					" join project_patterns pp on pp.project_name = ? and pp.pattern_id = s.parent_id " +
					" where f.name like '/' || ? || '/%'" +
					" and pp.pattern_role = " + PATTERN_ROLE_PRIMARY, 
					projectName, projectName);
			
			
			while (rs.next()) {
				try {
					IAbstractString str = createAbstractString(rs);
					result.add(new StringNodeDescriptor(str.getPosition(), str));
				} catch (UnsupportedStringOpEx e) {
					result.add(new UnsupportedNodeDescriptor(createPosition(rs), 
							e.getMessage(), e.getPosition()));
				}
			}
			
			return result;
			
		} 
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public List<FileRecord> getFilesToUpdate(String projectName) {
		ResultSet rs = db.query (
				" select f.id, f.name, f.batch_no" +
				" from files f" +
				" where f.name like '/' || ? || '/%'" +
				" and f.batch_no < (select max(batch_no) from project_patterns where project_name = ?)",
				projectName, projectName);
		
		List<FileRecord> records = new ArrayList<FileRecord>();
		try {
			while (rs.next()) {
				records.add(new FileRecord(rs.getInt("id"), rs.getString("name"), rs.getInt("batch_no")));
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
		
		// FIXME delete only primary patterns. Or clean everything???
		db.execute("delete from project_patterns where project_name = ?", projectName);
		
		for (StringPattern pattern : patterns) {
			int kind = getPatternKind(pattern);
			int patternId = getPatternId(kind, pattern.getClassName(), 
					pattern.getMethodName(), pattern.getArgumentNo());
			registerPatternForProject(patternId, projectName, PATTERN_ROLE_PRIMARY);
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
				" select p.id," +
				"        p.kind," +
				"        p.class_name," +
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
				StringPattern pattern;
				if (rs.getInt("kind") == PATTERN_KIND_HOTSPOT) {
					pattern = new HotspotPattern(rs.getString("class_name"), 
						rs.getString("method_name"), rs.getInt("argument_index"));
				} 
				else if (rs.getInt("kind") == PATTERN_KIND_FUNCTION) {
					pattern = new FunctionPattern(rs.getString("class_name"), 
							rs.getString("method_name"), rs.getInt("argument_index"));
				}
				else {
					throw new IllegalArgumentException();
				}
				result.add(new PatternRecord(pattern, rs.getInt("batch_no"), rs.getInt("id")));
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
		if (desc instanceof StringNodeDescriptor) {
			String projectName = PositionUtil.getProjectName(desc.getPosition()); 
			addAbstractString(((StringNodeDescriptor) desc).getAbstractValue(), pattern.getId(), 
					null, projectName);
		}
		else if (desc instanceof UnsupportedNodeDescriptor) {
			addUnsupported(((UnsupportedNodeDescriptor) desc).getProblemMessage(),
					desc.getPosition(),
					pattern.getId());
		}
		else {
			throw new IllegalArgumentException();
		}
	}
	
	private IAbstractString createAbstractString(ResultSet rs) {
		try {
			int kind = rs.getInt("kind");
			
			switch (kind) {
			case StringKind.CONSTANT: 
				return new StringConstant(createPosition(rs), rs.getString("str_value"), rs.getString("str_value2"));
			case StringKind.CHARSET: 
				return new StringCharacterSet(createPosition(rs), rs.getString("str_value"));
			case StringKind.SEQUENCE: return createStringCollection(rs, kind);
			case StringKind.CHOICE: return createStringCollection(rs, kind);
			case StringKind.FUNCTION_REF: return createStringFromFunctionRef(rs);
			case StringKind.HOTSPOT_REF: return createStringFromHotspotRef(rs);
			case StringKind.REPETITION: return createStringRepetition(rs);
			case StringKind.PARAMETER: return new StringParameter(rs.getInt("int_value"));
			case StringKind.UNSUPPORTED: throw new UnsupportedStringOpEx(rs.getString("str_value"), createPosition(rs));
			default: throw new IllegalArgumentException();
			}
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	private IAbstractString createStringFromFunctionRef(ResultSet rs) {
		 // TODO
		throw new UnsupportedStringOpEx("TODO", createPosition(rs));
	}

	private IAbstractString createStringFromHotspotRef(ResultSet rs) {
		 // TODO
		throw new UnsupportedStringOpEx("TODO", createPosition(rs));
	}

	private IAbstractString createStringRepetition(ResultSet rs) {
		try {
			ResultSet bodyRs = db.query(
					" select s.*, f.name " +
					" from abstract_strings s" +
					" join files f on f.id = s.file_id" +
					" where s.parent_id = ?" , rs.getInt("id"));
			
			boolean found = bodyRs.next();
			assert found;
			
			IAbstractString body = createAbstractString(bodyRs);
			
			// check that only one row was found
			found = bodyRs.next();
			assert !found;
			
			return new StringRepetition(createPosition(rs), body);
		} 
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private IAbstractString createStringCollection(ResultSet rs, int kind) {
		try {
			ResultSet childrenRs = db.query(
					" select s.*, f.name " +
					" from abstract_strings s" +
					" join files f on f.id = s.file_id" +
					" where s.parent_id = ?" +
					" order by s.item_index asc", rs.getInt("id"));
			
			List<IAbstractString> children = new ArrayList<IAbstractString>();
			while (childrenRs.next()) {
				children.add(createAbstractString(childrenRs));
			}
			
			if (kind == StringKind.SEQUENCE) {
				return new StringSequence(createPosition(rs), children);
			}
			else if (kind == StringKind.CHOICE) {
				return new StringChoice(createPosition(rs), children);
			}
			else {
				throw new IllegalArgumentException();
			}
		} 
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private IPosition createPosition(ResultSet rs) {
		try {
			String fileName = rs.getString("name");
			if (rs.wasNull()) {
				return null;
			}
			else {
				return new Position(fileName, rs.getInt("start"), rs.getInt("length"));
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	private int addAbstractString(IAbstractString str, Integer parentId, Integer itemIndex,
			String projectName) {
		if (str instanceof StringConstant) {
			StringConstant cnst = (StringConstant)str;
			assert cnst.getEscapedValue() != null;
			assert cnst.getConstant() != null;
			return addAbstractStringRecord(StringKind.CONSTANT, parentId, itemIndex, null, 
					cnst.getConstant(), cnst.getEscapedValue(), str.getPosition());
		}
		else if (str instanceof StringCharacterSet) {
			return addAbstractStringRecord(StringKind.CHARSET, parentId, itemIndex, null, 
					((StringCharacterSet) str).getContentsAsString(), null, str.getPosition());
		}
		else if (str instanceof StringParameter) {
			return addAbstractStringRecord(StringKind.PARAMETER, parentId, itemIndex,
					((StringParameter)str).getIndex(), 
					null, null, str.getPosition());
		}
		else if (str instanceof StringRepetition) {
			return addRepetition((StringRepetition)str, parentId, itemIndex, projectName);
		}
		else if (str instanceof AbstractStringCollection) {
			return addStringCollection((AbstractStringCollection)str, parentId, itemIndex, projectName); 
		}
		else if (str instanceof PatternReference) {
			return addPatternReference((PatternReference)str, parentId, itemIndex, projectName);
		}
		else {
			throw new IllegalArgumentException("Unexpected IAbstractString: " + str.getClass());
		}
	}

	private int addRepetition(StringRepetition str, Integer parentId, Integer itemIndex, String projectName) {
		
		int id = addAbstractStringRecord(StringKind.REPETITION, parentId, itemIndex, 
				null, null, null, str.getPosition());
		addAbstractString(str.getBody(), id, null, projectName); 
		return id;
	}
	
	private int addPatternReference(PatternReference str, Integer parentId, Integer itemIndex, String projectName) {
		
		// prepare pattern and project_pattern (if they don't exist yet)
		int patternKind = getPatternKind(str);
		int patternId = getPatternId(patternKind, str.getClassName(), 
				str.getMethodName(), str.getArgumentIndex());
		registerPatternForProject(patternId, projectName, PATTERN_ROLE_SECONDARY);
		
		// add reference to this pattern
		int recKind = (patternKind == PATTERN_KIND_FUNCTION) 
			? StringKind.FUNCTION_REF : StringKind.HOTSPOT_REF;
		
		int id = addAbstractStringRecord(recKind, parentId, itemIndex, patternId, null, null, str.getPosition());
		
		// more work for FunctionReference: add each argument
		if (str instanceof FunctionPatternReference) {
			FunctionPatternReference fpr = (FunctionPatternReference)str;
			for (Map.Entry<Integer, IAbstractString> entry : fpr.getInputArguments().entrySet()) {
				addAbstractString(entry.getValue(), id, entry.getKey(), projectName);
			}
		}
		
		return id;
	}

	private int addStringCollection(AbstractStringCollection str,
			Integer parentId, Integer itemIndex, String projectName) {
		
		int kind = (str instanceof StringSequence) ? StringKind.SEQUENCE : StringKind.CHOICE; 
		int id = addAbstractStringRecord(kind, parentId, itemIndex, null, null, null, str.getPosition());
		
		int childIndex = 1;
		for (IAbstractString child: str.getItems()) {
			addAbstractString(child, id, childIndex, projectName);
			childIndex++;
		}
		
		return id;
	}

	private void registerPatternForProject(int patternId, String projectName, int patternRole) {
		int existing = db.queryInt(
				" select count(*) from project_patterns " +
				" where project_name = ?" +
				" and pattern_id = ?", projectName, patternId);
		
		if (existing == 0) {
			db.execute (
					" insert into project_patterns " +
					" (project_name, pattern_id, pattern_role, batch_no)" +
					" values (?, ?, ?, ?)", 
					projectName, patternId, patternRole, this.currentBatchNo);
		}
	}
	
	private int getPatternId(int kind, String className, String methodName, int argumentIndex) {
		Integer id = db.queryMaybeInteger(
				" select id from patterns " +
				" where kind = ?" +
				" and class_name = ?" +
				" and method_name = ?" +
				" and argument_index = ?",
				kind, className, methodName, argumentIndex);

		if (id == null) {
			// create empty choice for pattern options
			id = db.insertAndGetId("insert into abstract_strings (kind) " +
					" values (" + StringKind.CHOICE + ")");
			
			// pattern uses same id
			db.execute (
					" insert into patterns (id, kind, class_name, method_name, argument_index)" +
					" values (?, ?, ?, ?, ?)", 
					id, kind, className, methodName, argumentIndex);
		}

		return id;		
	}

	private int addUnsupported(String msg, IPosition pos, Integer parentId) {
		assert parentId != null;
		return addAbstractStringRecord(StringKind.UNSUPPORTED, parentId,
				null, null, null, msg, pos);
	}
	
	private int addAbstractStringRecord(int kind, Integer parentId, 
			Integer itemIndex, Integer intValue,
			String strValue, String strValue2, IPosition pos) {
		
		
		Integer fileId = null;
		Integer start = null;
		Integer length = null;
		if (pos != null && !DummyPosition.isDummyPosition(pos)) {
			fileId = getFileId(pos.getPath());
			start = pos.getStart();
			length = pos.getLength();
		}
		
		return db.insertAndGetId(
				"insert into abstract_strings (kind, parent_id, " +
				" item_index, int_value, str_value, str_value2, " +
				" file_id, start, length) " +
				" values (?, ?, ?, ?, ?, ?, ?, ?, ?)", 
				kind,
				DatabaseHelper.encodeNull(parentId),
				DatabaseHelper.encodeNull(itemIndex),
				DatabaseHelper.encodeNull(intValue),
				DatabaseHelper.encodeNull(strValue),
				DatabaseHelper.encodeNull(strValue2),
				DatabaseHelper.encodeNull(fileId),
				DatabaseHelper.encodeNull(start),
				DatabaseHelper.encodeNull(length));
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
			try {
				id = db.queryInt("select id from files where name = ?", name);
				assert id != null; // TODO maybe should just create if doesn't exist ??
				fileIDs.put(name, id);
			} catch (Exception e) {
				throw new IllegalArgumentException("Can't find file: " + name);
			}
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
	
	private static int getPatternKind(StringPattern pattern) {
		if (pattern instanceof HotspotPattern) {
			return PATTERN_KIND_HOTSPOT;
		}
		else if (pattern instanceof FunctionPattern) {
			return PATTERN_KIND_FUNCTION;
		}
		else {
			throw new IllegalArgumentException();
		}
	}
	
	private static int getPatternKind(PatternReference patternReference) {
		if (patternReference instanceof HotspotPatternReference) {
			return PATTERN_KIND_HOTSPOT;
		}
		else if (patternReference instanceof FunctionPatternReference) {
			return PATTERN_KIND_FUNCTION;
		}
		else {
			throw new IllegalArgumentException();
		}
	}
	
	public void startNewBatch() {
		this.currentBatchNo++;
	}

	public void markFilesAsCurrent(List<FileRecord> fileRecords) {
		for (FileRecord rec : fileRecords) {
			db.execute("update files set batch_no = ? where id = ?", 
					this.currentBatchNo, rec.getId());
		}
	}
	
	public void printQueryCount() {
		System.out.println(db.queryCount);
	}
}

