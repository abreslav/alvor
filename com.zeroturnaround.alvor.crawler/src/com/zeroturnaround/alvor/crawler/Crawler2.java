package com.zeroturnaround.alvor.crawler;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.zeroturnaround.alvor.common.FunctionPatternReference;
import com.zeroturnaround.alvor.common.NodeDescriptor;
import com.zeroturnaround.alvor.common.HotspotPatternReference;
import com.zeroturnaround.alvor.common.StringNodeDescriptor;
import com.zeroturnaround.alvor.common.UnsupportedNodeDescriptor;
import com.zeroturnaround.alvor.common.UnsupportedStringOpEx;
import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.crawler.util.ASTUtil;
import com.zeroturnaround.alvor.crawler.util.JavaModelUtil;
import com.zeroturnaround.alvor.crawler.util.UnsupportedStringOpExAtNode;
import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.IPosition;
import com.zeroturnaround.alvor.string.StringChoice;
import com.zeroturnaround.alvor.string.StringConstant;
import com.zeroturnaround.alvor.string.StringRandomInteger;
import com.zeroturnaround.alvor.string.StringRecursion;
import com.zeroturnaround.alvor.string.StringSequence;
import com.zeroturnaround.alvor.string.util.ArgumentApplier;
import com.zeroturnaround.alvor.tracker.NameAssignment;
import com.zeroturnaround.alvor.tracker.NameInArgument;
import com.zeroturnaround.alvor.tracker.NameInMethodCallExpression;
import com.zeroturnaround.alvor.tracker.NameInParameter;
import com.zeroturnaround.alvor.tracker.NameUsage;
import com.zeroturnaround.alvor.tracker.NameUsageChoice;
import com.zeroturnaround.alvor.tracker.VariableTracker;

public class Crawler2 {
	private static final ILog LOG = Logs.getLog(Crawler2.class);
	private static boolean optimizeChoice = false;
	
	public final static Crawler2 INSTANCE = new Crawler2();
	
	public NodeDescriptor evaluate(Expression node) {
		try {
			IAbstractString str = eval(node, null); 
			return new StringNodeDescriptor(ASTUtil.getPosition(node), str);
		} catch (UnsupportedStringOpEx e) {
			return new UnsupportedNodeDescriptor(ASTUtil.getPosition(node), 
					e.getMessage(), e.getPosition());
		}
	}

