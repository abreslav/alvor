package com.zeroturnaround.alvor.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zeroturnaround.alvor.common.FieldPattern;
import com.zeroturnaround.alvor.common.FieldPatternReference;
import com.zeroturnaround.alvor.common.FunctionPattern;
import com.zeroturnaround.alvor.common.FunctionPatternReference;
import com.zeroturnaround.alvor.common.HotspotDescriptor;
import com.zeroturnaround.alvor.common.HotspotPattern;
import com.zeroturnaround.alvor.common.HotspotPatternReference;
import com.zeroturnaround.alvor.common.IntegerList;
import com.zeroturnaround.alvor.common.PatternReference;
import com.zeroturnaround.alvor.common.PositionUtil;
import com.zeroturnaround.alvor.common.StringConverter;
import com.zeroturnaround.alvor.common.StringNodeDescriptor;
import com.zeroturnaround.alvor.common.StringPattern;
import com.zeroturnaround.alvor.common.UnsupportedNodeDescriptor;
import com.zeroturnaround.alvor.common.UnsupportedStringOpEx;
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
import com.zeroturnaround.alvor.string.util.AbstractStringUtils;
import com.zeroturnaround.alvor.string.util.ArgumentApplier;

public class Cache {
	
	//private final static ILog LOG = Logs.getLog(ICacheService.class);
	
	private final static int PATTERN_KIND_HOTSPOT = 1;
	private final static int PATTERN_KIND_FUNCTION = 2;
	private final static int PATTERN_KIND_FIELD = 3;
	
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
		public final static int FIELD_REF    = 7;
		public final static int REPETITION   = 8;
		public final static int PARAMETER    = 9;
		public final static int UNSUPPORTED  = 10;
	}	
	
	private DatabaseHelper db;
	private Map<String, Integer> fileIDs = new HashMap<String, Integer>();
	private int currentBatchNo;
	
	private int maxDepth = 0;
	private int depth;
	
	
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
	
	public List<HotspotDescriptor> getProjectHotspots(String projectName) {
		return getHotspots(projectName, null);		
	}
	
	public List<HotspotDescriptor> getHotspots(String projectName, String fileName) {
		ResultSet rs = null;
		
		try {
			List<HotspotDescriptor> result = new ArrayList<HotspotDescriptor>();
			
			// query all strings from this project that are children 
			// of this project's primary patterns
			
			String sql = 
				" select s.*, f.name as file_name," +
				" hf.name as hotspot_file_name," +
				" h.start as hotspot_start," +
				" h.length as hotspot_length" +
				" from files f" +
				" join abstract_strings s on s.file_id = f.id" +
				" join project_patterns pp on pp.project_name = ? and pp.pattern_id = s.parent_id " +
				" join hotspots h on h.string_id = s.id" +
				" join files hf on hf.id = h.file_id";
			String fileFilter;
			
			if (fileName == null) {
				sql += " where f.name like '/' || ? || '/%'";
				fileFilter = projectName;
			}
			else {
				sql += " where f.name = ?";
				fileFilter = fileName;
			}
			sql += " and pp.pattern_role = " + PATTERN_ROLE_PRIMARY; 
			
			rs = db.query(sql, projectName, fileFilter);
			
			while (rs.next()) {
				
				IPosition hotspotPos = new Position(rs.getString("hotspot_file_name"), 
						rs.getInt("hotspot_start"), rs.getInt("hotspot_length")); 
				
				try {
					IAbstractString str = createAbstractString(rs, null);
					result.add(new StringNodeDescriptor(hotspotPos, str));
				} catch (UnsupportedStringOpEx e) {
					result.add(new UnsupportedNodeDescriptor(hotspotPos, 
							e.getMessage(), e.getPosition()));
				}
			}
			
			return result;
			
		} 
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		finally {
			db.checkCloseResult(rs);
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
		finally {
			db.checkCloseResult(rs);
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
					pattern.getMethodName(), pattern.getArgumentTypes(), pattern.getArgumentNo());
			registerPatternForProject(patternId, projectName, PATTERN_ROLE_PRIMARY);
		}
	}
	
