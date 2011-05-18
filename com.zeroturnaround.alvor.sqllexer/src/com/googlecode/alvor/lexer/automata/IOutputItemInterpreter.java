package com.googlecode.alvor.lexer.automata;

import java.util.List;

import com.googlecode.alvor.lexer.alphabet.IAbstractInputItem;
import com.googlecode.alvor.lexer.alphabet.IAbstractOutputItem;
import com.googlecode.alvor.lexer.alphabet.ISequence;

/**
 * Executes commands emitted by a transducer as output items. 
 * 
 * @author abreslav
 *
 */
public interface IOutputItemInterpreter {
	
	/**
	 * Empty interpreter: it does nothing, just returns the initial text
	 */
	IOutputItemInterpreter ID = new IOutputItemInterpreter() {
		
		@Override
		public ISequence<IAbstractInputItem> processOutputCommands(
				ISequence<IAbstractInputItem> text, IAbstractInputItem inputItem,
				List<IAbstractOutputItem> output, List<IAbstractInputItem> effect) {
			return text;
		}
	};
	
	/**
	 * Executes a sequence of commands
	 * @param text the text that was accumulated by the transducer before the commands where executed (this text is used to form token contents)
	 * @param inputItem the current input item
	 * @param output the commands corresponding to the input item (they sit on the same transition)
	 * @param effect a sequence of {@link IAbstractInputItem}s that is generated after executing the commands
	 * @return the new text to be remembered by the transducer (see {@param text})
	 */
	ISequence<IAbstractInputItem> processOutputCommands(
			ISequence<IAbstractInputItem> text,
			IAbstractInputItem inputItem,
			List<IAbstractOutputItem> output, List<IAbstractInputItem> effect);
}
