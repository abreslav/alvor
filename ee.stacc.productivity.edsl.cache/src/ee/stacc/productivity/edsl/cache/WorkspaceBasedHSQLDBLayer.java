package ee.stacc.productivity.edsl.cache;

public class WorkspaceBasedHSQLDBLayer extends HSQLDBLayer {

	@Override
	protected String getPath() {
		return EDSLCachePlugin.getDefault().getStateLocation().append("/cache").toPortableString();
	}

}
