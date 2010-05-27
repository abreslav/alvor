package ee.stacc.productivity.edsl.sqlparser;

public interface IStackFactory<S> {
	S newStack(IParserState state);
}