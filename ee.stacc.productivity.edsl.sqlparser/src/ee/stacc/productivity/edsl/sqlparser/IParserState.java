/**
 * 
 */
package ee.stacc.productivity.edsl.sqlparser;

import java.util.Collection;



public interface IParserState {

	public static final IParserState ACCEPT = new IParserState() {
		
		@Override
		public Collection<IAction> getActions(int symbolNumber) {
			throw new UnsupportedOperationException("Already accepted");
		}
		
		public String toString() {
			return "ACCEPT";			
		}

		@Override
		public boolean isTerminating() {
			return true;
		}

		@Override
		public boolean isError() {
			return false;
		}
	};

	Collection<IAction> getActions(int symbolNumber);
	boolean isTerminating();
	boolean isError();
}