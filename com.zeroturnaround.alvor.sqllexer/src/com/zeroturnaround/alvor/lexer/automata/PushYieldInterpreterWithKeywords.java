package com.zeroturnaround.alvor.lexer.automata;

import java.util.List;

import com.zeroturnaround.alvor.lexer.alphabet.BranchingSequence;
import com.zeroturnaround.alvor.lexer.alphabet.IAbstractInputItem;
import com.zeroturnaround.alvor.lexer.alphabet.IAbstractOutputItem;
import com.zeroturnaround.alvor.lexer.alphabet.ISequence;
import com.zeroturnaround.alvor.lexer.alphabet.PushInput;
import com.zeroturnaround.alvor.lexer.alphabet.Token;
import com.zeroturnaround.alvor.lexer.alphabet.Yield;
import com.zeroturnaround.alvor.lexer.sql.SQLLexer;
import com.zeroturnaround.alvor.sqllexer.SQLLexerData;

/**
 * This interpreter supports {@link PushInput} and {@link Yield}, and creates keyword tokens
 * based on the information in {@link SQLLexer} class (this information was initially got from 
 * "*.keywords" file, and integrated into {@link SQLLexerData} by the generator). 
 * @author abreslav
 *
 */
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
