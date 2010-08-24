/**
 * 
 */
package com.zeroturnaround.alvor.cache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.string.AbstractStringCollection;
import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.IAbstractStringVisitor;
import com.zeroturnaround.alvor.string.IPosition;
import com.zeroturnaround.alvor.string.Position;
import com.zeroturnaround.alvor.string.StringCharacterSet;
import com.zeroturnaround.alvor.string.StringChoice;
import com.zeroturnaround.alvor.string.StringConstant;
import com.zeroturnaround.alvor.string.StringParameter;
import com.zeroturnaround.alvor.string.StringRepetition;
import com.zeroturnaround.alvor.string.StringSequence;

public final class CacheServiceImpl implements ICacheService {
	private static interface StringTypes {
		int CONSTANT = 0;
		int CHAR_SET = 1;
		int SEQUENCE = 2;
		int CHOICE = 3;
		int REPETITION = 4;
		int SAME_AS = 5;
		int UNSUPPORTED = 6;
		int PARAMETER = 7;
	}
	
	private final static ILog LOG = Logs.getLog(ICacheService.class);
	private static final Integer MANY_PARENTS = -1;

	private class DBQueries {
		private final PreparedStatement queryGetAbstractString;
		private final PreparedStatement queryGetMethod;
		private final PreparedStatement queryGetSignature;
		private final PreparedStatement queryRemoveFile;
		
		public DBQueries() throws SQLException {
	        queryGetAbstractString = connection.prepareStatement(
		    		"SELECT AbstractStrings.id AS stringId, type, a FROM AbstractStrings WHERE sourceRange = " + 
		    		"	(SELECT id FROM SourceRanges WHERE" + 
		    		"	(file = (SELECT id FROM Files WHERE name = ?))" + 
		    		"	AND (start = ?) AND (length = ?))"
		    );
	        
	        queryGetMethod = connection.prepareStatement(
	        		"SELECT id FROM Methods WHERE (class = ?) AND (name = ?)"
	        );

	        queryGetSignature = connection.prepareStatement(
	        		"SELECT id FROM Signatures WHERE signature = ?"
	        );

	        queryRemoveFile = connection.prepareStatement(
					"DELETE FROM Files WHERE name = ?"
			);
	        
		}

		
		public PreparedStatement getAbstractStringQuery(IPosition position) throws SQLException {
			return getAbstractStringQuery(position.getPath(), position.getStart(), position.getLength());
		}
		
		public PreparedStatement getAbstractStringQuery(String filePath, int start, int length) throws SQLException {
			queryGetAbstractString.setString(1, filePath);
			queryGetAbstractString.setInt(2, start);
			queryGetAbstractString.setInt(3, length);

			return queryGetAbstractString;
		}

		public PreparedStatement getMethodQuery(IHotspotPattern pattern) throws SQLException {
			queryGetMethod.setString(1, pattern.getClassName());
			queryGetMethod.setString(2, pattern.getMethodName());

			return queryGetMethod;
		}
		
		public PreparedStatement getSignatureQuery(String signature) throws SQLException {
			queryGetSignature.setString(1, signature);

			return queryGetSignature;
		}
		
		public PreparedStatement getRemoveFileQuery(String path) throws SQLException {
			queryRemoveFile.setString(1, path);
			
			return queryRemoveFile;
		}
		
		public PreparedStatement getRemoveFilesQuery(Set<String> paths) throws SQLException {
			for (String path : paths) {
				queryRemoveFile.setString(1, path);
				queryRemoveFile.addBatch();
			}
			
			return queryRemoveFile;
		}
		
	}
	
	private final IDBLayer dbLayer;
	private final DBQueries queries;
	private final Connection connection;
	private final IScopedCache<IHotspotPattern, IPosition> usageCache = new UsageCache();	
	private final IScopedCache<MethodInvocationDescriptor, IAbstractString> methodTemplateCache = new MethodTemplateCache();	

	boolean nocache = false;
	
	public void setNocache(boolean value) {
		nocache = value;
	}
	
	public CacheServiceImpl(IDBLayer dbLayer) {
		this.dbLayer = dbLayer; 
	    try {
	        connection = dbLayer.connect();
	        
	        queries = new DBQueries();
	        
	    } catch (Exception e) { // Yes, we catch Exception here. But it is immediately rethrown inside an ISE
	    	LOG.exception(e);
	    	throw new IllegalStateException(e);
	    }
	    
	}

	protected void finalize() throws Throwable {
		shutdown();		
	}

	public void shutdown() {
		try {
			dbLayer.shutdown();
		} catch (SQLException e) {
			LOG.exception(e);
			throw new IllegalStateException(e);
		}
	}
	
