package com.zeroturnaround.alvor.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;

public class ProjectConfiguration {
	public enum CheckingStrategy {PREFER_STATIC, PREFER_DYNAMIC, ALL_CHECKERS}
	
	private List<IHotspotPattern> hotspots = new ArrayList<IHotspotPattern>();
	private List<DataSourceProperties> dataSources = new ArrayList<DataSourceProperties>();
	private Map<String, String> properties;
	private final IProject project;

	public ProjectConfiguration(List<IHotspotPattern> hotspots, List<DataSourceProperties> dataSources,
			Map<String, String> properties, IProject project) {
		this.hotspots = hotspots;
		this.dataSources = dataSources;
		this.properties = properties;
		this.project = project;
	}
	
	public List<DataSourceProperties> getDataSources() {
		return dataSources;
	}
	
	public List<IHotspotPattern> getHotspots() {
		return hotspots;
	}
	
	public void setHotspots(List<IHotspotPattern> hotspots) {
		this.hotspots = hotspots;
	}

	public void setDataSources(List<DataSourceProperties> dataSources) {
		this.dataSources = dataSources;
	}
	
	public CheckingStrategy getCheckingStrategy() {
		String result = properties.get("checkingStrategy");
		
		if (result.equals("allCheckers")) {
			return CheckingStrategy.ALL_CHECKERS; 
		} else if (result.equals("preferStatic")) {
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
		for (DataSourceProperties props : this.getDataSources()) {
			if (props.getPattern().equals("*")) {
				return props;
			}
		}
		return null;
	}
	
	public IProject getProject() {
		return project;
	}
	
	public String getProjectPath() {
		return this.project.getFullPath().toPortableString();
	}
}
