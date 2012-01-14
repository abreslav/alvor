package com.googlecode.alvor.lexer.automata;

import java.util.List;

import com.googlecode.alvor.lexer.alphabet.BranchingSequence;
import com.googlecode.alvor.lexer.alphabet.IAbstractInputItem;
import com.googlecode.alvor.lexer.alphabet.IAbstractOutputItem;
import com.googlecode.alvor.lexer.alphabet.ISequence;
import com.googlecode.alvor.lexer.alphabet.PushInput;
import com.googlecode.alvor.lexer.alphabet.Token;
import com.googlecode.alvor.lexer.alphabet.Yield;

/**
 * This interpreter supports {@link PushInput} and {@link Yield}, and creates keyword tokens
 * based on the information in {@link AbstractLexer} class (this information was initially got from 
 * "*.keywords" file, and integrated into {@link SQLLexerData} by the generator). 
 * @author abreslav
 *
 */
public class PushYieldInterpreterWithKeywords implements IOutputItemInterpreter {

	//public static PushYieldInterpreterWithKeywords INSTANCE = new PushYieldInterpreterWithKeywords();
	private final AbstractLexer lexer;
	
	public PushYieldInterpreterWithKeywords(AbstractLexer lexer) {
		this.lexer = lexer; 
	}
	
	@Override
	public ISequence<IAbstractInputItem> processOutputCommands(
			ISequence<IAbstractInputItem> text, IAbstractInputItem inputItem,
			List<IAbstractOutputItem> output, List<IAbstractInputItem> effect) {
		ISequence<IAbstractInputItem> result = text;
		for (IAbstractOutputItem command : output) {
			if (command instanceof Yield) {
				Yield yield = (Yield) command;
				int tokenType = yield.getTokenType();
				if (lexer.isIdentifier(tokenType)) {
					tokenType = lexer.getIdentifierTokenType(CharacterUtil.toString(result));
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
