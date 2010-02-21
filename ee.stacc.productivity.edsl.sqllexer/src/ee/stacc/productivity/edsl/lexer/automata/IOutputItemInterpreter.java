package ee.stacc.productivity.edsl.lexer.automata;

import java.util.List;

import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractInputItem;
import ee.stacc.productivity.edsl.lexer.alphabet.IAbstractOutputItem;
import ee.stacc.productivity.edsl.lexer.alphabet.ISequence;

public interface IOutputItemInterpreter {
	
	IOutputItemInterpreter ID = new IOutputItemInterpreter() {
		
		@Override
		public ISequence<IAbstractInputItem> processOutputCommands(
				ISequence<IAbstractInputItem> text, IAbstractInputItem inputItem,
				List<IAbstractOutputItem> output, List<IAbstractInputItem> effect) {
			return text;
		}
	};
	
	ISequence<IAbstractInputItem> processOutputCommands(
			ISequence<IAbstractInputItem> text,
			IAbstractInputItem inputItem,
			List<IAbstractOutputItem> output, List<IAbstractInputItem> effect);
}
