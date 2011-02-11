package com.zeroturnaround.alvor.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.zeroturnaround.alvor.common.HotspotPattern;

public class ProjectConfiguration {
	public enum CheckingStrategy {PREFER_STATIC, PREFER_DYNAMIC, ALL_CHECKERS}
	
	private List<HotspotPattern> hotspots = new ArrayList<HotspotPattern>();
	private List<DataSourceProperties> dataSources = new ArrayList<DataSourceProperties>();
	private Map<String, String> properties;

	public ProjectConfiguration(List<HotspotPattern> hotspots, List<DataSourceProperties> dataSources,
			Map<String, String> properties) {
		this.hotspots = hotspots;
		this.dataSources = dataSources;
		this.properties = properties;
	}
	
	public List<DataSourceProperties> getDataSources() {
		return dataSources;
	}
	
	public List<HotspotPattern> getHotspots() {
		return hotspots;
	}
	
	public void setHotspots(List<HotspotPattern> hotspots) {
		this.hotspots = hotspots;
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
		if (properties.get("supportLoops").equals("false")) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public Map<String, String> getProperties() {
		return properties;
	}
	
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
		result = result * 31 + hotspots.hashCode();
		result = result * 31 + properties.hashCode();
		return result;
	}
}
