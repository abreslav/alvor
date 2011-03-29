package com.zeroturnaround.alvor.crawler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.eclipse.core.resources.IProject;

import com.zeroturnaround.alvor.common.HotspotDescriptor;
import com.zeroturnaround.alvor.common.PositionUtil;
import com.zeroturnaround.alvor.common.StringHotspotDescriptor;
import com.zeroturnaround.alvor.common.UnsupportedHotspotDescriptor;
import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.samplegen.SampleGenerator;

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
		
		return !scA.hasNextLine() && !scB.hasNextLine();
	}

	public static boolean stringsAreExpected(List<String> items, String filePrefix) {
		
		File outFile = new File(filePrefix + "_found.txt");
		if (outFile.exists()) {
			boolean result = outFile.delete();
			assert result;
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
		boolean success = folder.mkdirs();
		assert (success || folder.exists());
		return folder;
	}
	
	public static void validateNodeDescriptors(List<HotspotDescriptor> descriptors, IProject project) {
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
		
		File folder = CrawlerTestUtil.getAndPrepareTestResultsFolder(project);
		
		boolean concreteResult = CrawlerTestUtil.stringsAreExpected(concreteLines, 
				folder.getAbsolutePath() + "/concrete_strings");
		boolean sortedAbstractResult = CrawlerTestUtil.stringsAreExpected(sortedDescriptorLines, 
				folder.getAbsolutePath() + "/node_descriptors_sorted");
		boolean abstractResult = CrawlerTestUtil.stringsAreExpected(descriptorLines, 
				folder.getAbsolutePath() + "/node_descriptors");
		boolean positionResult = CrawlerTestUtil.stringsAreExpected(positionLines, 
				folder.getAbsolutePath() + "/node_positions");
		
		if (!positionResult) {
			throw new AssertionError("Positions are different");
		}
		else if (!concreteResult) {
			throw new AssertionError("Concretes are different");
		}
		else if (!sortedAbstractResult) {
			throw new AssertionError("Abstract are different, but concretes are same");
		}
		else if (!abstractResult) {
			throw new AssertionError("Abstract result is in different order");
		}
		else {
			// all OK
		}
	}
	
}
