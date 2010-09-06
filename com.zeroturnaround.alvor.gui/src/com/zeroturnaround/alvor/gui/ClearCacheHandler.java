package com.zeroturnaround.alvor.gui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.zeroturnaround.alvor.cache.CacheService;
import com.zeroturnaround.alvor.crawler.NodeSearchEngine;

public class ClearCacheHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		NodeSearchEngine.clearASTCache();
		CacheService.getCacheService().clearAll();
		return null;
	}


}
