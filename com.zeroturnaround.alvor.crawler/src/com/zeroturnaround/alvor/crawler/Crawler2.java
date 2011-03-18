package com.zeroturnaround.alvor.crawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TagElement;

import com.zeroturnaround.alvor.common.EmptyStringConstant;
import com.zeroturnaround.alvor.common.FunctionPattern;
import com.zeroturnaround.alvor.common.FunctionPatternReference;
import com.zeroturnaround.alvor.common.HotspotPattern;
import com.zeroturnaround.alvor.common.HotspotPatternReference;
import com.zeroturnaround.alvor.common.HotspotDescriptor;
import com.zeroturnaround.alvor.common.PositionUtil;
import com.zeroturnaround.alvor.common.RecursionConverter;
import com.zeroturnaround.alvor.common.StringNodeDescriptor;
import com.zeroturnaround.alvor.common.UnsupportedNodeDescriptor;
import com.zeroturnaround.alvor.common.UnsupportedStringOpEx;
import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.crawler.util.ASTUtil;
import com.zeroturnaround.alvor.crawler.util.UnsupportedStringOpExAtNode;
import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.IPosition;
import com.zeroturnaround.alvor.string.StringChoice;
import com.zeroturnaround.alvor.string.StringConstant;
import com.zeroturnaround.alvor.string.StringRandomInteger;
import com.zeroturnaround.alvor.string.StringRecursion;
import com.zeroturnaround.alvor.string.StringSequence;
import com.zeroturnaround.alvor.tracker.NameAssignment;
import com.zeroturnaround.alvor.tracker.NameInArgument;
import com.zeroturnaround.alvor.tracker.NameInMethodCallExpression;
import com.zeroturnaround.alvor.tracker.NameInParameter;
import com.zeroturnaround.alvor.tracker.NameUsage;
import com.zeroturnaround.alvor.tracker.NameUsageChoice;
import com.zeroturnaround.alvor.tracker.VariableTracker;

public class Crawler2 {
	private static final ILog LOG = Logs.getLog(Crawler2.class);
	private static final String RESULT_FOR_SQL_CHECKER = "@ResultForSQLChecker";
	private static boolean optimizeChoice = false;
	
	public final static Crawler2 INSTANCE = new Crawler2();
	
	public HotspotDescriptor evaluate(Expression node) {
		try {
			IAbstractString str = removeRecursion(eval(node, null));
			
			return new StringNodeDescriptor(ASTUtil.getPosition(node), str);
		} catch (UnsupportedStringOpEx e) {
			return new UnsupportedNodeDescriptor(ASTUtil.getPosition(node), 
					e.getMessage(), e.getPosition());
		}
	}

	private IAbstractString eval(Expression node, NodePositionList context) {
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
					eval(((ConditionalExpression)node).getThenExpression(), new NodePositionList(node, context)),
					eval(((ConditionalExpression)node).getElseExpression(), new NodePositionList(node, context)));

