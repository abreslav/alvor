package ee.stacc.productivity.edsl.cache;



public class CacheService {
	
//	private final static ICacheService SERVICE  = new CacheServiceImpl(new WorkspaceBasedHSQLDBLayer());
	private final static ICacheService SERVICE  = new CacheServiceImpl(new FilesystemBasedHSQLDBLayer());
	
	public static ICacheService getCacheService() {
		return SERVICE;
	}
}
