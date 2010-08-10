package ee.stacc.productivity.edsl.completion;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class SchemaInformation implements ISchemaInformation {

	private SortedSet<String> schemaNames = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
	
	@Override
	public Set<String> getFieldNames(String schema, String table, String prefix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getSchemaNames(String prefix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getTableNames(String table, String prefix) {
		// TODO Auto-generated method stub
		return null;
	}

}
