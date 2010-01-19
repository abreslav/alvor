package ee.stacc.productivity.edsl.string;

public interface IAbstractStringVisitor<R, D> {

	R visitStringCharacterSet(StringCharacterSet characterSet, D data);

	R visitStringChoise(StringChoice stringChoise, D data);

	R visitStringConstant(StringConstant stringConstant, D data);

	R visitStringSequence(StringSequence stringSequence, D data);

	R visitStringRepetition(StringRepetition stringRepetition, D data);

}
