package ee.stacc.productivity.edsl.string;

public class StringRandomInteger extends StringCharacterSet {
	public StringRandomInteger() {
		super("0123456789");
	}
	
	public String toString() {
		return "\"666\"";
	}
	
	public String getExample() {
		return "666";
	}
}
