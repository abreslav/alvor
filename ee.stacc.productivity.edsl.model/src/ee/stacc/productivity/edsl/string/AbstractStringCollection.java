package ee.stacc.productivity.edsl.string;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AbstractStringCollection {

	private final List<IAbstractString> items;

	public AbstractStringCollection(List<? extends IAbstractString> options) {
		this.items = new ArrayList<IAbstractString>(options);
	}

	public AbstractStringCollection(IAbstractString... options) {
		this.items = Arrays.asList(options);
	}	
	
	public List<IAbstractString> getItems() {
		return Collections.unmodifiableList(items);
	}

}