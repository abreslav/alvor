package ee.stacc.productivity.edsl.gui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import ee.stacc.productivity.edsl.cache.CacheService;
import ee.stacc.productivity.edsl.crawler.NodeSearchEngine;

public class ClearCacheHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		NodeSearchEngine.clearCache();
		CacheService.getCacheService().clearAll();
		return null;
	}


}