	@Override
	public void addAbstractString(IPosition position,
			IAbstractString abstractString) {
		if (nocache)
			return;
		try {
			Integer id = getAbstractStringIdByPosition(position);
			if (id != null) {
				return;
			}
			
			createAbstractStringRecords(abstractString, position);
			
		} catch (SQLException e) {
			LOG.exception(e);
		} 
	}

	@Override
	public void addUnsupported(IPosition position, String message) {
		if (nocache)
			return;
		try {
			Integer id = getAbstractStringIdByPosition(position);
			if (id != null) {
				return;
			}
			
			createUnsupported(message, position);
			
		} catch (SQLException e) {
			LOG.exception(e);
		} catch (StackOverflowError e) {
			throw new RuntimeException("SOE");
		}
	}	
	
	private void createUnsupported(String message, IPosition position) throws SQLException {
		int rangeId = createSourceRange(position);
		int messageId = createUnsupportedEntry(message);
		
		PreparedStatement preparedStatement = connection.prepareStatement(
				"INSERT INTO AbstractStrings(type, a, sourceRange) VALUES (6, ?, ?)",
				Statement.RETURN_GENERATED_KEYS
		);
		preparedStatement.setInt(1, messageId);
		preparedStatement.setInt(2, rangeId);
		
		preparedStatement.executeUpdate();
	}

	private int createUnsupportedEntry(String message)
			throws SQLException {
		PreparedStatement insert = connection.prepareStatement(
				"INSERT INTO Unsupported(message) VALUES (?)",
				Statement.RETURN_GENERATED_KEYS
		);
		insert.setString(1, message);
		return insertAndGetId(insert);
	}
	
	/*
	 * If position is null, it must be taken from the string
	 */
	private int createAbstractStringRecords(IAbstractString abstractString,
			final IPosition position) throws SQLException {

		Integer idByPosition = getAbstractStringIdByPosition(abstractString.getPosition());
		if (idByPosition != null) {
			if (position == null || position.equals(abstractString.getPosition())) {
				return idByPosition;
			}
			// TODO: why? 
			return createSame(idByPosition, position);
		}
		
		IAbstractStringVisitor<Integer, Void> visitor = new IAbstractStringVisitor<Integer, Void>() {

			@Override
			public Integer visitStringCharacterSet(
					StringCharacterSet characterSet, Void data) {
				try {
					return createStringCharacterSet(characterSet);
				} catch (SQLException e) {
					throw new RethrowException(e);
				}
			}

			@Override
			public Integer visitStringChoice(StringChoice stringChoice,
					Void data) {
				try {
					return createStringChoice(stringChoice);
				} catch (SQLException e) {
					throw new RethrowException(e);
				}
			}

			@Override
			public Integer visitStringConstant(
					StringConstant stringConstant, Void data) {
				try {
					return createStringConstant(stringConstant);
				} catch (SQLException e) {
					throw new RethrowException(e);
				}
			}

			@Override
			public Integer visitStringRepetition(
					StringRepetition stringRepetition, Void data) {
				IAbstractString body = stringRepetition.getBody();
				try {
					int bodyId = createAbstractStringRecords(body, null);
					return createFromTypeAndA(StringTypes.REPETITION, stringRepetition, bodyId);
				} catch (SQLException e) {
					throw new RethrowException(e);
				}
			}

			@Override
			public Integer visitStringSequence(
					StringSequence stringSequence, Void data) {
				try {
					return createStringSequence(stringSequence);
				} catch (SQLException e) {
					throw new RethrowException(e);
				}
			}

			@Override
			public Integer visitStringParameter(
					StringParameter stringParameter, Void data) {
				try {
					return createFromTypeAndA(StringTypes.PARAMETER, stringParameter, stringParameter.getIndex());
				} catch (SQLException e) {
					throw new RethrowException(e);
				}
			}


		};
		try {
			return abstractString.accept(visitor, null);
		} catch (RethrowException e) {
			throw e.getCause();
		}
	}
	
	private Integer createFromTypeAndA(int type,
			IAbstractString abstractString, int a) throws SQLException {
		int rangeId = createSourceRange(abstractString.getPosition());
		
		PreparedStatement preparedStatement = connection.prepareStatement(
				"INSERT INTO AbstractStrings(type, a, sourceRange) VALUES (?, ?, ?)",
				Statement.RETURN_GENERATED_KEYS
		);
		preparedStatement.setInt(1, type);
		preparedStatement.setInt(2, a);
		preparedStatement.setInt(3, rangeId);
		
		return insertAndGetId(preparedStatement);
	}
	
