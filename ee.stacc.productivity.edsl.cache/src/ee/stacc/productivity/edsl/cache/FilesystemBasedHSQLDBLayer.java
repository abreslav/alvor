package ee.stacc.productivity.edsl.cache;

public class FilesystemBasedHSQLDBLayer extends HSQLDBLayer {

	@Override
	protected String getPath() {
		return "/media/data/work/STACC/trunk_ws/ee.stacc.productivity.edsl.cache/cache";
	}

}
