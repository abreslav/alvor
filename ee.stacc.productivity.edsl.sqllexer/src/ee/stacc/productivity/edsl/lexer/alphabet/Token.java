package ee.stacc.productivity.edsl.lexer.alphabet;


public class Token implements IAbstractInputItem {
	
	public static Token create(int type, ISequence<IAbstractInputItem> text) {
		return new Token(type, text);
	}
	
	private final int type;
	private final ISequence<IAbstractInputItem> text;
	
	private Token(int type, ISequence<IAbstractInputItem> text) {
		this.type = type;
		this.text = text;
	}
	
	public int getType() {
		return type;
	}
	
	public ISequence<IAbstractInputItem> getText() {
		return text;
	}

	@Override
	public int getCode() {
		return getType();
	}
	
	@Override
	public String toString() {
		return "T" + type + "[" + text + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		result = prime * result + type;
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
		Token other = (Token) obj;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
	
	
	
}
