package ee.stacc.productivity.edsl.sqlparser;

public interface IParserStackLike {
	IParserState getErrorOnTop();
	boolean hasErrorOnTop();
	boolean topAccepts();
}
