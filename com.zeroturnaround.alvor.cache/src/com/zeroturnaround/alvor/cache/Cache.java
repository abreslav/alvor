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
import com.zeroturnaround.alvor.common.StringHotspotDescriptor;
import com.zeroturnaround.alvor.common.StringPattern;
import com.zeroturnaround.alvor.common.UnsupportedHotspotDescriptor;
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

/**
 * see db/cache_setup.sql about structure of the database
 * @author Aivar
 *
 */
public class Cache {
	
	//private final static ILog LOG = Logs.getLog(ICacheService.class);
	
	private final static int PATTERN_KIND_HOTSPOT = 1;
	private final static int PATTERN_KIND_FUNCTION = 2;
	private final static int PATTERN_KIND_FIELD = 3;
	
	private final static int PATTERN_ROLE_PRIMARY = 1;
	private final static int PATTERN_ROLE_SECONDARY = 2;
	
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
	
	private final DatabaseHelper db;
//	private final String projectName;
	private final Map<String, Integer> fileIDs = new HashMap<String, Integer>();
	private int maxPatternBatchNo;
	
	/*package*/ Cache(DatabaseHelper db, String projectName) {
		this.db = db;
		this.maxPatternBatchNo = db.queryInt("select coalesce(max(batch_no), 0) from patterns");
	}
	
	public void removeFile(String fileName) {
		fileIDs.remove(fileName);
		db.execute("delete from files where name = ?", fileName);
		// this cascade-deletes related stuff
	}
	
	public void addFile(String fileName, boolean isThisProjectFile) {
		// in foreign files I don't want to find primary patterns,
		// therefore I set their batch_no as if I already searched them for primary patterns
		db.execute("insert into files (name, batch_no) values (?, ?)", 
				fileName, isThisProjectFile ? 0 : 1);
	}
	
	public void invalidateFile(String fileName, boolean isThisProjectFile) {
		db.execute("delete from abstract_strings where file_id = " +
				" (select id from files where name = ?)", fileName);
		db.execute("update files set batch_no = ? where name = ?", 
				isThisProjectFile ? 0 : 1, fileName);
	}
	
	// for debugging
	public IAbstractString getAbstractString(IPosition pos) {
		Integer id = db.queryMaybeInteger(
				" select a.id" +
				" from files f " +
				" join abstract_strings a on a.file_id = f.id" +
				" where f.name = ?" +
				" and a.start = ?", pos.getPath(), pos.getStart());
		if (id == null) {
			id = db.queryMaybeInteger(
					" select h.string_id" +
					" from files f " +
					" join hotspots h on h.file_id = f.id" +
					" where f.name = ?" +
					" and h.start = ?", pos.getPath(), pos.getStart());
		}
		if (id == null) {
			throw new IllegalArgumentException("can't find string with this position: path="
					+ pos.getPath() + ", start=" + pos.getStart());
		}
		else {
			return createAbstractString(id, null);
		}
	}
	