	private IAbstractString eval(Expression node, ContextLink context) {
		// recursion check
		IPosition pos = ASTUtil.getPosition(node);
		if (context != null && context.contains(pos)) { 
			// ie. i'm already computing the value of this node lower in the call stack
			// ie. it's recursion!
			return new StringRecursion(pos);			
		}

		ITypeBinding type = node.resolveTypeBinding();
		assert type != null;

		if (	type.getQualifiedName().equals("int") 
				|| type.getQualifiedName().equals("byte")
				|| type.getQualifiedName().equals("long")
				|| type.getQualifiedName().equals("short")
				|| type.getQualifiedName().equals("java.lang.Integer")
				|| type.getQualifiedName().equals("java.lang.Byte")
				|| type.getQualifiedName().equals("java.lang.Long")
				|| type.getQualifiedName().equals("java.lang.Short")
				|| type.getQualifiedName().equals("java.math.BigInteger")
				|| type.getQualifiedName().equals("java.math.BigDecimal")
		) {
			return new StringRandomInteger(ASTUtil.getPosition(node));
		}
		else if (node instanceof StringLiteral) {
			StringLiteral stringLiteral = (StringLiteral)node;
			return new StringConstant(ASTUtil.getPosition(node), 
					stringLiteral.getLiteralValue(), stringLiteral.getEscapedValue());
		}
		else if (node instanceof CharacterLiteral) {
			CharacterLiteral characterLiteral = (CharacterLiteral)node;
			return new StringConstant(ASTUtil.getPosition(node), 
					String.valueOf(characterLiteral.charValue()), characterLiteral.getEscapedValue());
		}
		else if (node instanceof NullLiteral) {
			return new StringConstant(ASTUtil.getPosition(node),
					"null", "\"null\"");
		}
		else if (node instanceof BooleanLiteral) {
			BooleanLiteral bl = (BooleanLiteral)node;
			String bStr;
			if (bl.booleanValue() == true) {
				bStr = "true";
			}
			else {
				bStr = "false";
			}
			return new StringConstant(ASTUtil.getPosition(node),
					bStr, "\"" + bStr + "\"");
		}
		else if (node instanceof Name) {
			if (!ASTUtil.isStringOrStringBuilderOrBuffer(type)) {
				throw new UnsupportedStringOpExAtNode("Unsupported type of Name: " + type.getQualifiedName(), node);
			}
			return evalName((Name)node, context);
		}
		else if (node instanceof CastExpression) {
			CastExpression cExp = (CastExpression)node;
			LOG.message("CAST expression: " + cExp + ", cast type=" + cExp.getType()
					+ ", exp type=" + cExp.getExpression().resolveTypeBinding().getQualifiedName());
			// try evaluating content
			return eval(cExp.getExpression(), context);
		}
		else if (node instanceof ConditionalExpression) {
			StringChoice choice = new StringChoice(ASTUtil.getPosition(node),
					eval(((ConditionalExpression)node).getThenExpression(), new ContextLink(node, context)),
					eval(((ConditionalExpression)node).getElseExpression(), new ContextLink(node, context)));

			if (optimizeChoice /*&& !choice.containsRecursion()*/) {
				// Recursion removal procedure assumes certain structure
				// TODO
				return StringConverter.optimizeChoice(choice);
			} else {
				return choice;
			}
		}
		else if (node instanceof ParenthesizedExpression) {
			return eval(((ParenthesizedExpression)node).getExpression(), new ContextLink(node, context));
		}
		else if (node instanceof InfixExpression) {
			return evalInfix((InfixExpression)node, context);
		}
		else if (node instanceof MethodInvocation) {
			return evalInvocationResult((MethodInvocation)node, context);
		}
		else if (node instanceof ClassInstanceCreation) {
			return evalClassInstanceCreation((ClassInstanceCreation)node, context);
		}
		else {
			throw new UnsupportedStringOpExAtNode("getValOf(" + node.getClass().getName() + ")", node);
		}
	}
	

	private IAbstractString evalInvocationResult(MethodInvocation inv, ContextLink context) {
		
		// First handle special methods
		if (inv.getExpression() != null
				&& ASTUtil.isStringOrStringBuilderOrBuffer(inv.getExpression().resolveTypeBinding())) {
			if (inv.getName().getIdentifier().equals("toString")) {
				return eval(inv.getExpression(), new ContextLink(inv, context));
			}
			else if (inv.getName().getIdentifier().equals("append")) {
				return new StringSequence(
						ASTUtil.getPosition(inv), 
						eval(inv.getExpression(), new ContextLink(inv, context)),
						eval((Expression)inv.arguments().get(0), new ContextLink(inv, context)));
			}
			else if (inv.getName().getIdentifier().equals("valueOf")) {
				assert (ASTUtil.isString(inv.getExpression().resolveTypeBinding()));
				return eval((Expression)inv.arguments().get(0), new ContextLink(inv, context));
			}
			else {
				throw new UnsupportedStringOpExAtNode("String/Builder/Buffer, method=" 
						+ inv.getName().getIdentifier(), inv); 
			}
		}
		if (inv.getExpression() != null
				&& ASTUtil.isIntegral(inv.getExpression().resolveTypeBinding())
				&& inv.getName().getIdentifier().equals("toString")) {
			return new StringRandomInteger(ASTUtil.getPosition(inv));
		}
		
		// handle as general method
		else  {
			return evalInvocationResultOrArgOut(inv, -1, context);
		}			
	}
	
	private IAbstractString evalInvocationResultOrArgOut(MethodInvocation inv,
			int resultArgumentIndex, ContextLink context) {
		
		// TODO need to handle overloading
		String className = inv.resolveMethodBinding().getDeclaringClass().getQualifiedName();
		String methodName = inv.getName().getIdentifier();
		
		// evaluate arguments
		Map<Integer, IAbstractString> inputArguments = new HashMap<Integer, IAbstractString>();
		
		for (int i = 0; i < inv.arguments().size(); i++) {
			Expression arg = (Expression)inv.arguments().get(i);
			ITypeBinding typ = arg.resolveTypeBinding();
			if (ASTUtil.isStringOrStringBuilderOrBuffer(typ)) {
				// using 1-based indexing
				inputArguments.put(i+1, this.eval(arg, new ContextLink(inv, context)));
			}
		}
		
		return new FunctionPatternReference(ASTUtil.getPosition(inv), 
				className, methodName, resultArgumentIndex, inputArguments);
	}