	private Integer createStringCharacterSet(
			StringCharacterSet characterSet) throws SQLException {
		if (characterSet.getContents().size() > 10) {
			throw new IllegalArgumentException("Character set is too big " + characterSet);
		}
		
		int rangeId = createSourceRange(characterSet.getPosition());
		int constantId = createCharacterSetEntry(characterSet);
		
		PreparedStatement preparedStatement = connection.prepareStatement(
				"INSERT INTO AbstractStrings(type, a, sourceRange) VALUES (1, ?, ?)",
				Statement.RETURN_GENERATED_KEYS
		);
		preparedStatement.setInt(1, constantId);
		preparedStatement.setInt(2, rangeId);
		
		return insertAndGetId(preparedStatement);
	}

	private int createCharacterSetEntry(StringCharacterSet characterSet) throws SQLException {
		PreparedStatement query = connection.prepareStatement(
				"SELECT id FROM CharacterSets WHERE (data = ?)"
		);
		String string = makeString(characterSet.getContents());
		query.setString(1, string);
		
		PreparedStatement insert = connection.prepareStatement(
				"INSERT INTO CharacterSets(data) VALUES (?)",
				Statement.RETURN_GENERATED_KEYS
		);
		insert.setString(1, string);
		return insertIfNotYet(query, insert);
	}

	private String makeString(Set<Character> contents) {
		StringBuilder b = new StringBuilder();
		for (Character character : contents) {
			b.append(character);
		}
		return b.toString();
	}

	private int createSame(Integer id, IPosition position) throws SQLException {
		int rangeId = createSourceRange(position);
		PreparedStatement preparedStatement = connection.prepareStatement(
				"INSERT INTO AbstractStrings(type, a, sourceRange) VALUES ("
					+ StringTypes.SAME_AS + ", ?, ?)", Statement.RETURN_GENERATED_KEYS);
		preparedStatement.setInt(1, id);
		preparedStatement.setInt(2, rangeId);
		
		return insertAndGetId(preparedStatement);
	}

	private int createStringSequence(StringSequence stringSequence) throws SQLException {
		return createStringCollection(stringSequence, StringTypes.SEQUENCE);			
	}

	private int createStringChoice(StringChoice stringChoice) throws SQLException {
		return createStringCollection(stringChoice, StringTypes.CHOICE);			
	}
	
	private int createStringCollection(AbstractStringCollection stringSequence,
			int type) throws SQLException {
		int rangeId = createSourceRange(stringSequence.getPosition());
		
		PreparedStatement preparedStatement = connection.prepareStatement(
				"INSERT INTO AbstractStrings(type, sourceRange) VALUES (?, ?)",
				Statement.RETURN_GENERATED_KEYS
		);
		preparedStatement.setInt(1, type);
		preparedStatement.setInt(2, rangeId);
		
		int collectionId = insertAndGetId(preparedStatement);
		
		
		PreparedStatement insertContents = connection.prepareStatement(
				"INSERT INTO CollectionContents(collection, item, index) VALUES (?, ?, ?)"
		);
		int index = 0;
		for (IAbstractString item : stringSequence.getItems()) {
			insertContents.setInt(1, collectionId);
			insertContents.setInt(2, createAbstractStringRecords(item, null));
			insertContents.setInt(3, index);
			insertContents.executeUpdate();
			index++;
		}
		
		return collectionId;
	}

	private int createStringConstant(
			StringConstant stringConstant) throws SQLException {
		int rangeId = createSourceRange(stringConstant.getPosition());
		int constantId = createConstantEntry(stringConstant);
		
		PreparedStatement preparedStatement = connection.prepareStatement(
				"INSERT INTO AbstractStrings(type, a, sourceRange) VALUES (0, ?, ?)",
				Statement.RETURN_GENERATED_KEYS
		);
		preparedStatement.setInt(1, constantId);
		preparedStatement.setInt(2, rangeId);
		
		return insertAndGetId(preparedStatement);
	}

	private int createConstantEntry(StringConstant stringConstant)
			throws SQLException {
		PreparedStatement query = connection.prepareStatement(
				"SELECT id FROM StringConstants WHERE (escapedValue = ?)"
		);
		query.setString(1, stringConstant.getEscapedValue());
		
		PreparedStatement insert = connection.prepareStatement(
				"INSERT INTO StringConstants(literalValue, escapedValue) VALUES (?, ?)",
				Statement.RETURN_GENERATED_KEYS
		);
		insert.setString(1, stringConstant.getConstant());
		insert.setString(2, stringConstant.getEscapedValue());
		return insertIfNotYet(query, insert);
	}
	
	private int createSourceRange(IPosition position) throws SQLException {
		int fileId = createFile(position.getPath());
		PreparedStatement query = connection.prepareStatement(
				"SELECT id FROM SourceRanges WHERE (file = ?) AND (start = ?) AND (length = ?)"
		);
		query.setInt(1, fileId);
		query.setInt(2, position.getStart());
		query.setInt(3, position.getLength());
		
		PreparedStatement insert = connection.prepareStatement(
				"INSERT INTO SourceRanges(file, start, length) VALUES (?, ?, ?)",
				Statement.RETURN_GENERATED_KEYS
		);
		insert.setInt(1, fileId);
		insert.setInt(2, position.getStart());
		insert.setInt(3, position.getLength());
		
		return insertIfNotYet(query, insert);
	}