	public List<HotspotDescriptor> getPrimaryHotspots(boolean onlyUnchecked) {
		ResultSet rs = null;
		
		try {
			List<HotspotDescriptor> result = new ArrayList<HotspotDescriptor>();
			
			// query all strings that are children of primary patterns
			
			String sql = 
				" select s.*, f.name as file_name," +
				" hf.name as hotspot_file_name," +
				" h.start as hotspot_start," +
				" h.length as hotspot_length" +
				" from patterns p" +
				" join abstract_strings s on s.parent_id = p.id" +
				" join files f on f.id = s.file_id" +
				" join hotspots h on h.string_id = s.id" +
				" join files hf on hf.id = h.file_id" +
				" where p.pattern_role = " + PATTERN_ROLE_PRIMARY;
			
			if (onlyUnchecked) {
				sql += " and h.checked = false";
			}
			rs = db.query(sql);
			
			while (rs.next()) {
				
				IPosition hotspotPos = new Position(rs.getString("hotspot_file_name"), 
						rs.getInt("hotspot_start"), rs.getInt("hotspot_length"));
				
				try {
					IAbstractString str = createAbstractString(rs, null);
					result.add(new StringHotspotDescriptor(hotspotPos, str));
				} catch (UnsupportedStringOpEx e) {
					result.add(new UnsupportedHotspotDescriptor(hotspotPos, 
							e.getMessage(), e.getPosition()));
//				} catch (Exception e) {
//					result.add(new UnsupportedHotspotDescriptor(hotspotPos, 
//							"Internal error: " + e.getMessage(), null));
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
	
	public List<FileRecord> getFilesToUpdate() {
		ResultSet rs = db.query (
				" select f.id, f.name, f.batch_no" +
				" from files f" +
				" where f.batch_no < ?", this.maxPatternBatchNo);
		
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
	
	public void initializeProject(Collection<HotspotPattern> primaryPatterns, 
			List<String> projectFiles) {
		
		assert (maxPatternBatchNo == 0);
		
		for (StringPattern pattern : primaryPatterns) {
			int kind = getPatternKind(pattern);
			createPatternRecord(kind, pattern.getClassName(),pattern.getMethodName(), 
					pattern.getArgumentTypes(), pattern.getArgumentNo(),
					PATTERN_ROLE_PRIMARY, 1);
		}
		this.maxPatternBatchNo = 1;
		
		for (String name : projectFiles) {
			addFile(name, true);
		}
	}
	
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
			if (!found) {
				return null;
			}
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
	
	public List<PatternRecord> getNewProjectPatterns() {
		int minFileBatchNo = db.queryInt(
				" select coalesce(min(batch_no),0) from files");
		
		ResultSet rs = db.query(
				" select p.id," +
				"        p.kind," +
				"        p.class_name," +
				" 		 p.method_name," +
				" 		 p.argument_types," +
				"        p.argument_index," +
				"        p.batch_no," +
				"        p.pattern_role" +
				" from patterns p" +
				" where p.batch_no > ?", minFileBatchNo);
		
		List<PatternRecord> result = new ArrayList<PatternRecord>();
		try {
			while (rs.next()) {
				StringPattern pattern = createPattern(rs);
				result.add(new PatternRecord(pattern, rs.getInt("batch_no"), 
						rs.getInt("pattern_role"), rs.getInt("id")));
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

	public void markHotspotAsChecked(HotspotDescriptor hotspot) {
	
		IPosition pos = hotspot.getPosition();
		db.execute(
			" update hotspots " +
			" set checked = true " +
			" where file_id = ?" +
			" and start = ?" +
			" and length = ?", 
			getFileId(pos.getPath()), pos.getStart(), pos.getLength());
	}
	
	public void addHotspot(PatternRecord pattern, HotspotDescriptor desc) {
		int id; 
		if (desc instanceof StringHotspotDescriptor) {
			IAbstractString str = ((StringHotspotDescriptor) desc).getAbstractValue();
			
			try {
				id = addAbstractString(str, pattern.getId(), null);
			} catch (IllegalArgumentException e) {
				System.err.println("RECSTRING: " + str);
				throw e;
			}
		}
		else if (desc instanceof UnsupportedHotspotDescriptor) {
			id = addUnsupported(((UnsupportedHotspotDescriptor) desc).getProblemMessage(),
					desc.getPosition(),
					pattern.getId());
			
		}
		else {
			throw new IllegalArgumentException();
		}
		
		// need to record also original position (because string inside desc may have other position)
		createHotspotRecord(desc.getPosition(), id);
		
		invalidateCheckingForDependentStrings(id, null);
	}
	
	private void createHotspotRecord(IPosition pos, int stringId) {
		db.execute(
				" insert into hotspots (string_id, file_id, start, length)" +
				" values (?, ?, ?, ?)", 
				stringId, getFileId(pos.getPath()), pos.getStart(), pos.getLength());
	}
	
	private void invalidateCheckingForDependentStrings(int stringId, IntegerList context) {
		
		// infinite recursion check
		if (context != null && context.contains(stringId)) {
			return;
		}
		
		db.execute("update hotspots set checked = false where string_id = ?", stringId);
		
		// invalidate ancestors or users
		Integer parentId = db.queryMaybeInteger("select parent_id from abstract_strings where id = ?", stringId);
		if (parentId != null) {
			invalidateCheckingForDependentStrings(parentId, new IntegerList(stringId, context));
		}
		// String is a pattern iff it doesn't have a parent.
		// In case of patterns, invalidate their users 
		else {
			ResultSet rs = db.query(
				" select id from abstract_strings " +
				" where kind in (" + StringKind.FUNCTION_REF + "," + StringKind.HOTSPOT_REF + ")" +
				" and int_value = ?",  stringId);
			
			try {
				while (rs.next()) {
					invalidateCheckingForDependentStrings(rs.getInt("id"), new IntegerList(stringId, context));
				}
			} 
			catch (SQLException e) {
				throw new RuntimeException(e);
			}
			finally {
				db.checkCloseResult(rs);
			}
		}
	}
	
	private IAbstractString createAbstractString(ResultSet rs, IntegerList context) {
		try {
			// recursion check
			IPosition pos = createPosition(rs);
			if (context != null && pos != null && context.contains(pos.hashCode())) {
				throw new UnsupportedStringOpEx("Cache recursion", pos); 
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
				IntegerList newContext = context;
				if (pos != null) {
					newContext = new IntegerList(pos.hashCode(), context);
				}
				IAbstractString body = createAbstractString(childRs, newContext);
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
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	private ResultSet queryChildren(int parentId) {
		return db.query(
				" select s.*, f.name as file_name" +
				" from abstract_strings s" +
				" left join files f on f.id = s.file_id" +
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
	
	private int addAbstractString(IAbstractString str, Integer parentId, Integer itemIndex) {
		
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
			return addRepetition((StringRepetition)str, parentId, itemIndex);
		}
		else if (str instanceof AbstractStringCollection) {
			return addStringCollection((AbstractStringCollection)str, parentId, itemIndex); 
		}
		else if (str instanceof PatternReference) {
			return addPatternReference((PatternReference)str, parentId, itemIndex);
		}
		else {
			throw new IllegalArgumentException("Unexpected IAbstractString: " + str.getClass());
		}
	}

	private int addRepetition(StringRepetition str, Integer parentId, Integer itemIndex) {
		
		int id = addAbstractStringRecord(StringKind.REPETITION, parentId, itemIndex, 
				null, null, null, str.getPosition());
		addAbstractString(str.getBody(), id, null); 
		return id;
	}
	
	private int addPatternReference(PatternReference str, Integer parentId, Integer itemIndex) {
		
		// publish pattern (if it doesn't exist yet)
		int patternKind = getPatternKind(str);
		Integer patternId = getPatternId(patternKind, 
				str.getPattern().getClassName(), 
				str.getPattern().getMethodName(), 
				str.getPattern().getArgumentTypes(), 
				str.getPattern().getArgumentNo());
		if (patternId == null) {
			this.maxPatternBatchNo += 1;
			patternId = createPatternRecord(patternKind, 
					str.getPattern().getClassName(), 
					str.getPattern().getMethodName(), 
					str.getPattern().getArgumentTypes(), 
					str.getPattern().getArgumentNo(),
					PATTERN_ROLE_SECONDARY, 
					maxPatternBatchNo);
		}
		
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
				addAbstractString(entry.getValue(), id, entry.getKey());
			}
		}
		
		return id;
	}

	private int addStringCollection(AbstractStringCollection str,
			Integer parentId, Integer itemIndex) {
		
		int kind = (str instanceof StringSequence) ? StringKind.SEQUENCE : StringKind.CHOICE; 
		int id = addAbstractStringRecord(kind, parentId, itemIndex, null, null, null, str.getPosition());
		
		int childIndex = 1;
		for (IAbstractString child: str.getItems()) {
			addAbstractString(child, id, childIndex);
			childIndex++;
		}
		
		return id;
	}
	
	private Integer getPatternId(int kind, String className, String methodName, String argumentTypes, int argumentIndex) {
		Integer id = db.queryMaybeInteger(
				" select id from patterns " +
				" where kind = ?" +
				" and class_name = ?" +
				" and method_name = ?" +
				" and argument_types = ?" +
				" and argument_index = ?",
				kind, className, methodName, argumentTypes, argumentIndex);

		return id;		
	}
	
	private int createPatternRecord(int kind, String className, String methodName, String argumentTypes, int argumentIndex,
			int patternRole, int batchNo) {
		// create empty choice for pattern options
		int id = db.insertAndGetId("insert into abstract_strings (kind) " +
				" values (" + StringKind.CHOICE + ")");
		
		// pattern uses same id
		db.execute (
				" insert into patterns (id, kind, class_name, method_name, argument_types, " +
				"     argument_index, pattern_role, batch_no)" +
				" values (?, ?, ?, ?, ?, ?, ?, ?)", 
				id, kind, className, methodName, argumentTypes, argumentIndex, patternRole, batchNo);
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
	
	// TODO should be called, when cache is up-to-date
	// otherwise you delete a pattern but later realize it was still needed
	private void removeOrphanedSecondaryPatterns() {
		// when I delete corresponding abstract string (choice) then pattern and project_pattern
		// records get deleted automatically
		
		// delete those patterns, whose
		
		db.execute(
				" delete from abstract_strings s" +
				" where parent_id is null " + // ie. it's a pattern
				" and not exists (select " +
				" and not exists (");
	}

	public void clearProject() {
		db.execute("delete from files"); 
		fileIDs.clear();
		// hotspots and abstract strings get deleted by cascade
		db.execute("delete from patterns");
		this.maxPatternBatchNo = 0;
	}

	private int getFileId(String name) {
		// first try faster cache
		Integer id = fileIDs.get(name);
		if (id == null) {
			try {
				id = db.queryInt("select id from files where name = ?", name);
				assert id != null; 
				fileIDs.put(name, id);
			} catch (Exception e) {
				throw new IllegalArgumentException("Can't find file: " + name, e);
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
	
	public void updateFilesBatchNo(List<FileRecord> fileRecords, int batchNo) {
		for (FileRecord rec : fileRecords) {
			db.execute("update files set batch_no = ? where id = ?", 
					batchNo, rec.getId());
		}
	}
	
	public boolean projectHasFiles() {
		int fileCount = db.queryInt("select count(*) from files");
		
		return fileCount > 0;
	}
	
	public void printDBInfo() {
		System.out.println("Currenlty open ResultSet-s: " + db.openResultSetCount);
		System.out.println("Max open ResultSet-s so far: " + db.maxOpenResultSetCount);
		System.out.println("Nr of executed queries: " + db.queryCount);
	}
	
}

