package com.zeroturnaround.alvor.tests.util;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import com.zeroturnaround.alvor.common.WorkspaceUtil;

public class MarkedFileChanger {
	public static void undoAllChangesInProject(IProject project, Pattern fileNamePattern) {
		applyChangesInProject(project, fileNamePattern, 0);
	}
	
	public static void undoAllChangesInFile(IFile file) {
		applyChangesInFile(file, 0);
	}
	
	public static boolean applyChangesInProject(IProject project, Pattern fileNamePattern, int changeNo) {
		
		List<IFile> files = WorkspaceUtil.getAllFilesInContainer(project, fileNamePattern);
		
		boolean projectHadChanges = false;
		
		for (IFile file : files) {
			boolean fileHadChanges = applyChangesInFile(file, changeNo);
			projectHadChanges = projectHadChanges || fileHadChanges;
		}
		
		return projectHadChanges;
	}
	
	public static boolean applyChangesInFile(IFile file, int changeNo) {
		try {
			file.refreshLocal(IResource.DEPTH_INFINITE, null);
			Scanner sc = new Scanner(file.getLocation().toFile());
			
			StringBuilder oldText = new StringBuilder();
			StringBuilder newText = new StringBuilder();
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				oldText.append(line + "\n");
				newText.append(makeChangeInLine(line, changeNo) + "\n");
			}
			if (!newText.toString().equals(oldText.toString())) {
				file.setContents(new ByteArrayInputStream(newText.toString().getBytes()), 0, null);
				file.refreshLocal(IResource.DEPTH_INFINITE, null);
				return true;
			}
			else {
				return false;
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
		
	
//	static String undoChangesInLine(String line) {
//		return makeChangeInLine(line, 0);
//	}
	/**
	 * 
	 * @param line
	 * @param changeNo, 0 means revert all changes
	 * @return
	 */
	static String makeChangeInLine(String line, int changeNo) {
		if (changeNo == 0) {
			String[] parts = line.split(" //\\d<< ");
			if (parts.length == 2) {
				// need to know the actual change number
				Pattern pat = Pattern.compile(" //(\\d)<< ");
				Matcher mat = pat.matcher(line);
				boolean found = mat.find();
				assert (found);
				int actualChangeNo = Integer.parseInt(mat.group(1));
				
				// reverse 
				return parts[1] + " //" + actualChangeNo + ">> " + parts[0];
			}
			else {
				return line;
			}
		}
		else {
			String[] parts = line.split(" //" + changeNo + ">> ");
			if (parts.length == 2) {
				return parts[1] + " //" + changeNo + "<< " + parts[0];
			} else {
				return line;
			}
		}
		
		
	}
}
