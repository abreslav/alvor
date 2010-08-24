package com.zeroturnaround.alvor.lexer.automata;

import java.util.List;

import com.zeroturnaround.alvor.lexer.alphabet.BranchingSequence;
import com.zeroturnaround.alvor.lexer.alphabet.IAbstractInputItem;
import com.zeroturnaround.alvor.lexer.alphabet.IAbstractOutputItem;
import com.zeroturnaround.alvor.lexer.alphabet.ISequence;
import com.zeroturnaround.alvor.lexer.alphabet.PushInput;
import com.zeroturnaround.alvor.lexer.alphabet.Token;
import com.zeroturnaround.alvor.lexer.alphabet.Yield;

/**
 * This interpreter supports {@link PushInput} and {@link Yield}. 
 * 
 * @author abreslav
 *
 */
public class PushYieldInterpreter implements IOutputItemInterpreter {

	public static PushYieldInterpreter INSTANCE = new PushYieldInterpreter();
	
	private PushYieldInterpreter() {}
	
	@Override
	public ISequence<IAbstractInputItem> processOutputCommands(
			ISequence<IAbstractInputItem> text, IAbstractInputItem inputItem,
			List<IAbstractOutputItem> output, List<IAbstractInputItem> effect) {
		ISequence<IAbstractInputItem> result = text;
		for (IAbstractOutputItem command : output) {
			if (command instanceof Yield) {
				Yield yield = (Yield) command;
				Token token = Token.create(yield.getTokenType(), result);
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
