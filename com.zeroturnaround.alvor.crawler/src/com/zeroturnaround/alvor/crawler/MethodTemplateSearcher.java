package com.zeroturnaround.alvor.crawler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import com.zeroturnaround.alvor.cache.CacheService;
import com.zeroturnaround.alvor.common.MethodInvocationDescriptor;
import com.zeroturnaround.alvor.string.IAbstractString;

public class MethodTemplateSearcher extends CachedSearcher<MethodInvocationDescriptor, IAbstractString> {
	private AbstractStringEvaluator evaluator;
	
	public MethodTemplateSearcher(AbstractStringEvaluator evaluator) {
		// needs existing evaluator to keep track of current analysis level
		this.evaluator = evaluator;
	}
	
	@Override
	protected void performSearchInScope(List<IJavaElement> scopeToSearchIn,
			MethodInvocationDescriptor key, List<? super IAbstractString> values) {
		
		IJavaElement[] elems = scopeToSearchIn.toArray(new IJavaElement[scopeToSearchIn.size()]);
		List<MethodDeclaration> decls = NodeSearchEngine.findMethodDeclarations(elems, key.getInvocation());
		for (MethodDeclaration decl : decls) {
			IAbstractString template = evaluator.getMethodTemplate(decl, key.getGetArgIndex()); 
			values.add(template);
			CacheService.getCacheService().getMethodTemplateCache().add(key, template);
		}
	}
	
	public List<IAbstractString> findMethodTemplates(IJavaElement[] scope, 
			MethodInvocation inv, int argPos) {
		
		List<IAbstractString> result = new ArrayList<IAbstractString>();
		MethodInvocationDescriptor invDesc = new MethodInvocationDescriptor(inv, argPos);
		
		performCachedSearch(
				NodeSearchEngine.getAllFilesInScope(scope), 
				CacheService.getCacheService().getMethodTemplateCache(), 
				invDesc, 
				result);
		
		return result;
	}
	
}
