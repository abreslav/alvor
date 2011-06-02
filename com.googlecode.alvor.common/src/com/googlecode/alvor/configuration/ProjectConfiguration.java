package com.googlecode.alvor.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.googlecode.alvor.common.HotspotPattern;

public class ProjectConfiguration {
	public static final int DEFAULT_EFFORT_LEVEL = 2; 
	public enum CheckingStrategy {PREFER_STATIC, PREFER_DYNAMIC, ALL_CHECKERS}
	
	private List<HotspotPattern> hotspotPatterns = new ArrayList<HotspotPattern>();
	private List<CheckerConfiguration> checkers = new ArrayList<CheckerConfiguration>();
	private Map<String, String> properties;

	public ProjectConfiguration(List<HotspotPattern> hotspots, List<CheckerConfiguration> checkers,
			Map<String, String> properties) {
		this.hotspotPatterns = hotspots;
		this.checkers = checkers;
		this.properties = properties;
	}
	
	public List<CheckerConfiguration> getCheckers() {
		return checkers;
	}
	
	public List<HotspotPattern> getHotspotPatterns() {
		return hotspotPatterns;
	}
	
	public void setHotspotPatterns(List<HotspotPattern> hotspots) {
		this.hotspotPatterns = hotspots;
	}

	public void setCheckers(List<CheckerConfiguration> checkers) {
		this.checkers = checkers;
	}
	
	public CheckingStrategy getCheckingStrategy() {
		String result = properties.get("checkingStrategy");
		
		if (result != null && result.equals("allCheckers")) {
			return CheckingStrategy.ALL_CHECKERS; 
		} else if (result != null && result.equals("preferStatic")) {
			return CheckingStrategy.PREFER_STATIC; 
		} else {
			return CheckingStrategy.PREFER_DYNAMIC;
		}
	}
	
	public boolean getSupportLoops() {
		String result = properties.get("supportLoops"); 
		if (result != null && result.equals("false")) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public Map<String, String> getProperties() {
		return properties;
	}
	
	public boolean getMarkHotspots() {
		String result = properties.get("markHotspots"); 
		if (result != null && result.equals("false")) {
			return false;
		}
		else {
			return true;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || ! (obj instanceof ProjectConfiguration)) {
			return false;
		}
		
		ProjectConfiguration that = (ProjectConfiguration)obj;
		return this.properties.equals(that.properties)
				&& this.checkers.equals(that.checkers)
				&& this.hotspotPatterns.equals(that.hotspotPatterns);
	}
	
	@Override
	public int hashCode() {
		int result = 17;
		result = result * 31 + checkers.hashCode();
		result = result * 31 + hotspotPatterns.hashCode();
		result = result * 31 + properties.hashCode();
		return result;
	}
	
	public int getEffortLevel() {
		 if (this.properties != null) {
			 String levelStr = this.properties.get("effortLevel");
			 if (levelStr != null) {
				 try {
					 return Integer.parseInt(levelStr);
				 } 
				 catch (NumberFormatException e) {
					 return DEFAULT_EFFORT_LEVEL;
				 }
			 }
			 else {
				 return DEFAULT_EFFORT_LEVEL; 
			 }
		 }
		 else {
			 return DEFAULT_EFFORT_LEVEL;
		 }
	}
	
	public void setProperty(String name, String value) {
		if (properties == null) {
			properties = new HashMap<String, String>();
		}
		properties.put(name, value);
	}
	
	public CheckerConfiguration getCheckerConfiguration(String connectionPattern) {
		if (connectionPattern != null) {
			for (CheckerConfiguration checkerConf : this.getCheckers()) {
				if (checkerConf.matchesPattern(connectionPattern)) {
					return checkerConf;
				}
			}
		}
		// none matched
		return getDefaultCheckerConfiguration();
	}
	
	private CheckerConfiguration getDefaultCheckerConfiguration() {
		for (CheckerConfiguration conf : this.getCheckers()) {
			if (conf.getPatterns().isEmpty()) {
				return conf;
			}
		}
		
		return null;
	}
}
