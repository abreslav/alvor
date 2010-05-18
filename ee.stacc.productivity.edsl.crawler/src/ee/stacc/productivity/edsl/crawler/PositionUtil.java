package ee.stacc.productivity.edsl.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;

import ee.stacc.productivity.edsl.common.logging.ILog;
import ee.stacc.productivity.edsl.common.logging.Logs;
import ee.stacc.productivity.edsl.string.IPosition;
import ee.stacc.productivity.edsl.string.Position;

public class PositionUtil {
	private static final ILog LOG = Logs.getLog(AbstractStringEvaluator.class);

	public static IFile getFile(ASTNode node) {
		try {
			ICompilationUnit unit = ASTUtil.getICompilationUnit(node);
			IFile correspondingResource = (IFile) unit.getCorrespondingResource();
			return correspondingResource;
		} catch (JavaModelException e) {
			LOG.exception(e);
			throw new IllegalStateException(e);
		}
	}
	
	public static String getFileString(ASTNode node) {
		IResource file = getFile(node);
		return getFileString(file);
	}

	public static String getFileString(IResource file) {
		return file.getFullPath().toPortableString();
	}

	public static IPosition getPosition(ASTNode node) {
		return new Position(getFileString(node), node.getStartPosition(), node.getLength());
	}

	public static IFile getFile(IPosition position) {
		return ResourcesPlugin.getWorkspace().getRoot().getFile(Path.fromPortableString(position.getPath()));
	}
	
	public static int getLineNumber(ASTNode node) {
		return getLineNumber(getPosition(node));
	}
	
	public static int getLineNumber(IPosition position) {
		IFile file = getFile(position);
		Reader contents = null;
		int start = position.getStart();
		try {
			contents = new BufferedReader(new InputStreamReader(file.getContents(true)));
			boolean cr = false;
			int line = 1;
			int offset = 0;
			loop: while (true) {
				int c = contents.read();
				switch (c) {
				case '\r':
					if (cr) {
						line++;
					}
					cr = true;
					break;
				case '\n':
					cr = false;
					line++;
					break;
				default:
					if (cr) {
						line++;
						cr = false;
					}
					if (c == -1) {
						break loop;
					}
					break;
				}
				if (offset == start) {
					return line;
				}
				offset++;
			}
		} catch (CoreException e) {
			e.printStackTrace();
			return -1;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		} finally {
			if (contents != null) {
				try {
					contents.close();
				} catch (IOException e) {
					// Nothing
				}
			}
		}
		return -1;
	}

	public static String getLineString(IPosition sr) {
		return sr.getPath() + ":" + getLineNumber(sr);
	}
	
	public static IPosition getDummyPosition() {
		return new Position("__dummy__", 1, 2);
	}
}
