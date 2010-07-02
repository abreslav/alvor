package ee.stacc.productivity.edsl.gui;

import java.util.Iterator;
import java.util.List;

import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.IAbstractStringVisitor;
import ee.stacc.productivity.edsl.string.StringCharacterSet;
import ee.stacc.productivity.edsl.string.StringChoice;
import ee.stacc.productivity.edsl.string.StringConstant;
import ee.stacc.productivity.edsl.string.StringParameter;
import ee.stacc.productivity.edsl.string.StringRepetition;
import ee.stacc.productivity.edsl.string.StringSequence;

public class CommonNotationRenderer {

	public static String render(IAbstractString string) {
		StringBuilder builder = new StringBuilder();
		string.accept(OPTIMIZER, builder);
		return builder.toString();
	}
	
	private static final IAbstractStringVisitor<Void, StringBuilder> OPTIMIZER  = new IAbstractStringVisitor<Void, StringBuilder>() {
	
		@Override
		public Void visitStringCharacterSet(StringCharacterSet characterSet,
				StringBuilder builder) {
			builder.append("[");
			for (Character c : characterSet.getContents()) {
				builder.append(c);
			}
			builder.append("]");
			return null;
		}

		@Override
		public Void visitStringChoice(StringChoice stringChoice,
				StringBuilder builder) {
			List<IAbstractString> items = stringChoice.getItems();
			if (items.isEmpty()) {
				return null;
			}
			if (items.size() == 1) {
				items.get(0).accept(this, builder);
				return null;
			}
			builder.append("(");
			Iterator<IAbstractString> iterator = items.iterator();
			iterator.next().accept(this, builder);
			while (iterator.hasNext()) {
				builder.append(" | ");
				iterator.next().accept(this, builder);
			}
			builder.append(")");
			return null;
		}

		@Override
		public Void visitStringConstant(StringConstant stringConstant,
				StringBuilder builder) {
			builder.append("\"").append(stringConstant.getConstant()).append("\"");
			return null;
		}

		@Override
		public Void visitStringParameter(StringParameter stringParameter,
				StringBuilder builder) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Void visitStringRepetition(StringRepetition stringRepetition,
				StringBuilder builder) {
			IAbstractString body = stringRepetition.getBody();
			if (body instanceof StringSequence) {
//				StringSequence seq = (StringSequence) body;
				builder.append("(");
				body.accept(this, builder);
				builder.append(")+");
			} else {
				body.accept(this, builder);
				builder.append("+");
			}
			return null;
		}

		@Override
		public Void visitStringSequence(StringSequence stringSequence,
				StringBuilder builder) {
			List<IAbstractString> items = stringSequence.getItems();
			if (items.isEmpty()) {
				return null;
			}
			if (items.size() == 1) {
				items.get(0).accept(this, builder);
				return null;
			}
			Iterator<IAbstractString> iterator = items.iterator();
			iterator.next().accept(this, builder);
			while (iterator.hasNext()) {
				builder.append(" ");
				iterator.next().accept(this, builder);
			}
			return null;
		}
		
	};

}