//	public List<PatternRecord> getProjectPatterns(String projectName) {
//		return null;
//	}

	private StringPattern createPattern(int id) {
		ResultSet rs = null;

		try {
			rs = db.query(
				" select p.id," +
				"        p.kind," +
				"        p.class_name," +
				" 		 p.method_name," +
				" 		 p.argument_types," +
				"        p.argument_index" +
				" from patterns p" +
				" where p.id = ?", 
				id);
			boolean found = rs.next();
			assert found;
			return createPattern(rs);
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		finally {
			db.checkCloseResult(rs);
		}
	}
	
	private StringPattern createPattern(ResultSet rs) {
		try {
			StringPattern pattern;
			if (rs.getInt("kind") == PATTERN_KIND_HOTSPOT) {
				pattern = new HotspotPattern(rs.getString("class_name"), 
						rs.getString("method_name"), rs.getString("argument_types"),
						rs.getInt("argument_index"));
			} 
			else if (rs.getInt("kind") == PATTERN_KIND_FUNCTION) {
				pattern = new FunctionPattern(rs.getString("class_name"), 
						rs.getString("method_name"), rs.getString("argument_types"), 
						rs.getInt("argument_index"));
			}
			else if (rs.getInt("kind") == PATTERN_KIND_FIELD) {
				pattern = new FieldPattern(rs.getString("class_name"), 
						rs.getString("method_name"));
			}
			else {
				throw new IllegalArgumentException();
			}
			return pattern;
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public List<PatternRecord> getNewProjectPatterns(String projectName) {
		int minFileBatchNo = db.queryInt(
				" select coalesce(min(batch_no),0)" +
				" from files where name like '/' || ? || '/%'", projectName);
		
		ResultSet rs = db.query(
				" select p.id," +
				"        p.kind," +
				"        p.class_name," +
				" 		 p.method_name," +
				" 		 p.argument_types," +
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
				StringPattern pattern = createPattern(rs);
				result.add(new PatternRecord(pattern, rs.getInt("batch_no"), rs.getInt("id")));
			}
			return result;
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		finally {
			db.checkCloseResult(rs);
		}
	}

	private void markHotspotsAsChecked(Collection<IPosition> positions) {
		
		for (IPosition pos : positions) {
			db.execute(
				" update hotspots " +
				" set checked = true " +
				" where file_id = ?" +
				" and start = ?" +
				" and lenght = ?", 
				getFileId(pos.getPath()), pos.getStart(), pos.getLength());
		}
	}
	
	public void addHotspot(PatternRecord pattern, HotspotDescriptor desc) {
		int id; 
		if (desc instanceof StringNodeDescriptor) {
			String projectName = PositionUtil.getProjectName(desc.getPosition());
			
			IAbstractString str = ((StringNodeDescriptor) desc).getAbstractValue();
			
			// TODO should I include item-index? or should i rely on increasing id values ??
			try {
				id = addAbstractString(str, pattern.getId(), null, projectName);
			} catch (IllegalArgumentException e) {
				System.err.println("RECSTRING: " + str);
				throw e;
			}
		}
		else if (desc instanceof UnsupportedNodeDescriptor) {
			id = addUnsupported(((UnsupportedNodeDescriptor) desc).getProblemMessage(),
					desc.getPosition(),
					pattern.getId());
			
		}
		else {
			throw new IllegalArgumentException();
		}
		
		// need to record also original position (because string inside desc may have other position)
		createHotspotRecord(desc.getPosition(), id);
		
		invalidateCheckingForDependentStrings(pattern.getId());
	}
	
	private void createHotspotRecord(IPosition pos, int stringId) {
		db.execute(
				" insert into hotspots (string_id, file_id, start, length)" +
				" values (?, ?, ?, ?)", 
				stringId, getFileId(pos.getPath()), pos.getStart(), pos.getLength());
	}
	
	private void invalidateCheckingForDependentStrings(int stringId) {
		
//		
//		
//		// TODO this should be skipped during full scan, because
//		// everything will be rechecked anyway
//		
//		// TODO this can go into cycle
//		
//		// dependent strings are its ancestors 
//		// and if it's a pattern, then also its users (kind = FUNCTION_REF or HOTSPOT_REF)
//		
//		db.execute("update hotspots set checked = false where id = ?", stringId);
//		
//		// invalidate ancestors
//		Integer parentId = db.queryMaybeInteger("select parent_id from abstract_strings where id = ?", stringId);
//		if (parentId != null) {
//			invalidateCheckingForDependentStrings(parentId);
//		}
//		// String is a pattern iff it doesn't have a parent.
//		// In case of patterns, invalidate their users 
//		else {
//			ResultSet rs = db.query(
//				" select id from abstract_strings " +
//				" where kind in (" + StringKind.FUNCTION_REF + "," + StringKind.HOTSPOT_REF + ")" +
//				" and int_value = ?",  stringId);
//			
//			try {
//				while (rs.next()) {
//					invalidateCheckingForDependentStrings(rs.getInt("id"));
//				}
//			} 
//			catch (SQLException e) {
//				throw new RuntimeException(e);
//			}
		
		// TODO close result set
//		}
	}
	
	private IAbstractString createAbstractString(ResultSet rs, IntegerList context) {
		this.depth++;
		if (this.depth > maxDepth) {
			maxDepth = depth;
		}
		
		try {
			// recursion check
			IPosition pos = createPosition(rs);
			if (context != null && pos != null && context.contains(pos.hashCode())) {
				throw new UnsupportedStringOpEx("Cache recursion at: " + PositionUtil.getLineString(pos), pos); 
			}
				
			int kind = rs.getInt("kind");
			
			switch (kind) {
			case StringKind.CONSTANT: 
				return new StringConstant(createPosition(rs), rs.getString("str_value"), rs.getString("str_value2"));
			case StringKind.CHARSET: 
				return new StringCharacterSet(createPosition(rs), rs.getString("str_value"));
			case StringKind.SEQUENCE: 
				return createStringCollection(rs, kind, context);
			case StringKind.CHOICE: 
				return createStringCollection(rs, kind, context);
			case StringKind.FUNCTION_REF: 
				return createStringFromFunctionRef(rs, context);
			case StringKind.HOTSPOT_REF: 
				return createStringFromHotspotOrFieldRef(rs, context);
			case StringKind.FIELD_REF: 
				return createStringFromHotspotOrFieldRef(rs, context);
			case StringKind.REPETITION: 
				return createStringRepetition(rs, context);
			case StringKind.PARAMETER: 
				return new StringParameter(rs.getInt("int_value"));
			case StringKind.UNSUPPORTED: 
				throw new UnsupportedStringOpEx(rs.getString("str_value"), createPosition(rs));
			default: 
				throw new IllegalArgumentException();
			}
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		finally {
			this.depth--;
		}
	}
	
	private IAbstractString createStringFromFunctionRef(ResultSet rs, IntegerList context) {
		try {
			IPosition pos = createPosition(rs);
			
			// get function
			IAbstractString template = createAbstractString(rs.getInt("int_value"), new IntegerList(pos.hashCode(), context));
			
			// get arguments
			Map<Integer, IAbstractString> args = new HashMap<Integer, IAbstractString>();
			ResultSet argRs = queryChildren(rs.getInt("id"));
			try {
				while (argRs.next()) {
					args.put(argRs.getInt("item_index"), createAbstractString(argRs, new IntegerList(pos.hashCode(), context)));
				}
			}
			finally {
				db.checkCloseResult(argRs);
			}
			
			// TODO isn't there wrong position for resulting string?
			return ArgumentApplier.applyArgumentsMap(template, args);
		} 
		catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private IAbstractString createStringFromHotspotOrFieldRef(ResultSet rs, IntegerList context) {
		// Return a pattern(choice) referred to by int_value.
		// Take all from referred record, except position info.
		// ch - choice
		// ref - reference
		ResultSet newRs = null;
		try {
			newRs = db.query(
					" select ch.id," +
					"        ch.kind," +
					"        ch.parent_id," +
					"        ch.item_index," +
					"        ch.int_value," +
					"        ch.str_value," +
					"        ch.str_value2," +
					"        rf.name as file_name," +
					"        ref.start," +
					"        ref.length" +
					" from abstract_strings ref" +
					" join abstract_strings ch on ch.id = ref.int_value" +
					" join files rf on rf.id = ref.file_id" +
					" where ref.id = ?", 
					rs.getInt("id")); 
			
			boolean found = newRs.next();
			assert found;
//			IPosition refPos = createPosition(rs);
//			return createAbstractString(newRs, new IntegerList(refPos, context));
			return createAbstractString(newRs, context); // jääb sama kontekst, sest patternil pole positsiooni
		} 
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		finally {
			db.checkCloseResult(newRs);
		}
	}

	private IAbstractString createStringRepetition(ResultSet rs, IntegerList context) {
		try {
			IPosition pos = createPosition(rs);
			
			ResultSet childRs = queryChildren(rs.getInt("id"));
			try {
				boolean found = childRs.next();
				assert found;
				IAbstractString body = createAbstractString(childRs, new IntegerList(pos.hashCode(), context));
				assert (!childRs.next());
				return new StringRepetition(pos, body);
			}
			finally {
				db.checkCloseResult(childRs);
			}
		} 
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	private IAbstractString createAbstractString(int id, IntegerList context) {
		ResultSet rs = null;
		try {
			rs = db.query(
					" select s.*, f.name as file_name" +
					" from abstract_strings s" +
					" left join files f on f.id = s.file_id" +
					" where s.id = ?" , id);
			if (!rs.next()) {
				throw new IllegalArgumentException("Abstract string " + id + " not found");
			}
			return createAbstractString(rs, context);
		} 
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		finally {
			db.checkCloseResult(rs);
		}
	}

	private IAbstractString createStringCollection(ResultSet rs, int kind, IntegerList context) {
		try {
			int id = rs.getInt("id");
			IPosition pos = createPosition(rs);
			List<IAbstractString> children = new ArrayList<IAbstractString>();
			
			IntegerList newContext = context;
			if (pos != null) {
				newContext = new IntegerList(pos.hashCode(), context);
			}
			
			ResultSet childrenRs = queryChildren(id);
			try {
				while (childrenRs.next()) {
					children.add(createAbstractString(childrenRs, newContext));
				}
			}
			finally {
				db.checkCloseResult(childrenRs);
			}
			
			if (kind == StringKind.SEQUENCE) {
				return new StringSequence(pos, children);
			}
			else if (kind == StringKind.CHOICE) {
				children = AbstractStringUtils.removeDuplicates(children, true);
				
				if (children.size() == 1) {
					return children.get(0);
				}
				else if (children.size() == 0) {
					// must be a pattern
					StringPattern pattern = createPattern(id);
					if (pattern != null) {
						String problem = "Can't find definition for: ";
						if (pattern instanceof HotspotPattern) {
							problem = "Can't find call-sites for:";
						}
						throw new UnsupportedStringOpEx(problem
								+ pattern.getClassName()
								+ "." + pattern.getMethodName()
								+ "(" + pattern.getArgumentTypes() + ")", pos);
					}
					else {
						throw new UnsupportedStringOpEx("String collection failed (empty choice)", pos);
					}
				}
				else {
					StringChoice choice = new StringChoice(pos, children);
					if (!choice.containsRecursion()) {
						return StringConverter.optimizeChoice(choice);
					}
					else {
						return choice;
					}
				}
			}
			else {
				throw new IllegalArgumentException();
			}
		} 
		catch (SQLException e) {
			System.out.println("DEPTH: " + this.depth);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	private ResultSet queryChildren(int parentId) {
		return db.query(
				" select s.*, f.name as file_name" +
				" from abstract_strings s" +
				" join files f on f.id = s.file_id" +
				" where s.parent_id = ?" +
				" order by s.item_index asc, s.id asc", parentId);
	}

	private IPosition createPosition(ResultSet rs) {
		try {
			String fileName = rs.getString("file_name");
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
		int patternId = getPatternId(patternKind, 
				str.getPattern().getClassName(), 
				str.getPattern().getMethodName(), 
				str.getPattern().getArgumentTypes(), 
				str.getPattern().getArgumentNo());
		registerPatternForProject(patternId, projectName, PATTERN_ROLE_SECONDARY);
		
		// add reference to this pattern
		int recKind;
		if (str.getPattern() instanceof FunctionPattern) { recKind = StringKind.FUNCTION_REF; }
		else if (str.getPattern() instanceof HotspotPattern) { recKind = StringKind.HOTSPOT_REF; }
		else if (str.getPattern() instanceof FieldPattern) { recKind = StringKind.FIELD_REF; }
		else {throw new IllegalArgumentException(); }
		
		int id = addAbstractStringRecord(recKind, parentId, itemIndex, patternId, null, null, str.getPosition());
		
		// more work in case of FunctionReference: add each argument
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
					projectName, patternId, patternRole, 
					this.currentBatchNo+1); // will be dealt with in next batch
		}
	}
	
	private int getPatternId(int kind, String className, String methodName, String argumentTypes, int argumentIndex) {
		Integer id = db.queryMaybeInteger(
				" select id from patterns " +
				" where kind = ?" +
				" and class_name = ?" +
				" and method_name = ?" +
				" and argument_types = ?" +
				" and argument_index = ?",
				kind, className, methodName, argumentTypes, argumentIndex);

		if (id == null) {
			// create empty choice for pattern options
			id = db.insertAndGetId("insert into abstract_strings (kind) " +
					" values (" + StringKind.CHOICE + ")");
			
			// pattern uses same id
			db.execute (
					" insert into patterns (id, kind, class_name, method_name, argument_types, argument_index)" +
					" values (?, ?, ?, ?, ?, ?)", 
					id, kind, className, methodName, argumentTypes, argumentIndex);
		}

		return id;		
	}

	private int addUnsupported(String msg, IPosition pos, Integer parentId) {
		assert parentId != null;
		return addAbstractStringRecord(StringKind.UNSUPPORTED, parentId,
				null, null, msg, null, pos);
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

	public void clearAll() {
		db.execute("delete from abstract_strings"); 
		db.execute("delete from project_patterns"); 
		db.execute("delete from patterns"); 
		db.execute("delete from hotspots"); 
		db.execute("delete from files"); 
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
		else if (patternReference instanceof FieldPatternReference) {
			return PATTERN_KIND_FIELD;
		}
		else {
			throw new IllegalArgumentException();
		}
	}
	
	public void startNextBatch() {
		this.currentBatchNo++;
	}

	public void markFilesAsCurrent(List<FileRecord> fileRecords) {
		for (FileRecord rec : fileRecords) {
			db.execute("update files set batch_no = ? where id = ?", 
					this.currentBatchNo, rec.getId());
		}
	}
	
	public boolean projectIsInitialized(String projectName) {
		
		// TODO: measure how slow is it
		int fileCount = db.queryInt(
			" select count(*) from files" +
			" where name like '/' || ? || '/%'", projectName);
		
		return fileCount > 0;
	}
	
	public void printQueryCount() {
		System.out.println(db.queryCount);
	}
	
	public Collection<HotspotDescriptor> getFileHotspots(String fileName, String projectName) {
		return getHotspots(projectName, fileName);
	}
	
	public Collection<String> getUncheckedFiles(String projectName) {
		ResultSet rs = db.query(
				" select" + 
				" distinct f.name" + 
				" from hotspots h" + 
				" join files f on f.id = h.file_id" + 
				" where h.checked = false" + 
				" and f.name like '/' || ? || '/%'", projectName);
		
		try {
			List<String> result = new ArrayList<String>();
			try {
				while (rs.next()) {
					result.add(rs.getString(1));
				}
			} 
			catch (SQLException e) {
				throw new RuntimeException(e);
			}
			
			return result;
		} 
		finally {
			db.checkCloseResult(rs);
		}
	}
	
	public void printDBInfo() {
		System.out.println("Currenlty open ResultSet-s: " + db.openResultSetCount);
		System.out.println("Max open ResultSet-s so far: " + db.maxOpenResultSetCount);
		System.out.println("Nr of executed queries: " + db.queryCount);
	}
	
	private IntegerList contextOf(IPosition pos, IntegerList prevContext) {
		return new IntegerList(pos.hashCode(), prevContext);
	}
	
	
	
}

