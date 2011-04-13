package com.zeroturnaround.alvor.tests.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.zeroturnaround.alvor.common.HotspotDescriptor;
import com.zeroturnaround.alvor.common.PositionUtil;
import com.zeroturnaround.alvor.common.StringHotspotDescriptor;
import com.zeroturnaround.alvor.common.UnsupportedHotspotDescriptor;
import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.samplegen.SampleGenerator;

public class TestUtil {
	
	public static boolean filesAreEqual(File a, File b) {
		try {
			assert a.exists() && b.exists();
			
			Scanner scA = new Scanner(a);
			Scanner scB = new Scanner(b);
			
			while (scA.hasNextLine()) {
				if (!scB.hasNextLine() || ! scA.nextLine().equals(scB.nextLine())) {
					return false;
				}
			}
			
			return !scA.hasNextLine() && !scB.hasNextLine();
		} 
		catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static IPath getTestResultsFolder(IProject project, String subfolderName) {
		// prepare folder
		IPath folderPath = project.getLocation().append("AlvorSelfTestResults");
		if (subfolderName != null && !subfolderName.isEmpty() && !subfolderName.equals(".")) {
			folderPath.append(subfolderName);
		}
		if (!folderPath.toFile().exists()) {
			boolean success = folderPath.toFile().mkdirs();
			assert success;
		}
		return folderPath;
	}
	
	public static void storeFoundHotspotInfo(List<HotspotDescriptor> descriptors, IPath folder) {
		List<String> descriptorLines = new ArrayList<String>();
		List<String> concreteLines = new ArrayList<String>();
		List<String> positionLines = new ArrayList<String>(); 
		
		for (HotspotDescriptor desc : descriptors) {
			
			String positionString = PositionUtil.getLineString(desc.getPosition());
			positionLines.add(positionString);
			
			if (desc instanceof StringHotspotDescriptor) {
				IAbstractString aStr = ((StringHotspotDescriptor)desc).getAbstractValue(); 
				descriptorLines.add(positionString + ", " + aStr.toString());
				try {
					concreteLines.addAll(SampleGenerator.getConcreteStrings(aStr));
				} catch (Exception e) {
					concreteLines.add("ERROR GENERATING SAMPLES: " + e.getMessage()
							+ ", POS=" + aStr.getPosition() + ", ABS_STR=" + aStr);
				}
			}
			else if (desc instanceof UnsupportedHotspotDescriptor) {
				descriptorLines.add(positionString + ", unsupported: " 
						+ ((UnsupportedHotspotDescriptor)desc).getProblemMessage());
			}
			else {
				descriptorLines.add("???");
			}
		}
		
		List<String> sortedDescriptorLines = new ArrayList<String>(descriptorLines);
		Collections.sort(sortedDescriptorLines);
		Collections.sort(concreteLines);
		Collections.sort(positionLines);
		
		storeFoundTestResults(positionLines, folder, "node_positions");
		storeFoundTestResults(concreteLines, folder, "concrete_strings");
		storeFoundTestResults(descriptorLines, folder, "node_descriptors");
		storeFoundTestResults(sortedDescriptorLines, folder, "node_descriptors_sorted");
	}
	
	// returns non-matching topics
	public static String findDifferencesInResults(File folder) {
		File[] files = folder.listFiles();
		Set<String> processedTopics = new HashSet<String>();
		String result = "";
		
		for (File file : files) {
			if (file.isDirectory()) {
				if (file.getName().equals(".svn")) {
					continue;
				}
				String dirResult = findDifferencesInResults(file);
				if (!dirResult.isEmpty()) {
					result += new Path(file.getAbsolutePath()).lastSegment()
						+ "(" + dirResult + ") ;";
				}
			}
			else {
				String name = file.getName();
				String regex;
				if (name.endsWith("_found.txt")) {
					regex = "(\\w+)_found\\.txt";
				}
				else if (name.endsWith("_expected.txt")) {
					regex = "(\\w+)_expected\\.txt";
				}
				else {
					System.err.println("Unknown file in results folder: " + name);
					continue;
				}
				Matcher matcher = Pattern.compile(regex).matcher(name);
				boolean foundMatch = matcher.find();
				assert foundMatch;
				String topic = matcher.group(1);
				if (processedTopics.contains(topic)) {
					continue;
				}
				
				File expected = new File(folder.getAbsolutePath() + "/" + topic + "_expected.txt");
				File found = new File(folder.getAbsolutePath() + "/" + topic + "_found.txt");
				if (!expected.exists() || !found.exists() || !filesAreEqual(expected, found)) {
					result += topic + "; ";
				}
				
				processedTopics.add(topic);
			}
		}
		
		return result;
	}
	
	public static void storeFoundTestResults(List<String> items, IPath folder, String topic) {
		// write strings to file
		File file = folder.append(topic + "_found.txt").toFile();
		if (file.exists()) {
			boolean result = file.delete();
			assert result;
		}
		try {
			PrintStream outStream = new PrintStream(file);
			for (String item : items) {
				outStream.println(item);
			}
			outStream.close();
		} 
		catch (FileNotFoundException e){
			throw new IllegalStateException(e);
		}
	}
}
