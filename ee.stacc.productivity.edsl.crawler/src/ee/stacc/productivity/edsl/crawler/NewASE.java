package ee.stacc.productivity.edsl.crawler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
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
import org.eclipse.jdt.internal.core.NameLookup;

import ee.stacc.productivity.edsl.string.AbstractStringCollection;
import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.StringCharacterSet;
import ee.stacc.productivity.edsl.string.StringChoice;
import ee.stacc.productivity.edsl.string.StringConstant;
import ee.stacc.productivity.edsl.string.StringParameter;
import ee.stacc.productivity.edsl.string.StringRandomInteger;
import ee.stacc.productivity.edsl.string.StringRepetition;
import ee.stacc.productivity.edsl.string.StringSequence;
import ee.stacc.productivity.edsl.tracker.NameInArgument;
import ee.stacc.productivity.edsl.tracker.NameInParameter;
import ee.stacc.productivity.edsl.tracker.NameMethodCall;
import ee.stacc.productivity.edsl.tracker.NameUsage;
import ee.stacc.productivity.edsl.tracker.NameUsageChoice;
import ee.stacc.productivity.edsl.tracker.NameAssignment;
import ee.stacc.productivity.edsl.tracker.NameUsageLoopChoice;
import ee.stacc.productivity.edsl.tracker.VarTrack;


// TODO test Expression.resolveConstantExpressionValue

public class NewASE {
	private int maxLevel = 2;
	private boolean supportParameters = true;
	private boolean supportInvocations = true;
	
	private int level;
	private MethodInvocation invocationContext;
	private IJavaElement scope;
	
	private NameUsage startingPlace;
	private Statement mainBlock;
	
	private NewASE(int level, MethodInvocation invocationContext, IJavaElement scope) {
		
		if (level > maxLevel) {
			throw new UnsupportedStringOpEx("Analysis level (" + level + ") too deep");
		}
		
		this.level = level;
		this.invocationContext = invocationContext;
		this.scope = scope;
	}
	
	public static IAbstractString evaluateExpression(Expression node) {
		NewASE evaluator = 
			new NewASE(0, null, ASTUtil.getNodeProject(node));
		return evaluator.eval(node);
	}
	
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
			NameUsage usage = VarTrack.getLastMod((Name)node);
			return evalNameAfterUsage(usage); 
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
	
	IAbstractString widenToRegular(IAbstractString str) {
		if (hasRecursiveChoice(str)) {
			throw new UnsupportedStringOpEx("RecursiveChoice");
		} else {
			// first see if there are some null-s
			// replace them with str
			return str;
		}
	}
	
	boolean hasRecursiveChoice(IAbstractString str) {
		if (str instanceof StringConstant) {
			return false;
		}
		else if (str instanceof StringCharacterSet) {
			return false;
		}
		else if (str instanceof StringParameter) {
			return false;
		}
		else if (str instanceof StringRepetition) {
			return hasRecursiveChoice(((StringRepetition)str).getBody());
		}
		else if (str instanceof AbstractStringCollection) {
			for (IAbstractString as : ((AbstractStringCollection)str).getItems()) {
				if (hasRecursiveChoice(as)) {
					return true;
				}
			}
			return false;
		}
		throw new IllegalArgumentException();
	}
	
	IAbstractString evalNameAfterUsage(NameUsage usage) {
		// check, if it's necessary to start new evaluator (entering the loop from below) 
		if (usage.getMainStatement() != this.mainBlock
				// and if usage.mainStmt inside this.mainBlock and it's a loop then 
				) {
			NewASE newEval = new NewASE(level, invocationContext, scope);
			newEval.mainBlock = usage.getMainStatement();
			newEval.startingPlace = usage;
			return widenToRegular(newEval.evalNameAfterUsage(usage));
		}
		
		// can use this evaluator
		if (usage instanceof NameUsageChoice) {
			NameUsageChoice uc = (NameUsageChoice)usage;
			/*
			List<IAbstractString> stringChoices = new ArrayList<IAbstractString>();
			for (NameUsage usageItem : ((NameUsageChoice)usage).getChoices()) {
				stringChoices.add(this.evalNameAfterUsage(usageItem));
			}
			*/
			return new StringChoice(this.evalNameAfterUsage(uc.getThenUsage()),
					this.evalNameAfterUsage(uc.getElseUsage())); 
		}
		else if (usage instanceof NameUsageLoopChoice) {
			NameUsageLoopChoice loopChoice = (NameUsageLoopChoice)usage;
			
			if (loopChoice.getLoopUsage() == this.startingPlace) {
				IAbstractString baseString = evalNameAfterUsage(loopChoice.getBaseUsage());
				return new RecursiveStringChoice(baseString, null); // null means outermost string TODO too ugly
			}
			else {
				throw new UnsupportedStringOpEx("NameUsageLoopChoice weird case");
			}
		}
		else if (usage instanceof NameAssignment) {
			return eval(((NameAssignment) usage).getValueExpression());
		}
		else if (usage instanceof NameInParameter) {
			return new StringParameter(((NameInParameter)usage).getIndex());
		}
		else if (usage instanceof NameInArgument) {
			// if (stringBuffer) {
				// get abstract representation of respective parameter of the method
				// apply this to real arguments and return
			// }
			// else goto previous usage place
			throw new UnsupportedStringOpEx("NameInArgument");
		}
		else if (usage instanceof NameMethodCall) {
			// get abstract representation of return value of the method
			// apply this to real arguments and return
			throw new UnsupportedStringOpEx("NameMethodCall");
		}
		else {
			throw new UnsupportedStringOpEx("Unsupported NameUsage: " + usage.getClass());
		}
	}
	

}
