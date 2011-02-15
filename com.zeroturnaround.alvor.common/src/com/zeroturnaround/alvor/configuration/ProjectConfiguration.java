package com.zeroturnaround.alvor.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.zeroturnaround.alvor.common.HotspotPattern;

public class ProjectConfiguration {
	public enum CheckingStrategy {PREFER_STATIC, PREFER_DYNAMIC, ALL_CHECKERS}
	
	private List<HotspotPattern> hotspotPatterns = new ArrayList<HotspotPattern>();
	private List<DataSourceProperties> dataSources = new ArrayList<DataSourceProperties>();
	private Map<String, String> properties;

	public ProjectConfiguration(List<HotspotPattern> hotspots, List<DataSourceProperties> dataSources,
			Map<String, String> properties) {
		this.hotspotPatterns = hotspots;
		this.dataSources = dataSources;
		this.properties = properties;
	}
	
	public List<DataSourceProperties> getDataSources() {
		return dataSources;
	}
	
	public List<HotspotPattern> getHotspotPatterns() {
		return hotspotPatterns;
	}
	
	public void setHotspotPatterns(List<HotspotPattern> hotspots) {
		this.hotspotPatterns = hotspots;
	}

	public void setDataSources(List<DataSourceProperties> dataSources) {
		this.dataSources = dataSources;
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
	
//	public String getProperty(String key) {
//		String value = this.properties.get(key);
//		if (value != null) {
//			return value;
//		} else {
//			return "";
//		}
//	}
//	
	public DataSourceProperties getDefaultDataSource() {
//		for (DataSourceProperties props : this.getDataSources()) {
//			if (props.getPattern().equals("*")) {
//				return props;
//			}
//		}
//		return null;
		
		// temporary: give first one
		if (dataSources.size() > 0) {
			return dataSources.get(0);
		}
		else {
			return null;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj != null 
			&& obj instanceof ProjectConfiguration 
			&& this.hashCode() == obj.hashCode();
	}
	
	@Override
	public int hashCode() {
		int result = 17;
		result = result * 31 + dataSources.hashCode();
		result = result * 31 + hotspotPatterns.hashCode();
		result = result * 31 + properties.hashCode();
		return result;
	}
}
