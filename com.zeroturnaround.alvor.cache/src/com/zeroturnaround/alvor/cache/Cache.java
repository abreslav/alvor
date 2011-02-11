package com.zeroturnaround.alvor.cache;

import java.util.Collection;
import java.util.List;

import com.zeroturnaround.alvor.common.HotspotPattern;
import com.zeroturnaround.alvor.common.NodeDescriptor;
import com.zeroturnaround.alvor.common.UnsupportedNodeDescriptor;
import com.zeroturnaround.alvor.string.AbstractStringCollection;
import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.IPosition;

public class Cache {

	public void removeFile(String fileName) {
		invalidateFile(fileName);
		// delete from files where name = ?
	}
	
	public void addFile(String projectName, String fileName) {
		// get project id
		// insert into files (name, project_id, batch_no) values (?, ?, 0)
	}
	
	private void invalidateFile(String fileName) {
		// delete from abstract_strings where file = (select id from files where name = ?)
	}
	
	public Collection<NodeDescriptor> getPrimaryHotspotDescriptors(String projectName) {
		return null;
	}

	public void setProjectPrimaryPatterns(String projectName,
			Collection<HotspotPattern> patterns) {
	}

	public List<HotspotPattern> getProjectPatterns(String projectName) {
		return null;
	}

	public List<HotspotPattern> getNewProjectPatterns(String projectName) {
		return null;
	}

	public void updateFileContributionsForPatterns(String fileName,
			Collection<NodeDescriptor> descriptors,
			Collection<HotspotPattern> patterns) {
		
		// TODO maybe better to require batch number instead of patterns ??
		
		// remove old descriptors for this file and these patterns (and subnodes ??)
		// add new ones
	}
	
	public List<IAbstractString> getUncheckedHotspots(String projectName) {
		return null;
	}
	
	public void markScopeAsChecked(/**/) {
		
	}
	
	public void addHotspot(HotspotPattern pattern, NodeDescriptor desc) {
		// desc should be a choice (???)
		// creates a choice with information about pattern
	}
	
	/**
	 * Marks this primary branch as unchecked  
	 */
	private void invalidateRespectivePrimaryHotspot() {
		
	}
	
	private void addUnsupported(UnsupportedNodeDescriptor desc) {
		
	}
	
	private void addString(IAbstractString str) {
		
	}

	private void addCollection(AbstractStringCollection str) {
		
	}

	private void cleanUnusedNodesForFile(String fileName) {
		
	}

	public void removeFileStrings(String fileName) {
	}

	public void removeOrphanedHotspotPatterns(String elementName) {
	}

	public void cleanProject(String name) {
	}

	public void addPrimaryHotspotPatterns(String projectName, List<HotspotPattern> primaryPatterns) {
	}
}
