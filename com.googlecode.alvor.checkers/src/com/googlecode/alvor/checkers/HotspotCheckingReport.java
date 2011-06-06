package com.googlecode.alvor.checkers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HotspotCheckingReport {
	private List<String> passedCheckers = new ArrayList<String>();
	private List<HotspotProblem> problems = new ArrayList<HotspotProblem>();
	
	public void addPassedChecker(String checkerName) {
		this.passedCheckers.add(checkerName);
	}
	
	public void addProblem(String checkerName, HotspotProblem problem) {
		this.problems.add(new HotspotProblem(
			problem.getMessage() + " [" + checkerName + "]",
			problem.getPosition(),
			problem.getProblemType()
		));
	}
	
	public void addProblems(String checkerName, Collection<HotspotProblem> problems) {
		for (HotspotProblem problem : problems) {
			this.addProblem(checkerName, problem);
		}
	}
	
	public List<String> getPassedCheckers() {
		return passedCheckers;
	}
	
	public List<HotspotProblem> getProblems() {
		return problems;
	}
}