	private int createFile(String path) throws SQLException {
		PreparedStatement preparedQuery = getFileStatement(path);

		PreparedStatement preparedInsert = connection.prepareStatement(
				"INSERT INTO Files(name) VALUES (?)",
				Statement.RETURN_GENERATED_KEYS
		);
		preparedInsert.setString(1, path);

		return insertIfNotYet(preparedQuery, preparedInsert);
	}

	private PreparedStatement getFileStatement(String path) throws SQLException {
		PreparedStatement preparedQuery = connection.prepareStatement(
				"SELECT id FROM Files WHERE name = ?"
		);
		preparedQuery.setString(1, path);
		return preparedQuery;
	}

	private int insertIfNotYet(PreparedStatement preparedQuery,
			PreparedStatement preparedInsert) throws SQLException {
		ResultSet res = preparedQuery.executeQuery();
		if (res.next()) {
			return notNull(res, res.getInt("id"));
		}
		
		return insertAndGetId(preparedInsert);
	}

	private int insertAndGetId(PreparedStatement preparedInsert)
			throws SQLException {
		ResultSet res;
		int rows = preparedInsert.executeUpdate();
		if (rows != 1) {
			throw new IllegalStateException();
		}
		
		res = preparedInsert.getGeneratedKeys();
		if (res.next()) {
			return res.getInt(1);
		}
		throw new IllegalStateException();
	}

	private Integer getAbstractStringIdByPosition(IPosition position) throws SQLException {
		PreparedStatement preparedStatement = queries.getAbstractStringQuery(position);
		ResultSet res = preparedStatement.executeQuery();
		if (!res.next()) {
			return null;
		}
		
		return notNull(res, res.getInt("stringId"));
	}

	@Override
	public IAbstractString getAbstractString(IPosition position) {
		if (nocache)
			return null;
		try {
			return runStringConstruction(queries.getAbstractStringQuery(position), position);
		} catch (SQLException e) {
			LOG.exception(e);
			return null;
		} catch (StackOverflowError e) {
			throw new RuntimeException("SOE: " + position.getPath() + ":" + position.getStart());
		}
	}

	private IAbstractString runStringConstruction(
			PreparedStatement preparedStatement, IPosition position) throws SQLException {
		ResultSet res = preparedStatement.executeQuery();
		if (!res.next()) {
			return null;
		}
		
		return fetchAbstractString(position, res);
	}

	private IAbstractString fetchAbstractString(IPosition position,
			ResultSet res) throws SQLException {
		int id = notNull(res, res.getInt("stringId"));
		int type = notNull(res, res.getInt("type"));
		Integer a = mayBeNull(res, res.getInt("a"));

		if (position == null) {
			position = fetchPosition(res);
		}
		
		return constructAbstractString(id, type, a, position);
	}

	private IAbstractString constructAbstractString(int id, int type, Integer a, IPosition position) throws SQLException {
		switch (type) {
		case StringTypes.CONSTANT:
			return getStringConstant(a, position); 
		case StringTypes.CHAR_SET:
			return getStringCharacterSet(a, position); 
		case StringTypes.SEQUENCE:
			return new StringSequence(position,	getCollectionContents(id));
		case StringTypes.CHOICE:
			return new StringChoice(position, getCollectionContents(id));
		case StringTypes.REPETITION:
			return new StringRepetition(position, getAbstractStringById(a));
		case StringTypes.SAME_AS:
			return getAbstractStringById(a);
		case StringTypes.PARAMETER:
			return new StringParameter(position, a);
		case StringTypes.UNSUPPORTED:
			throwUnsupportedStringOpEx(position, a);
		default:
			throw new IllegalStateException("Unknown AbstractString type: " + type);
		}
	}

	private IAbstractString getStringCharacterSet(Integer id, IPosition position) throws SQLException {
		PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT data FROM CharacterSets " + 
				"WHERE id = ?"
		);
		preparedStatement.setInt(1, id);
		ResultSet res = preparedStatement.executeQuery();
		if (!res.next()) {
			return null;
		}
		
