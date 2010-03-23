package ee.stacc.productivity.edsl.crawler;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import ee.stacc.productivity.edsl.common.logging.ILog;
import ee.stacc.productivity.edsl.common.logging.Logs;
import ee.stacc.productivity.edsl.string.IPosition;
import ee.stacc.productivity.edsl.string.Position;

public class PositionUtil {
	private static final ILog LOG = Logs.getLog(AbstractStringEvaluator.class);

	public static IFile getFile(ASTNode node) {
		try {
			ICompilationUnit unit = ASTUtil.getICompilationUnit(node);
			if (unit == null) {
				// probably node is from a patched unit, try to recover ICompilationUnit
				CompilationUnit cUnit = ASTUtil.getCompilationUnit(node);
				if (cUnit.getProperty(ASTUtil.ORIGINAL_I_COMPILATION_UNIT) != null) {
					unit = (ICompilationUnit)cUnit.getProperty(ASTUtil.ORIGINAL_I_COMPILATION_UNIT);
				}
				else {
					throw new IllegalStateException("Can't getFile for node");
				}
			}
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

	public static IPosition getPosition(ASTNode node) {
		return new Position(getFileString(node), node.getStartPosition(), node.getLength());
	}

}
