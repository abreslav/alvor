package com.zeroturnaround.alvor.gui.changetests;

import java.util.Collection;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

import com.zeroturnaround.alvor.crawler.util.JavaModelUtil;
import com.zeroturnaround.alvor.gui.GuiFacade;

public class MarkedFileChanger {
	public static void undoAllChangesInProject(IJavaProject project) {
		applyChangesInProject(project, 0);
	}
	
	public static void undoAllChangesInFile(IFile file) {
		applyChangesInFile(file, 0);
	}
	
	public static void applyChangesInProject(IJavaProject project, int changeNo) {
		Collection<ICompilationUnit> cunits = JavaModelUtil.getAllCompilationUnits(project, false);
		for (ICompilationUnit cunit : cunits) {
			try {
				applyChangesInFile((IFile)cunit.getCorrespondingResource(), changeNo);
			} catch (JavaModelException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public static void applyChangesInFile(IFile file, int changeNo) {
		try {
			Scanner sc = new Scanner(file.getLocation().toFile());
			
			StringBuilder newText = new StringBuilder();
			while (sc.hasNextLine()) {
				newText.append(makeChangeInLine(sc.nextLine(), changeNo) + "\n");
			}
			GuiFacade.setFileContent(file, newText.toString());
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
