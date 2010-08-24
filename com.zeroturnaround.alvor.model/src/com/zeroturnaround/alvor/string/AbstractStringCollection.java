package com.zeroturnaround.alvor.string;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class AbstractStringCollection extends PositionedString {

	private final List<IAbstractString> items;

	public AbstractStringCollection(List<? extends IAbstractString> options) {
		this(null, options);
	}
	
	public AbstractStringCollection(IPosition pos, List<? extends IAbstractString> options) {
		super(pos);
		this.items = new ArrayList<IAbstractString>(options);
	}

	public AbstractStringCollection(IAbstractString... options) {
		this(null, options);
	}
	
	public AbstractStringCollection(IPosition pos, IAbstractString... options) {
		super(pos);
		this.items = Arrays.asList(options);
	}	
	
	public List<IAbstractString> getItems() {
		return Collections.unmodifiableList(items);
	}
	
	public IAbstractString get(int index) {
		return this.getItems().get(index);
	}
	
	public boolean isEmpty() {
		for (IAbstractString s : items) {
			if (!s.isEmpty()) {
				return false;
			}
		}
		return true;
	}

}
