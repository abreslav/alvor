package ee.stacc.productivity.edsl.lexer.automata;

import java.util.List;

import ee.stacc.productivity.edsl.lexer.alphabet.BranchingSequence;
import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;
import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractOutputItem;
import ee.stacc.productivity.edsl.lexer.alphabet.ISequence;
import ee.stacc.productivity.edsl.lexer.alphabet.PushInput;
import ee.stacc.productivity.edsl.lexer.alphabet.Token;
import ee.stacc.productivity.edsl.lexer.alphabet.Yield;
import ee.stacc.productivity.edsl.lexer.sql.SQLLexer;

public class PushYieldInterpreterWithKeywords implements IOutputItemInterpreter {

	public static PushYieldInterpreterWithKeywords INSTANCE = new PushYieldInterpreterWithKeywords();
	
	private PushYieldInterpreterWithKeywords() {}
	
	@Override
	public ISequence<IAbstractInputItem> processOutputCommands(
			ISequence<IAbstractInputItem> text, IAbstractInputItem inputItem,
			List<IAbstractOutputItem> output, List<IAbstractInputItem> effect) {
		ISequence<IAbstractInputItem> result = text;
		for (IAbstractOutputItem command : output) {
			if (command instanceof Yield) {
				Yield yield = (Yield) command;
				int tokenType = yield.getTokenType();
				if (SQLLexer.isIdentifier(tokenType)) {
					tokenType = SQLLexer.getIdentifierTokenType(CharacterUtil.toString(result));
				}
				Token token = Token.create(tokenType, result);
				effect.add(token);
				result = new BranchingSequence<IAbstractInputItem>();
			} else if (command instanceof PushInput) {
				result = result.append(inputItem);
			} else {
				throw new IllegalArgumentException("Unsupported command: " + command);
			}
		}
		return result;
	}

}
