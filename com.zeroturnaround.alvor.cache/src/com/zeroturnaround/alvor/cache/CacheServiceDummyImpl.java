package com.zeroturnaround.alvor.cache;

import java.util.Collection;
import java.util.Set;

import com.zeroturnaround.alvor.common.MethodInvocationDescriptor;
import com.zeroturnaround.alvor.configuration.IHotspotPattern;
import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.IPosition;

public class CacheServiceDummyImpl implements ICacheService {

	@Override
	public void shutdown() {
		// do nothing
	}

	@Override
	public void clearAll() {
		// do nothing
	}

	@Override
	public void removeFile(String path) {
		// do nothing
	}

	@Override
	public void removeFiles(Set<String> paths) {
		// do nothing
	}

	@Override
	public void setNocache(boolean value) {
		// do nothing
	}

	@Override
	public Collection<IPosition> getInvalidatedHotspotPositions() {
		return null;
	}

	@Override
	public IAbstractString getAbstractString(IPosition position) {
		return null;
	}

	@Override
	public IAbstractString getContainingAbstractString(String fileString,
			int offset) {
		return null;
	}

	@Override
	public void addAbstractString(IPosition position, IAbstractString result) {
	}

	@Override
	public void addUnsupported(IPosition position, String message) {
	}

	@Override
	public IScopedCache<IHotspotPattern, IPosition> getHotspotCache() {
		return null;
	}

	@Override
	public IScopedCache<MethodInvocationDescriptor, IAbstractString> getMethodTemplateCache() {
		return null;
	}

}
