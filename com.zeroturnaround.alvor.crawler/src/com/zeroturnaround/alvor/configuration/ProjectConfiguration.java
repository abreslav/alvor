package com.zeroturnaround.alvor.configuration;

import java.util.ArrayList;
import java.util.List;

public class ProjectConfiguration {
	private List<HotspotProperties> hotspots = new ArrayList<HotspotProperties>();
	private List<DataSourceProperties> dataSources = new ArrayList<DataSourceProperties>();

	public ProjectConfiguration(List<HotspotProperties> hotspots, List<DataSourceProperties> dataSources) {
		this.hotspots = hotspots;
		this.dataSources = dataSources;
	}
	
	public List<DataSourceProperties> getDataSources() {
		return dataSources;
	}
	
	public List<HotspotProperties> getHotspots() {
		return hotspots;
	}
	
	public void setHotspots(List<HotspotProperties> hotspots) {
		this.hotspots = hotspots;
	}

	public void setDataSources(List<DataSourceProperties> dataSources) {
		this.dataSources = dataSources;
	}
	
}
