package ee.stacc.productivity.edsl.lexer.alphabet;


public class SimpleCharacter implements IAbstractInputItem {

	public static IAbstractInputItem create(int code) {
		if (code == -1) {
			return EOF;
		}
		return new SimpleCharacter(code);
	}
	
	private final int code;

	private SimpleCharacter(int code) {
		this.code = code;
	}

	@Override
	public int getCode() {
		return code;
	}

	@Override
	public String toString() {
		return "" + (char) code;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + code;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleCharacter other = (SimpleCharacter) obj;
		if (code != other.code)
			return false;
		return true;
	}
	
	
	
}
