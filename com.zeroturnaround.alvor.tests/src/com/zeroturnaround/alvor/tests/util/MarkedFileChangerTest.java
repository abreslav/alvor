package com.zeroturnaround.alvor.tests.util;

import junit.framework.Assert;

import org.junit.Test;


/**
 * This tests the FileChanger itself
 * @author Aivar
 *
 */
public class MarkedFileChangerTest {

//	@Test
//	public void doSomeChanges() {
//		System.out.println(MarkedFileChanger.makeChangeInLine("a //1>> b", 1)); 
//		System.out.println(MarkedFileChanger.makeChangeInLine("a //2>> b", 1)); 
//		System.out.println(MarkedFileChanger.makeChangeInLine("a //1>> b", 0)); 
//		System.out.println(MarkedFileChanger.makeChangeInLine("b //2<< a", 0)); 
//	}
	
	@Test
	public void undoChange() {
		String before = "abc //1<< xyz";
		String after =  MarkedFileChanger.makeChangeInLine(before, 0);
		String expectedAfter = "xyz //1>> abc"; 
		Assert.assertEquals(expectedAfter, after);
	}
	
	@Test
	public void undoNoChanges() {
		String before = "abc //1>> xyz";
		String after =  MarkedFileChanger.makeChangeInLine(before, 0);
		String expectedAfter = "abc //1>> xyz"; 
		Assert.assertEquals(expectedAfter, after);
	}
	
	@Test
	public void performRightChange() {
		String before = "abc //1>> xyz";
		String after =  MarkedFileChanger.makeChangeInLine(before, 1);
		String expectedAfter = "xyz //1<< abc"; 
		Assert.assertEquals(expectedAfter, after);
	}
	
	@Test
	public void ingnoreWrongChange() {
		String before = "abc //1>> xyz";
		String after =  MarkedFileChanger.makeChangeInLine(before, 2);
		String expectedAfter = "abc //1>> xyz";; 
		Assert.assertEquals(expectedAfter, after);
	}
}
