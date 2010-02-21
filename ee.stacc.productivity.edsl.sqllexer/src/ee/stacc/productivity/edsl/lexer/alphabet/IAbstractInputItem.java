package ee.stacc.productivity.edsl.lexer.alphabet;

public interface IAbstractInputItem {

	IAbstractInputItem EOF = new IAbstractInputItem() {
		
		@Override
		public int getCode() {
			return -1;
		}
		
		public String toString() {
			return "EOF";
		};
	};
	
	int getCode();
}
