package ee.stacc.productivity.edsl.completion;

import java.util.Set;

public interface ISchemaInformation {

	Set<String> getSchemaNames(String prefix);
	Set<String> getTableNames(String table, String prefix);
	Set<String> getFieldNames(String schema, String table, String prefix);
	
}
