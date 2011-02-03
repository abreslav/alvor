package com.zeroturnaround.alvor.cache;

import java.util.Collection;

import com.zeroturnaround.alvor.common.HotspotPattern;
import com.zeroturnaround.alvor.common.NodeDescriptor;

public interface ICache {
	Collection<NodeDescriptor> getPrimaryHotspotDescriptors(String projectName);
	
	void setProjectPrimaryPatterns(String projectName, Collection<HotspotPattern> patterns);
	
	Collection<HotspotPattern> getProjectPatterns(String projectName);
	
	void setFileContributions(String fileName, Collection<NodeDescriptor> descriptors);
	
}
