package ee.stacc.productivity.edsl.crawler;

import ee.stacc.productivity.edsl.checkers.IPositionDescriptor;
import ee.stacc.productivity.edsl.string.StringConstant;

public interface IPositionStorage {
	IPositionStorage NONE = new IPositionStorage() {
		
		@Override
		public void setPosition(StringConstant literal,
				IPositionDescriptor descriptor) {
		}
	};
	
	void setPosition(StringConstant literal, IPositionDescriptor descriptor);
}
