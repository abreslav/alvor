package ee.stacc.productivity.edsl.crawler;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import ee.stacc.productivity.edsl.common.logging.ILog;
import ee.stacc.productivity.edsl.common.logging.Logs;

public class PositionUtil {
	private static final ILog LOG = Logs.getLog(AbstractStringEvaluator.class);

	public static IFile getFile(ASTNode node) {
		try {
			ICompilationUnit unit = (ICompilationUnit) ((CompilationUnit) node.getRoot()).getJavaElement();
			IFile correspondingResource = (IFile) unit.getCorrespondingResource();
			return correspondingResource;
		} catch (JavaModelException e) {
			LOG.exception(e);
			throw new IllegalStateException(e);
		}
	}
	
	public static String getFileString(ASTNode node) {
		return getFile(node).getFullPath().toPortableString();
	}

	
}
