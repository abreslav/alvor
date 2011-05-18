package com.googlecode.alvor.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.googlecode.alvor.string.IPosition;
import com.googlecode.alvor.string.Position;

public class PositionUtil {
	public static String getFileString(IResource file) {
		return file.getFullPath().toPortableString();
	}

	public static IFile getFile(IPosition position) {
		return ResourcesPlugin.getWorkspace().getRoot().getFile(Path.fromPortableString(position.getPath()));
	}
	
	public static IPosition shiftPosition(IPosition position, int startDelta, int lengthDelta) {
		return new Position(position.getPath(), 
				position.getStart() + startDelta,
				position.getLength() + lengthDelta);
	}

	public static int getLineNumber(IPosition position) {
		return getLineNumber(getFile(position), position.getStart());
	}
	
	public static int getLineNumber(IFile file, int start) {
		Reader contents = null;
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
	
	public static IResource getPositionResource(IPosition pos) {
		IPath path = Path.fromPortableString(pos.getPath());
		return ResourcesPlugin.getWorkspace().getRoot().findMember(path);
	}
	
	public static String getProjectName(IPosition pos) {
		IPath path = Path.fromPortableString(pos.getPath());
		String str = path.toString();
		if (str.startsWith("/")) {
			return str.substring(1, str.indexOf('/', 1));
		}
		else {
			return str.substring(0, str.indexOf('/'));
		}
	}
}
