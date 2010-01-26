/**
 * 
 */
package ee.stacc.productivity.edsl.sqlparser;


public interface IParserState {
	public static final IParserState ERROR = new IParserState() {
		@Override
		public IAction getAction(int symbolNumber) {
			throw new UnsupportedOperationException("Error state reached");
		}
		
		public String toString() {
			return "ERROR";
		}

		@Override
		public boolean isTerminating() {
			return true;
		}
	};
	
	public static final IParserState ACCEPT = new IParserState() {
		
		@Override
		public IAction getAction(int symbolNumber) {
			throw new UnsupportedOperationException("Already accepted");
		}
		
		public String toString() {
			return "ACCEPT";			
		}

		@Override
		public boolean isTerminating() {
			return true;
		}
	};

	IAction getAction(int symbolNumber);
	boolean isTerminating();
}