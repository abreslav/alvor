package com.zeroturnaround.alvor.gui.debug;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import com.zeroturnaround.alvor.cache.Cache;
import com.zeroturnaround.alvor.cache.CacheProvider;
import com.zeroturnaround.alvor.common.HotspotDescriptor;
import com.zeroturnaround.alvor.common.StringHotspotDescriptor;
import com.zeroturnaround.alvor.common.UnsupportedHotspotDescriptor;
import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.crawler.StringExpressionEvaluator;
import com.zeroturnaround.alvor.crawler.util.ASTUtil;
import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.IPosition;
import com.zeroturnaround.alvor.string.Position;

public class ShowValueFromCacheHandler implements IEditorActionDelegate {

	private static final ILog LOG = Logs.getLog(ShowValueFromCacheHandler.class);
	private ISelection selection;
	private IEditorPart targetEditor;
	
	public void run(IAction action) {
		try {
			doit();
		} catch (Exception e) {
			LOG.exception(e);
		}
	}
	
	void doit() throws Exception {
		//System.out.println("-- Editor action --" + selection.toString());
		
		assert selection instanceof ITextSelection;
		ITextSelection textSel = (ITextSelection) selection;
		IResource resource = (IResource) targetEditor.getEditorInput().getAdapter(IResource.class);
		
		IPosition pos = new Position(resource.getFullPath().toPortableString(), textSel.getOffset(), textSel.getLength());
		Cache cache = CacheProvider.getCache(resource.getProject().getName());
		IAbstractString str = cache.getAbstractString(pos);
		LOG.message("Str from cache: " + str);
		
	}
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		this.targetEditor = targetEditor;
	}

}
