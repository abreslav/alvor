/**
 * 
 */
package ee.stacc.productivity.edsl.sqlparser;



public interface IParserState {

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

		@Override
		public boolean isError() {
			return false;
		}
	};

	IAction getAction(int symbolNumber);
	boolean isTerminating();
	boolean isError();
}