	private IAbstractString evalName(Name name, ContextLink context) {
		IVariableBinding var = (IVariableBinding)name.resolveBinding();
		
		if (var.isField()) {
			return new StringChoice(ASTUtil.getPosition(name));
			//throw new RuntimeException();
			// FIXME
//			return evalField(var, context);
		}
		else {
			return evalNameBefore(name, name, context);
		}
	}

	private IAbstractString evalNameBefore(Name name, ASTNode target, ContextLink context) {
		NameUsage usage = VariableTracker.getLastReachingMod
			((IVariableBinding)name.resolveBinding(), target);
		return evalNameAfter(name, usage, new ContextLink(name, context)); 
	}

	private IAbstractString evalNameAfter(Name name, NameUsage usage, ContextLink context) {
		if (usage == null) {
			throw new UnsupportedStringOpEx("internal error: Can't find definition for '" + name + "'", 
					ASTUtil.getPosition(name));
		}
		assert usage.getNode() != null;
		
		
		if (usage instanceof NameUsageChoice) {
			return evalNameAfterUsageChoice(name, (NameUsageChoice)usage, context);
		}
		else if (usage instanceof NameAssignment) {
			return evalNameAfterAssignment(name, (NameAssignment)usage, context);
		}
		else if (usage instanceof NameInParameter) {
			IPosition pos = ASTUtil.getPosition(usage.getNode());
			NameInParameter nip = (NameInParameter) usage; 
			return new HotspotPatternReference(pos, 
					nip.getMethodDecl().resolveBinding().getDeclaringClass().getQualifiedName(),
					nip.getMethodDecl().getName().getIdentifier(),
					nip.getIndex());
		}
		else if (usage instanceof NameInArgument) {
			return new StringChoice(ASTUtil.getPosition(usage.getNode()));
			//throw new RuntimeException();
			// FIXME
//			return evalNameAfterBeingArgument(name, (NameInArgument)usage, context);
		}
		else if (usage instanceof NameInMethodCallExpression) {
			return evalNameAfterCallingItsMethod(name, (NameInMethodCallExpression)usage, context);
		}
		else {
			throw new UnsupportedStringOpExAtNode("Unsupported NameUsage: " + usage.getClass(), usage.getNode());
		}
	}
	
	
	private IAbstractString evalNameAfterUsageChoice(Name name, NameUsageChoice uc, ContextLink context) {
		IAbstractString thenStr;
		if (uc.getThenUsage() == null) {
			thenStr = this.evalNameBefore(name, uc.getNode(), context); // eval before if statement
		}
		else {
			 thenStr = this.evalNameAfter(name, uc.getThenUsage(), context);
		}
		
		IAbstractString elseStr;
		if (uc.getElseUsage() == null) {
			elseStr = this.evalNameBefore(name, uc.getNode(), context); // eval before if statement
		}
		else {
			elseStr = this.evalNameAfter(name, uc.getElseUsage(), context);
		}
		
		StringChoice result = new StringChoice(ASTUtil.getPosition(uc.getNode()),
				thenStr, elseStr); 
		
		if (optimizeChoice) {
			return StringConverter.optimizeChoice(result);
		} else {
			return result;
		}
	}
	

	private IAbstractString evalInfix(InfixExpression expr, ContextLink context) {
		if (expr.getOperator() == InfixExpression.Operator.PLUS) {
			List<IAbstractString> ops = new ArrayList<IAbstractString>();
			ops.add(eval(expr.getLeftOperand(), new ContextLink(expr, context)));
			ops.add(eval(expr.getRightOperand(), new ContextLink(expr, context)));
			for (Object operand: expr.extendedOperands()) {
				ops.add(eval((Expression)operand, new ContextLink(expr, context)));
			}
			return new StringSequence(ASTUtil.getPosition(expr), ops);
		}
		else {
			throw new UnsupportedStringOpExAtNode("getValOf( infix op = " + expr.getOperator() + ")", expr);
		}
	}
	
