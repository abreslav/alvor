package ee.stacc.productivity.edsl.sqlparser;

public interface IStackFactory {
	IAbstractStack newStack(IParserState state);
}