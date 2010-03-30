package ee.stacc.productivity.edsl.crawler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;

import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.StringChoice;
import ee.stacc.productivity.edsl.string.StringConstant;
import ee.stacc.productivity.edsl.string.StringRandomInteger;
import ee.stacc.productivity.edsl.string.StringSequence;
import ee.stacc.productivity.edsl.tracker.NameUsage;
import ee.stacc.productivity.edsl.tracker.VariableTracker;

public class NewASE {
	
	private IAbstractString eval(Expression node) {
		ITypeBinding type = node.resolveTypeBinding();
		assert type != null;
		
		if (type.getName().equals("int")) {
			return new StringRandomInteger(PositionUtil.getPosition(node));
		}
		else if (node instanceof StringLiteral) {
			StringLiteral stringLiteral = (StringLiteral)node;
			StringConstant stringConstant = new StringConstant(PositionUtil.getPosition(node), 
					stringLiteral.getLiteralValue(), stringLiteral.getEscapedValue());
			return stringConstant;
		}
		else if (node instanceof CharacterLiteral) {
			CharacterLiteral characterLiteral = (CharacterLiteral)node;
			StringConstant stringConstant = new StringConstant(PositionUtil.getPosition(node), 
					String.valueOf(characterLiteral.charValue()), characterLiteral.getEscapedValue());
			return stringConstant;
		}
		else if (node instanceof Name) {
			return evalName((Name)node); 
		}
		else if (node instanceof ConditionalExpression) {
			return new StringChoice(PositionUtil.getPosition(node),
					eval(((ConditionalExpression)node).getThenExpression()),
					eval(((ConditionalExpression)node).getElseExpression()));
		}
		else if (node instanceof ParenthesizedExpression) {
			return eval(((ParenthesizedExpression)node).getExpression());
		}
		else if (node instanceof InfixExpression) {
			return evalInfix((InfixExpression)node);
		}
		else if (node instanceof MethodInvocation) {
			return evalInvocationResult((MethodInvocation)node);
		}
		else if (node instanceof ClassInstanceCreation) {
			return evalClassInstanceCreation((ClassInstanceCreation)node);
		}
		else {
			throw new UnsupportedStringOpEx
				("getValOf(" + node.getClass().getName() + ")");
		}
	}

	private IAbstractString evalInvocationResult(MethodInvocation inv) {
		// get all possible methods
		// get parameterised abs-string for each
		// apply arguments to each
		// return choice
		return null;
	}

	private IAbstractString evalClassInstanceCreation(ClassInstanceCreation node) {
		assert (isStringBuilderOrBuffer(node.resolveTypeBinding()));
		if (node.arguments().size() == 1) {
			Expression arg = (Expression)node.arguments().get(0);
			// string initializer
			if (arg.resolveTypeBinding().getName().equals("String")) {
				return eval(arg);
			}
			else if (arg.resolveTypeBinding().getName().equals("int")) {
				StringConstant stringConstant = new StringConstant(PositionUtil.getPosition(node), 
						"", "");
				return stringConstant;
			}
			else { // CharSequence
				throw new UnsupportedStringOpEx("Unknown StringBuilder/Buffer constructor: " 
						+ arg.resolveTypeBinding().getName());
			}
		}
		else {
			assert node.arguments().size() == 0;
			StringConstant stringConstant = new StringConstant(PositionUtil.getPosition(node), 
					"", "");
			return stringConstant;
		}
	}

	private IAbstractString evalInfix(InfixExpression expr) {
		if (expr.getOperator() == InfixExpression.Operator.PLUS) {
			List<IAbstractString> ops = new ArrayList<IAbstractString>();
			ops.add(eval(expr.getLeftOperand()));
			ops.add(eval(expr.getRightOperand()));
			for (Object operand: expr.extendedOperands()) {
				ops.add(eval((Expression)operand));
			}
			return new StringSequence(PositionUtil.getPosition(expr), ops);
		}
		else {
			throw new UnsupportedStringOpEx
				("getValOf( infix op = " + expr.getOperator() + ")");
		}
	}
	
	private static boolean isStringBuilderOrBuffer(ITypeBinding typeBinding) {
		return typeBinding.getQualifiedName().equals("java.lang.StringBuffer")
		|| typeBinding.getQualifiedName().equals("java.lang.StringBuilder");
	}
	
	private IAbstractString evalName(Name name) {
		List<IAbstractString> result = new ArrayList<IAbstractString>();
		
		for (NameUsage usage : VariableTracker.getPrecedingOccurrences(name)) {
			result.add(getUsageResult(usage));
		}
		
		return new StringChoice(result);
	}
	
	private IAbstractString getUsageResult(NameUsage usage) {
		/*
		if (usage is assignment) {
			return eval(usage.assExpr);
		}
		else if (usage is sb.append) {
			return seq(eval(usage.name), eval(usage.expr));
		}
		*/
		return null;
	}
	

}