			if (optimizeChoice /*&& !choice.containsRecursion()*/) {
				// Recursion removal procedure assumes certain structure
				// TODO
				return StringConverter.optimizeChoice(choice);
			} else {
				return choice;
			}
		}
		else if (node instanceof ParenthesizedExpression) {
			return eval(((ParenthesizedExpression)node).getExpression(), new NodePositionList(node, context));
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
	

	private IAbstractString evalInvocationArgOut(MethodInvocation inv,
			int argumentNo, NodePositionList context) {
		
		IMethodBinding binding = inv.resolveMethodBinding(); 
		String className = binding.getDeclaringClass().getQualifiedName();
		String methodName = inv.getName().getIdentifier();
		
		
		// evaluate arguments
		return new FunctionPatternReference(ASTUtil.getPosition(inv), new FunctionPattern( 
				className, methodName,
				ASTUtil.getSimpleArgumentTypesAsString(binding),
				argumentNo
		), evaluateStringArguments(inv, context));
	}

	private IAbstractString evalInvocationResult(MethodInvocation inv, NodePositionList context) {
		//return evalInvocationResultOrArgOut(inv, -1, context);
		// First handle special methods
		IMethodBinding binding = inv.resolveMethodBinding(); 
		String className = binding.getDeclaringClass().getQualifiedName();
		
		if (inv.getExpression() != null
				&& ASTUtil.isStringOrStringBuilderOrBuffer(inv.getExpression().resolveTypeBinding())) {
			if (inv.getName().getIdentifier().equals("toString")) {
				return eval(inv.getExpression(), new NodePositionList(inv, context));
			}
			else if (inv.getName().getIdentifier().equals("append")) {
				return new StringSequence(
						ASTUtil.getPosition(inv), 
						eval(inv.getExpression(), new NodePositionList(inv, context)),
						eval((Expression)inv.arguments().get(0), new NodePositionList(inv, context)));
			}
			else if (inv.getName().getIdentifier().equals("valueOf")) {
				assert (ASTUtil.isString(inv.getExpression().resolveTypeBinding()));
				return eval((Expression)inv.arguments().get(0), new NodePositionList(inv, context));
			}
			else {
				throw new UnsupportedStringOpExAtNode("String/Builder/Buffer, method=" 
						+ inv.getName().getIdentifier(), inv); 
			}
		}
		// method with numeric result
		else if (inv.getExpression() != null
				&& ASTUtil.isIntegral(inv.getExpression().resolveTypeBinding())
				&& inv.getName().getIdentifier().equals("toString")) {
			return new StringRandomInteger(ASTUtil.getPosition(inv));
		}
		// enum
		else if (inv.getExpression() != null
				// FIXME
				&& className.toLowerCase().contains("enum")) {
			throw new UnsupportedStringOpEx("TODO Enum", ASTUtil.getPosition(inv));
		}
		
		// handle as general method
		else  {
			// TODO need to handle overloading
			String methodName = inv.getName().getIdentifier();
			return new FunctionPatternReference(ASTUtil.getPosition(inv), new FunctionPattern(
					className, methodName, 
					ASTUtil.getSimpleArgumentTypesAsString(binding),
					-1
			), evaluateStringArguments(inv, context));
		}			
	}
	
	private Map<Integer, IAbstractString> evaluateStringArguments(MethodInvocation inv, NodePositionList context) {
		Map<Integer, IAbstractString> inputArguments = new HashMap<Integer, IAbstractString>();
		
		for (int i = 0; i < inv.arguments().size(); i++) {
			Expression arg = (Expression)inv.arguments().get(i);
			ITypeBinding typ = arg.resolveTypeBinding();
			if (ASTUtil.isStringOrStringBuilderOrBuffer(typ)) {
				// using 1-based indexing
				inputArguments.put(i+1, this.eval(arg, new NodePositionList(inv, context)));
			}
		}
		return inputArguments;
	}

	private IAbstractString evalName(Name name, NodePositionList context) {
		ITypeBinding type = name.resolveTypeBinding();
		if (!ASTUtil.isStringOrStringBuilderOrBuffer(type)) {
			throw new UnsupportedStringOpExAtNode("Unsupported type of Name: " + type.getQualifiedName(), name);
		}
		
		IVariableBinding var = (IVariableBinding)name.resolveBinding();
		if (var.isField()) {
			// FIXME
			throw new UnsupportedStringOpExAtNode("reference to field", name);
			// return new StringChoice(ASTUtil.getPosition(name));
			//throw new RuntimeException();
//			return evalField(var, context);
		}
		else {
			return evalNameBefore(name, name, context);
		}
	}

	private IAbstractString evalNameBefore(Name name, ASTNode target, NodePositionList context) {
		NameUsage usage = VariableTracker.getLastReachingMod
			((IVariableBinding)name.resolveBinding(), target);
		return evalNameAfter(name, usage, new NodePositionList(name, context)); 
	}

	private IAbstractString evalNameAfter(Name name, NameUsage usage, NodePositionList context) {
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
			IMethodBinding binding = nip.getMethodDecl().resolveBinding();
			return new HotspotPatternReference(pos, new HotspotPattern( 
					binding.getDeclaringClass().getQualifiedName(),
					binding.getName(),
					ASTUtil.getSimpleArgumentTypesAsString(binding),
					nip.getParameterNo()
			));
		}
		else if (usage instanceof NameInArgument) {
			return evalNameAfterBeingArgument(name, (NameInArgument)usage, context);
		}
		else if (usage instanceof NameInMethodCallExpression) {
			return evalNameAfterCallingItsMethod(name, (NameInMethodCallExpression)usage, context);
		}
		else {
			throw new UnsupportedStringOpExAtNode("Unsupported NameUsage: " + usage.getClass(), usage.getNode());
		}
	}
	
	
	private IAbstractString evalNameAfterBeingArgument(Name name, NameInArgument usage, NodePositionList context) {
		if (ASTUtil.isString(name.resolveTypeBinding())) {
			// this usage doesn't affect it, keep looking
			return evalNameBefore(name, usage.getNode(), context);
		}
		else {
			assert ASTUtil.isStringBuilderOrBuffer(name.resolveTypeBinding());
			return evalInvocationArgOut(usage.getInv(), usage.getArgumentNo(), context); 
		}
	}

	private IAbstractString evalNameAfterUsageChoice(Name name, NameUsageChoice uc, NodePositionList context) {
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
	

	private IAbstractString evalInfix(InfixExpression expr, NodePositionList context) {
		if (expr.getOperator() == InfixExpression.Operator.PLUS) {
			List<IAbstractString> ops = new ArrayList<IAbstractString>();
			ops.add(eval(expr.getLeftOperand(), new NodePositionList(expr, context)));
			ops.add(eval(expr.getRightOperand(), new NodePositionList(expr, context)));
			for (Object operand: expr.extendedOperands()) {
				ops.add(eval((Expression)operand, new NodePositionList(expr, context)));
			}
			return new StringSequence(ASTUtil.getPosition(expr), ops);
		}
		else {
			throw new UnsupportedStringOpExAtNode("getValOf( infix op = " + expr.getOperator() + ")", expr);
		}
	}
	
	private IAbstractString evalClassInstanceCreation(ClassInstanceCreation node, NodePositionList context) {
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

	private IAbstractString evalNameAfterAssignment(Name name, NameAssignment usage, NodePositionList context) {
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
					eval(usage.getLeftHandSide(), new NodePositionList(usage.getNode(), context)),
					eval(usage.getRightHandSide(), new NodePositionList(usage.getNode(), context)));
		}
		else {
			throw new UnsupportedStringOpExAtNode("Unknown assignment operator: " + usage.getOperator(), usage.getNode());
		}
	}
	/*
	 * Meant for analyzing mutating method calls on name
	 */
	private IAbstractString evalNameAfterCallingItsMethod(Name name, 
			NameInMethodCallExpression usage, NodePositionList context) {
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
						eval(inv.getExpression(), new NodePositionList(inv, context)),
						eval((Expression)inv.arguments().get(0), new NodePositionList(inv, context)));
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
				return eval(inv.getExpression(), new NodePositionList(inv, context));
			}
			else {
				throw new UnsupportedStringOpExAtNode("Unknown method called on StringBuilder: " 
						+ inv.getName(), inv);
			}
		}
	}
	
	
	public HotspotDescriptor getMethodTemplateDescriptor(MethodDeclaration decl, int argNo) {
		IPosition pos = ASTUtil.getPosition(decl);
		try {
			IAbstractString str = removeRecursion(getMethodTemplate(decl, argNo));
			return new StringNodeDescriptor(pos, str);
		}
		catch (UnsupportedStringOpEx e) {
			return new UnsupportedNodeDescriptor(pos, e.getMessage(), e.getPosition());
		}
	}
	
	private IAbstractString getMethodTemplate(MethodDeclaration decl, int argNo) {
		if (argNo == -1) {
			return getMethodReturnTemplate(decl);
		}
		else {
			return getMethodArgOutTemplate(decl, argNo);
		}
	}
	
	private IAbstractString getMethodReturnTemplate(MethodDeclaration decl) {
		// if it has @ResultForSQLChecker in JAVADOC then return this
		IAbstractString javadocResult = getMethodReturnValueFromJavadoc(decl);
		if (javadocResult != null) {
			return javadocResult;
		}
		
		assert decl != null;
		
		// find all return statements
		final List<ReturnStatement> returnStmts = new ArrayList<ReturnStatement>();
		ASTVisitor visitor = new ASTVisitor() {
			@Override
			public boolean visit(ReturnStatement node) {
				returnStmts.add(node);
				return true;
			}
		};
		decl.accept(visitor);
		
		// get choice out of different return expressions
		List<IAbstractString> options = new ArrayList<IAbstractString>();
		for (ReturnStatement ret: returnStmts) {
			options.add(eval(ret.getExpression(), null));
		}
		if (options.size() == 1) {
			return options.get(0);
		}
		else {
			return new StringChoice(ASTUtil.getPosition(decl), options);
		}
	}
	
	private IAbstractString getMethodArgOutTemplate(MethodDeclaration decl, int argNo) {
		int argumentIndex0 = argNo-1;
		// TODO: at first look for javadoc annotation for this arg
		Name paramName = ((SingleVariableDeclaration)decl.parameters().get(argumentIndex0)).getName();
		
		NameUsage lastMod = VariableTracker.getLastModIn(
				(IVariableBinding)paramName.resolveBinding(), decl);

		return evalNameAfter(paramName, lastMod, null);
	}

	private IAbstractString getMethodReturnValueFromJavadoc(MethodDeclaration decl) {
		// TODO: allow also specifying result as regex
		
		if (decl.getJavadoc() == null) {
			return null;
		}
		TagElement tag = ASTUtil.getJavadocTag(decl.getJavadoc(), RESULT_FOR_SQL_CHECKER);
		
		if (tag != null) {
			String tagText = ASTUtil.getTagElementText(tag);
			if (tagText == null) {
				throw new UnsupportedStringOpExAtNode("Problem reading " + RESULT_FOR_SQL_CHECKER, decl);
			} else {
				//return new StringConstant(tagText);
				return new StringConstant(ASTUtil.getPosition(tag), 
						tagText, '"'+tagText+'"');
			}
		}
		else {
			return null;
		}
	}
	
	private IAbstractString removeRecursion(IAbstractString str) {
		if (str.containsRecursion()) {
			System.out.println("FOUND RECURSION");
			
			throw new UnsupportedStringOpEx("Recursion", str.getPosition());
//			TODO put back when path-sens is done				
//			return RecursionConverter.recursionToRepetition(str);
		}
		else {
			return str;
		}
	}
	
}