		return new StringCharacterSet(
				position, 
				notNull(res, res.getString("data")));
	}

	private void throwUnsupportedStringOpEx(IPosition position, Integer id) throws SQLException {
		PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT message FROM Unsupported WHERE id = ? "
		);
		preparedStatement.setInt(1, id);
		
		ResultSet res = preparedStatement.executeQuery();
		if (!res.next()) {
			throw new IllegalArgumentException("No record in the DB");
		}
		
		String message = notNull(res, res.getString("message"));
		throw new UnsupportedStringOpEx(message);
	}

	private List<IAbstractString> getCollectionContents(int collectionId) throws SQLException {
		PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT AbstractStrings.id AS stringId, type, a, name, start, length FROM CollectionContents " +
				"	LEFT JOIN AbstractStrings ON (item = AbstractStrings.id) " +
				"	LEFT JOIN SourceRanges ON (sourceRange = SourceRanges.id) " +
				"	LEFT JOIN Files ON (SourceRanges.file = Files.id) " +
				"WHERE collection = ? " +
				"ORDER BY CollectionContents.index ASC"
		);
		preparedStatement.setInt(1, collectionId);
		
		ResultSet res = preparedStatement.executeQuery();
		if (!res.next()) {
			return Collections.emptyList();
		}
		
		List<IAbstractString> result = new ArrayList<IAbstractString>();
		do {
			result.add(fetchAbstractString(null, res));
		} while (res.next());
		return result;
	}

	private IAbstractString getAbstractStringById(int id) throws SQLException {
		PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT AbstractStrings.id as stringId, type, a, name, start, length FROM AbstractStrings " +
				"	LEFT JOIN SourceRanges ON (sourceRange = SourceRanges.id)" +
				"	LEFT JOIN Files ON (SourceRanges.file = Files.id)" +
				"WHERE AbstractStrings.id = ?"
		);
		preparedStatement.setInt(1, id);
		return runStringConstruction(preparedStatement, null);
	}

	private IAbstractString getStringConstant(int id, IPosition position) throws SQLException {
		PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT literalValue, escapedValue FROM StringConstants " + 
				"WHERE StringConstants.id = ?"
		);
		preparedStatement.setInt(1, id);
		ResultSet res = preparedStatement.executeQuery();
		if (!res.next()) {
			return null;
		}
		
		return new StringConstant(
				position, 
				notNull(res, res.getString("literalValue")), 
				notNull(res, res.getString("escapedValue")));
	}

	private IPosition fetchPosition(ResultSet res) throws SQLException {
		return new Position(
				notNull(res, res.getString("name")),
				notNull(res, res.getInt("start")), 
				notNull(res, res.getInt("length"))
		);
	}

	private <T> T notNull(ResultSet resultSet, T value) throws SQLException {
		if (resultSet.wasNull()) {
			throw new IllegalStateException();
		}
		return value;
	}

	private <T> T mayBeNull(ResultSet resultSet, T value) throws SQLException {
		if (resultSet.wasNull()) {
			return null;
		}
		return value;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public IScopedCache<IHotspotPattern, IPosition> getHotspotCache() {
		return usageCache;
	}

	
	private final class MethodTemplateCache implements IScopedCache<MethodInvocationDescriptor, IAbstractString> {
		
		@Override
		public void add(MethodInvocationDescriptor desc, IAbstractString str) {
			if (nocache) 
				return;
			try {
				int stringId = createAbstractStringRecords(str, str.getPosition());
				int signatureId = createSignature(desc.getSignature());
				
				PreparedStatement insert = connection.prepareStatement(
						"INSERT INTO MethodImplementations(signature, abstractstring) VALUES (?, ?)"
				);
				insert.setInt(1, signatureId);
				insert.setInt(2, stringId);
				
				insert.executeUpdate();
			} catch (SQLException e) {
				LOG.exception(e);
				throw new RethrowException(e);
			}
		}
		
		private int createSignature(String signature) throws SQLException {
			PreparedStatement query = queries.getSignatureQuery(signature);
			PreparedStatement insert = connection.prepareStatement(
					"INSERT INTO Signatures(signature) VALUES (?)",
					Statement.RETURN_GENERATED_KEYS
			);
			insert.setString(1, signature);
			return insertIfNotYet(query, insert);
		}
		
		@Override
		public void getCachedResultsInScope(Set<Integer> scope, MethodInvocationDescriptor desc,
				Collection<? super IAbstractString> values) {
			if (nocache) 
				return;
			try {
				PreparedStatement stmt = connection.prepareStatement(
						"SELECT a.id as stringId, a.type, a.a, name, start, length " +
						"   FROM Signatures s" +
						"   JOIN MethodImplementations mi ON (mi.signature = s.id) " +
						"   JOIN AbstractStrings a        ON (a.id = mi.abstractString) " +
						"	LEFT JOIN SourceRanges sr     ON (sr.id = a.sourceRange) " +
						"	LEFT JOIN Files f             ON (f.id = sr.file) " +
						"WHERE s.signature = ?"
				);
				stmt.setString(1, desc.getSignature());
				ResultSet res = stmt.executeQuery();
				
				while (res.next()) {
					values.add(fetchAbstractString(null, res));
				}
			} catch (SQLException e) {
				LOG.exception(e);
				throw new RethrowException(e);
			}
		}
		
		@Override
		public Map<String, Integer> getCachedScope(MethodInvocationDescriptor desc) {
			if (nocache) 
				return Collections.emptyMap();
			try {
				PreparedStatement query = connection.prepareStatement(
						"SELECT f.name, f.id AS fileId " +
						"   FROM Signatures s" +
						"   JOIN MethodImplementationScope mis ON (mis.signature = s.id)" +
						"	JOIN Files f ON (f.id = mis.file)" +
						" WHERE (s.signature = ?)"
				);
				query.setString(1, desc.getSignature());
				ResultSet res = query.executeQuery();
				
				Map<String, Integer> result = new HashMap<String, Integer>();
				
				while (res.next()) {
					result.put(
						notNull(res, res.getString("name")), 
						notNull(res, res.getInt("fileId")));
				}
				
				return result;
			} catch (SQLException e) {
				LOG.exception(e);
				throw new RethrowException(e);
			}
		}
		
		@Override
		public void markScopeAsCached(MethodInvocationDescriptor desc, Set<String> scope) {
			if (nocache) 
				return;
			try {
				PreparedStatement query = queries.getSignatureQuery(desc.getSignature());
				ResultSet res = query.executeQuery();
				if (!res.next()) {
					LOG.error("Signature not found " + desc.getSignature());
					return;
				}
				
				int signatureId = notNull(res, res.getInt("id"));

				PreparedStatement insert = connection.prepareStatement(
						"INSERT INTO MethodImplementationScope(signature, file) VALUES (?, ?)"
				);
				for (String name : scope) {
					insert.setInt(1, signatureId);
					insert.setInt(2, createFile(name));
					insert.addBatch();
				}
				insert.executeBatch();
			} catch (SQLException e) {
				LOG.exception(e);
				throw new RethrowException(e);
			}
		}
	}

	
	private final class UsageCache implements IScopedCache<IHotspotPattern, IPosition> {

		@Override
		public void add(IHotspotPattern pattern, IPosition position) {
			if (nocache) 
				return;
			try {
				int rangeId = createSourceRange(position);
				int methodId = createMethod(pattern);
				PreparedStatement insert = connection.prepareStatement(
						"INSERT INTO Hotspots(method, argumentIndex, sourceRange) VALUES (?, ?, ?)"
				);
				insert.setInt(1, methodId);
				insert.setInt(2, pattern.getArgumentIndex());
				insert.setInt(3, rangeId);
				
				insert.executeUpdate();
			} catch (SQLException e) {
				LOG.exception(e);
				throw new RethrowException(e);
			}
		}
		
		private int createMethod(IHotspotPattern pattern) throws SQLException {
			PreparedStatement query = queries.getMethodQuery(pattern);
			PreparedStatement insert = connection.prepareStatement(
					"INSERT INTO Methods(class, name) VALUES (?, ?)",
					Statement.RETURN_GENERATED_KEYS
			);
			insert.setString(1, pattern.getClassName());
			insert.setString(2, pattern.getMethodName());
			return insertIfNotYet(query, insert);
		}

		@Override
		public void getCachedResultsInScope(Set<Integer> scope,
				IHotspotPattern pattern, Collection<? super IPosition> result) {
			if (nocache) 
				return;
			try {
				PreparedStatement query = connection.prepareStatement(
						"SELECT Files.name AS name, start, length, Files.id AS fileId FROM Hotspots" +
						"	LEFT JOIN Methods ON (method = Methods.id)" +
						"	LEFT JOIN SourceRanges ON (sourceRange = SourceRanges.id)" +
						"	LEFT JOIN Files ON (file = Files.id)" +
						" WHERE (Methods.class = ?) AND (Methods.name = ?) AND (Hotspots.argumentIndex = ?)"
				);
				query.setString(1, pattern.getClassName());
				query.setString(2, pattern.getMethodName());
				query.setInt(3, pattern.getArgumentIndex());
				
				ResultSet res = query.executeQuery();
				
				while (res.next()) {
					Integer fileId = notNull(res, res.getInt("fileId"));
					if (scope.contains(fileId)) {
						String name = notNull(res, res.getString("name"));
						int start = notNull(res, res.getInt("start"));
						int length = notNull(res, res.getInt("length"));
						result.add(new Position(name, start, length));
					}
				}
			} catch (SQLException e) {
				LOG.exception(e);
				throw new RethrowException(e);
			}
		}

		@Override
		public Map<String, Integer> getCachedScope(IHotspotPattern pattern) {
			if (nocache) 
				return Collections.emptyMap();
			try {
				PreparedStatement query = connection.prepareStatement(
						"SELECT Files.name AS name, Files.id AS fileId FROM MethodUsageScope" +
						"	LEFT JOIN Methods ON (method = Methods.id)" +
						"	LEFT JOIN Files ON (file = Files.id)" +
						" WHERE (Methods.class = ?) AND (Methods.name = ?)"
				);
				query.setString(1, pattern.getClassName());
				query.setString(2, pattern.getMethodName());
				
				ResultSet res = query.executeQuery();
				
				Map<String, Integer> result = new HashMap<String, Integer>();
				
				while (res.next()) {
					result.put(
						notNull(res, res.getString("name")), 
						notNull(res, res.getInt("fileId")));
				}
				
				return result;
			} catch (SQLException e) {
				LOG.exception(e);
				throw new RethrowException(e);
			}
		}

		@Override
		public void markScopeAsCached(IHotspotPattern pattern, Set<String> scope) {
			if (nocache) 
				return;
			try {
				PreparedStatement query = queries.getMethodQuery(pattern);
				
				ResultSet res = query.executeQuery();

				if (!res.next()) {
					LOG.error("Method not found " + pattern);
					return;
				}
				
				int methodId = notNull(res, res.getInt("id"));

				PreparedStatement insert = connection.prepareStatement(
						"INSERT INTO MethodUsageScope(method, file) VALUES (?, ?)"
				);
				for (String name : scope) {
					insert.setInt(1, methodId);
					insert.setInt(2, createFile(name));
					insert.addBatch();
				}
				insert.executeBatch();
			} catch (SQLException e) {
				LOG.exception(e);
				throw new RethrowException(e);
			}
		}

	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public IScopedCache<MethodInvocationDescriptor, IAbstractString> getMethodTemplateCache() {
		return methodTemplateCache;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void clearAll() {
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(
					"DELETE FROM Files"
			);
			
			preparedStatement.executeUpdate();
			
			cleanup();
		} catch (SQLException e) {
			LOG.exception(e);
			throw new RethrowException(e);
		}
	}
	
	@Override
	public void removeFile(String path) {
		try {
			PreparedStatement preparedStatement = queries.getRemoveFileQuery(path);
			
			preparedStatement.executeUpdate();
			
			cleanup();
		} catch (SQLException e) {
			LOG.exception(e);
			throw new RethrowException(e);
		}
	}
	
	@Override
	public void removeFiles(Set<String> paths) {
		try {
			if (paths.isEmpty()) {
				return;
			}
			
			PreparedStatement preparedStatement = queries.getRemoveFilesQuery(paths);
			
			preparedStatement.executeBatch();
			
			cleanup();
			
		} catch (SQLException e) {
			LOG.exception(e);
			throw new RethrowException(e);
		}
	}
	
	private void cleanup() throws SQLException {
		
		Statement batch = connection.createStatement();
		
		// unreferenced string constants
		batch.addBatch(  
			"DELETE FROM StringConstants WHERE id IN (" +
			"	SELECT id FROM StringConstants" + 
			"		LEFT JOIN (SELECT a FROM AbstractStrings WHERE type = 0)" + 
			"			ON (a = StringConstants.id)" +
			"	WHERE a IS NULL" +
			")"
		);
		// unreferenced character sets
		batch.addBatch(
			"DELETE FROM CharacterSets WHERE id IN (" +
			"	SELECT id FROM CharacterSets " +
			"		LEFT JOIN (SELECT a FROM AbstractStrings WHERE type = 1)" + 
			"			ON (a = CharacterSets.id)" +
			"	WHERE a IS NULL" +
			")"
		);
		// unreferenced unsupported stuff
		batch.addBatch(
			"DELETE FROM Unsupported WHERE id IN (" +
			"	SELECT id FROM Unsupported " +
			"		LEFT JOIN (SELECT a FROM AbstractStrings WHERE type = 6)" + 
			"			ON (a = Unsupported.id)" +
			"	WHERE a IS NULL" +
			")"
		);
		int[] res = batch.executeBatch();
		if (res.length != 3) {
			LOG.error("CLEANUP FAILED!");
		}
		
		// CollectionContents is cleaned using triggers
	}

	/**
	 * Returns hotspots that don't have corresponding abstract string in cache
	 */
	@Override
	public Collection<IPosition> getInvalidatedHotspotPositions() {
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(
					"SELECT name, start, length FROM Hotspots " +
					"	LEFT JOIN AbstractStrings ON (Hotspots.sourceRange = AbstractStrings.sourceRange)" +
					"	LEFT JOIN SourceRanges ON (Hotspots.sourceRange = SourceRanges.id)" +
					"	LEFT JOIN Files ON (file = Files.id)" +
					"WHERE AbstractStrings.id IS NULL"
			);
			
			ResultSet res = preparedStatement.executeQuery();
			
			Collection<IPosition> result = new ArrayList<IPosition>();
			while (res.next()) {
				result.add(new Position(
						notNull(res, res.getString("name")),
						notNull(res, res.getInt("start")),
						notNull(res, res.getInt("length"))
				));
			}
			
			return result;
		} catch (SQLException e) {
			LOG.exception(e);
			throw new RethrowException(e);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public IAbstractString getContainingAbstractString(String path,
			int offset) {
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(
					"	SELECT MAX(start) AS ms, MIN(length) AS ml  " +
					"		FROM SourceRanges " +
					"		WHERE start = (" +
					"			SELECT MAX(start) " +
					"				FROM SourceRanges " +
					"					LEFT JOIN Files ON (file = Files.id)" +
					"				WHERE (start < ?) AND (? - start <= length) AND (name = ?)" +
					"		)" +
					""
			);
			preparedStatement.setInt(1, offset);
			preparedStatement.setInt(2, offset);
			preparedStatement.setString(3, path);
			
			ResultSet res = preparedStatement.executeQuery();
			if (!res.next()) {
				return null;
			}
			
			Integer start = mayBeNull(res, res.getInt("ms"));
			if (start == null) {
				return null;
			}
			
			Integer length = mayBeNull(res, res.getInt("ml"));
			if (length == null) {
				return null;
			}

			Integer id = getAbstractStringIdByPosition(new Position(path, start, length));
			if (id == null) {
				return null;
			}
			
			return findOutermostAbstractString(id);
		} catch (SQLException e) {
			LOG.exception(e);
			throw new RethrowException(e);
		}
	}
	
	private IAbstractString findOutermostAbstractString(int id) throws SQLException {
		while (true) {
			Integer parent = getParent(id);
//			System.out.println(parent);
			if (parent == null) {
				return getAbstractStringById(id);
			}
			if (parent == MANY_PARENTS) {
				return null;
			}
//			System.out.println(getAbstractStringById(parent));
//			System.out.println("is a parent for");
//			System.out.println(getAbstractStringById(id));
			if (id == parent) {
				throw new IllegalStateException();
			}
			id = parent;
		}
	}
	
	private Integer getParent(int id) throws SQLException {
		// look in CollectionContents
		//		find parent strings
		//			if many, fail MANY_PARENTS

		PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT collection FROM CollectionContents WHERE item = ?"
		);
		preparedStatement.setInt(1, id);
		ResultSet res = preparedStatement.executeQuery();
		Integer result = null;
		if (res.next()) {
			result = notNull(res, res.getInt("collection"));
			if (res.next()) {
//				System.out.println("dup: " + getAbstractStringById(result));
//				System.out.println(getAbstractStringById(notNull(res, res.getInt("collection"))));
				return MANY_PARENTS;
			}
		}
		
		// for types 0 1 4 6 look in AbstractStrings
		//		find parent strings
		//			if many, fail MANY_PARENTS
		preparedStatement = connection.prepareStatement(
				"SELECT id, type FROM AbstractStrings WHERE (a = ?) AND (type IN (0, 1, 4, 5, 6))"
		);
		preparedStatement.setInt(1, id);
		res = preparedStatement.executeQuery();
		Integer psr = null;
		while (res.next()) {
			boolean parentlessSame = isParentlessSame(res);
			if (result != null && !parentlessSame) {
//				System.out.println("dup: " + getAbstractStringById(result));
//				System.out.println(getAbstractStringById(notNull(res, res.getInt("id"))));
				return MANY_PARENTS;
			}
			if (parentlessSame) {
				psr = result;
			} else {
				result = notNull(res, res.getInt("id"));
			}
		}
		if (result == null) {
			result = psr;
		}

		return result;
	}

	private boolean isParentlessSame(ResultSet res) throws SQLException {
		int type = notNull(res, res.getInt("type"));
		boolean parentlessSame = false;
		if (type == StringTypes.SAME_AS) {
			int sameId = notNull(res, res.getInt("id"));
			parentlessSame = getParent(sameId) == null;
		}
		return parentlessSame;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void dumpLog() {
		try {
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Log");
			ResultSet res = preparedStatement.executeQuery();
			while (res.next()) {
				for (int i = 1; i <= res.getMetaData().getColumnCount(); i++) {
					System.out.print(res.getObject(i) + " ");
				}
				System.out.println();
			}
			connection.prepareStatement("DELETE FROM Log").execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
	@SuppressWarnings("serial")
	private class RethrowException extends RuntimeException {

		public RethrowException(SQLException cause) {
			super(cause);
		}
		
		public SQLException getCause() {
			return (SQLException) super.getCause();
		}
		
	}

}
