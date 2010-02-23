package ee.stacc.productivity.edsl.crawler;

import ee.stacc.productivity.edsl.checkers.IPositionDescriptor;
import ee.stacc.productivity.edsl.string.StringConstant;

public interface IStringPositionStorage {
	IStringPositionStorage NONE = new IStringPositionStorage() {
		
		@Override
		public void setPositionInformation(StringConstant literal,
				IPositionDescriptor descriptor, String escapedValue) {
		}
	};
	
	void setPositionInformation(StringConstant literal, IPositionDescriptor descriptor, String escapedValue);
}