	private IAbstractString evalClassInstanceCreation(ClassInstanceCreation node, ContextLink context) {
		if (!ASTUtil.isStringOrStringBuilderOrBuffer(node.resolveTypeBinding())) {
			throw new UnsupportedStringOpExAtNode("Unsupported type in class instance creation: "
					+ node.resolveTypeBinding().getQualifiedName(), node);
		}
		if (node.arguments().size() == 1) {
			Expression arg = (Expression)node.arguments().get(0);
			// string initializer
			if (arg.resolveTypeBinding().getName().equals("String")) {
				return eval(arg, context);
			}
			else if (arg.resolveTypeBinding().getName().equals("int")) {
				return new EmptyStringConstant(ASTUtil.getPosition(node));
			}
			else { // CharSequence
				throw new UnsupportedStringOpExAtNode("Unknown String/StringBuilder/Buffer constructor: " 
						+ arg.resolveTypeBinding().getName(), node);
			}
		}
		else {
			assert node.arguments().size() == 0;
			return new EmptyStringConstant(ASTUtil.getPosition(node));
		}
	}

	private IAbstractString evalNameAfterAssignment(Name name, NameAssignment usage, ContextLink context) {
		if (usage.getOperator() == Assignment.Operator.ASSIGN) {
			return eval(usage.getRightHandSide(), context);
		}
		else if (usage.getOperator() == Assignment.Operator.PLUS_ASSIGN) {
			// extra recursion check because concatenation expression is implicit
			// and i can't pass it's node to eval (for common recursion check)
			IPosition pos = ASTUtil.getPosition(usage.getNode());
			if (context != null && context.contains(pos)) { 
				// ie. i'm already computing the value of this node lower in the call stack
				// ie. it's recursion!
				return new StringRecursion(pos);			
			}
			
			return new StringSequence(
					ASTUtil.getPosition(usage.getNode()),
					eval(usage.getLeftHandSide(), new ContextLink(usage.getNode(), context)),
					eval(usage.getRightHandSide(), new ContextLink(usage.getNode(), context)));
		}
		else {
			throw new UnsupportedStringOpExAtNode("Unknown assignment operator: " + usage.getOperator(), usage.getNode());
		}
	}
	/*
	 * Meant for analyzing mutating method calls on name
	 */
	private IAbstractString evalNameAfterCallingItsMethod(Name name, 
			NameInMethodCallExpression usage, ContextLink context) {
		if (ASTUtil.isString(name.resolveTypeBinding())) {
			// this usage doesn't affect it
			return evalNameBefore(name, usage.getNode(), context);
		}
		
		else {
			assert ASTUtil.isStringBuilderOrBuffer(name.resolveTypeBinding());
			MethodInvocation inv = usage.getInv();
			String methodName = inv.getName().getIdentifier(); 
			
			if (methodName.equals("append")) {
				// extra recursion check because concatenation expression is implicit
				// and i can't pass it's node to eval (for normal recursion check)
				IPosition pos = ASTUtil.getPosition(inv);
				if (context != null && context.contains(pos)) { 
					return new StringRecursion(pos);			
				}
				
				return new StringSequence(
						ASTUtil.getPosition(inv),
						eval(inv.getExpression(), new ContextLink(inv, context)),
						eval((Expression)inv.arguments().get(0), new ContextLink(inv, context)));
			}
			// non-modifying method calls
			else if (methodName.equals("toString")
					|| methodName.equals("capacity")
					|| methodName.equals("charAt")
					|| methodName.equals("codePointAt")
					|| methodName.equals("codePointBefore")
					|| methodName.equals("codePointCount")
					|| methodName.equals("ensureCapacity")
					|| methodName.equals("getChars")
					|| methodName.equals("indexOf")
					|| methodName.equals("lastIndexOf")
					|| methodName.equals("length")
					|| methodName.equals("offsetByCodePoints")
					|| methodName.equals("subSequence")
					|| methodName.equals("substring")
					|| methodName.equals("trimToSize")
					) {
				return eval(inv.getExpression(), new ContextLink(inv, context));
			}
			else {
				throw new UnsupportedStringOpExAtNode("Unknown method called on StringBuilder: " 
						+ inv.getName(), inv);
			}
		}
	}
	
	
}


