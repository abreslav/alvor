package com.zeroturnaround.alvor.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProjectConfiguration {
	private List<HotspotProperties> hotspots = new ArrayList<HotspotProperties>();
	private List<DataSourceProperties> dataSources = new ArrayList<DataSourceProperties>();
	private File sourceFile;

	public ProjectConfiguration(List<HotspotProperties> hotspots, List<DataSourceProperties> dataSources,
			File sourceFile) {
		this.hotspots = hotspots;
		this.dataSources = dataSources;
		this.sourceFile = sourceFile;
	}
	
	public List<DataSourceProperties> getDataSources() {
		return dataSources;
	}
	
	public List<HotspotProperties> getHotspots() {
		return hotspots;
	}
	
	public File getSourceFile() {
		return sourceFile;
	}
	
	
}
