package ee.stacc.productivity.edsl.lexer.automata;

public interface IAlphabetConverter {

	IAlphabetConverter ID = new IAlphabetConverter() {
		
		@Override
		public int convert(int c) {
			return c;
		}
	};
	
	int convert(int c);
}
