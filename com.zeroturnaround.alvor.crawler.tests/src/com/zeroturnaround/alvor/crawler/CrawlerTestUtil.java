package com.zeroturnaround.alvor.crawler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

public class CrawlerTestUtil {
	
	public static boolean filesAreEqual(File a, File b) throws FileNotFoundException {
		assert a.exists() && b.exists();
		
		Scanner scA = new Scanner(a);
		Scanner scB = new Scanner(b);
		
		while (scA.hasNextLine()) {
			if (!scB.hasNextLine() || ! scA.nextLine().equals(scB.nextLine())) {
				return false;
			}
		}
		
		return true;
	}

	public static boolean stringsAreExpected(List<String> items, String filePrefix) {
		
		File outFile = new File(filePrefix + "_found.txt");
		if (outFile.exists()) {
			outFile.delete();
		}
		try {
			PrintStream outStream = new PrintStream(outFile);
			for (String item : items) {
				outStream.println(item);
			}
			outStream.close();
			
			File expectedFile = new File(filePrefix + "_expected.txt");
			if (expectedFile.exists()) {
				return CrawlerTestUtil.filesAreEqual(outFile, expectedFile);
			} else {
				return false;
			}
		} 
		catch (FileNotFoundException e){
			throw new IllegalStateException(e);
		}
	}
	
	public static File getAndPrepareTestResultsFolder(IProject project) {
		File folder = project.getLocation().append("AlvorSelfTestResults").toFile();
		folder.mkdirs();
		return folder;
	}

	public static IProject getProject(String projectName) throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(projectName);
		if (!project.isOpen()) {
			project.open(null);
		}
		return project;
	}
	
}
