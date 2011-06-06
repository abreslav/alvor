package com.googlecode.alvor.checkers;

public class CheckerInfo {
	private final String checkerName;
	private final String description;
	private final boolean usesDatabase;
	private final String defaultDriver;

	public CheckerInfo(String checkerName, String description, boolean usesDatabase, 
			String defaultDriver) {
				this.checkerName = checkerName;
				this.description = description;
				this.usesDatabase = usesDatabase;
				this.defaultDriver = defaultDriver;
	}
	
	public String getCheckerName() {
		return checkerName;
	}
	
	public String getDefaultDriver() {
		return defaultDriver;
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean getUsesDatabase() {
		return usesDatabase;
	}
	
}
