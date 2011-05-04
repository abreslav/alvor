package com.zeroturnaround.alvor.crawler;

import java.lang.reflect.Modifier;
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
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.zeroturnaround.alvor.common.EmptyStringConstant;
import com.zeroturnaround.alvor.common.FieldPattern;
import com.zeroturnaround.alvor.common.FieldPatternReference;
import com.zeroturnaround.alvor.common.FunctionPattern;
import com.zeroturnaround.alvor.common.FunctionPatternReference;
import com.zeroturnaround.alvor.common.HotspotDescriptor;
import com.zeroturnaround.alvor.common.HotspotPattern;
import com.zeroturnaround.alvor.common.HotspotPatternReference;
import com.zeroturnaround.alvor.common.IntegerList;
import com.zeroturnaround.alvor.common.RecursionConverter;
import com.zeroturnaround.alvor.common.StringConverter;
import com.zeroturnaround.alvor.common.StringHotspotDescriptor;
import com.zeroturnaround.alvor.common.UnsupportedHotspotDescriptor;
import com.zeroturnaround.alvor.common.UnsupportedStringOpEx;
import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.crawler.util.ASTUtil;
import com.zeroturnaround.alvor.crawler.util.UnsupportedStringOpExAtNode;
import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.IPosition;
import com.zeroturnaround.alvor.string.Position;
import com.zeroturnaround.alvor.string.StringChoice;
import com.zeroturnaround.alvor.string.StringConstant;
import com.zeroturnaround.alvor.string.StringParameter;
import com.zeroturnaround.alvor.string.StringRandomInteger;
import com.zeroturnaround.alvor.string.StringRecursion;
import com.zeroturnaround.alvor.string.StringSequence;
import com.zeroturnaround.alvor.tracker.NameAssignment;
import com.zeroturnaround.alvor.tracker.NameInArgument;
import com.zeroturnaround.alvor.tracker.NameInMethodCallExpression;
import com.zeroturnaround.alvor.tracker.NameInParameter;
import com.zeroturnaround.alvor.tracker.NameUsage;
import com.zeroturnaround.alvor.tracker.NameUsageChoice;
import com.zeroturnaround.alvor.tracker.UsageFilter;
import com.zeroturnaround.alvor.tracker.VariableTracker;

public class StringExpressionEvaluator {
	private static final ILog LOG = Logs.getLog(StringExpressionEvaluator.class);
	private static final String RESULT_FOR_SQL_CHECKER = "@ResultForSQLChecker";
	private static boolean optimizeChoice = false;
	private static boolean supportRepetition = false;
	public static enum ParamEvalMode {AS_HOTSPOT, AS_PARAM};
	
	public final static StringExpressionEvaluator INSTANCE = new StringExpressionEvaluator();
	private static final int MAX_CONTEXT_DEPTH = 20;
	
	public HotspotDescriptor evaluateFinalField(VariableDeclarationFragment decl) {
		try {
			Expression init = decl.getInitializer();
			if (init == null) {
				throw new UnsupportedStringOpExAtNode("Fields without initializer are not supported", decl);
			}
			IVariableBinding var = decl.resolveBinding();
			if ((var.getModifiers() & Modifier.FINAL) == 0) {
				// FUTURE: support also non-final String-s
				throw new UnsupportedStringOpExAtNode("Non-final fields are not supported: "
						+ var.getDeclaringClass().getName() + '.' + var.getName(), decl);
			}
			IAbstractString str = removeRecursion(eval(init, null, ParamEvalMode.AS_HOTSPOT));
			
			return new StringHotspotDescriptor(ASTUtil.getPosition(decl), str);
		} catch (UnsupportedStringOpEx e) {
			return new UnsupportedHotspotDescriptor(ASTUtil.getPosition(decl), 
					e.getMessage(), e.getPosition());
		}
	}

	public HotspotDescriptor evaluate(Expression node, ParamEvalMode mode) {
		try {
			IAbstractString str = removeRecursion(eval(node, null, mode));
			
			return new StringHotspotDescriptor(ASTUtil.getPosition(node), str);
		} catch (UnsupportedStringOpEx e) {
			return new UnsupportedHotspotDescriptor(ASTUtil.getPosition(node), 
					e.getMessage(), e.getPosition());
		}
	}

	private IAbstractString eval(Expression node, IntegerList context, ParamEvalMode mode) {
		if (context != null && context.getLength() > MAX_CONTEXT_DEPTH) {
			throw new UnsupportedStringOpExAtNode("String analysis too deep", node);
		}
		
		// recursion check
		IPosition pos = ASTUtil.getPosition(node);
		if (context != null && pos != null && context.contains(pos.hashCode())) { 
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
			IPosition actualPos = ASTUtil.getPosition(node);
			return new StringConstant(new Position(actualPos.getPath(), actualPos.getStart()-1, actualPos.getLength()),
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
			return evalName((Name)node, context, mode);
		}
		else if (node instanceof CastExpression) {
			CastExpression cExp = (CastExpression)node;
			LOG.message("CAST expression: " + cExp + ", cast type=" + cExp.getType()
					+ ", exp type=" + cExp.getExpression().resolveTypeBinding().getQualifiedName());
			// try evaluating content
			return eval(cExp.getExpression(), context, mode);
		}
		else if (node instanceof ConditionalExpression) {
			StringChoice choice = new StringChoice(ASTUtil.getPosition(node),
					eval(((ConditionalExpression)node).getThenExpression(), contextOf(node, context), mode),
					eval(((ConditionalExpression)node).getElseExpression(), contextOf(node, context), mode));

			if (optimizeChoice /*&& !choice.containsRecursion()*/) {
				// Recursion removal procedure assumes certain structure
				// TODO
				return StringConverter.optimizeChoice(choice);
			} else {
				return choice;
			}
		}
		else if (node instanceof ParenthesizedExpression) {
			return eval(((ParenthesizedExpression)node).getExpression(), context, mode);
		}
		else if (node instanceof InfixExpression) {
			return evalInfix((InfixExpression)node, context, mode);
		}
		else if (node instanceof MethodInvocation) {
			return evalInvocationResult((MethodInvocation)node, context, mode);
		}
		else if (node instanceof ClassInstanceCreation) {
			return evalClassInstanceCreation((ClassInstanceCreation)node, context, mode);
		}
		else {
			throw new UnsupportedStringOpExAtNode("getValOf(" + node.getClass().getName() + ")", node);
		}
	}
	

	private IAbstractString evalInvocationArgOut(MethodInvocation inv,
			int argumentNo, IntegerList context, ParamEvalMode mode) {
		
		IMethodBinding binding = inv.resolveMethodBinding(); 
		String className = binding.getDeclaringClass().getErasure().getQualifiedName();
		String methodName = inv.getName().getIdentifier();
		
		
		// evaluate arguments
		return new FunctionPatternReference(ASTUtil.getPosition(inv), new FunctionPattern( 
				className, methodName,
				ASTUtil.getSimpleArgumentTypesAsString(binding),
				argumentNo
		), evaluateStringArguments(inv, context, mode));
	}

	private IAbstractString evalInvocationResult(MethodInvocation inv, IntegerList context, ParamEvalMode mode) {
		//return evalInvocationResultOrArgOut(inv, -1, context);
		// First handle special methods
		
		IntegerList invContext = contextOf(inv, context);
		
		IMethodBinding binding = inv.resolveMethodBinding();
		String className = binding.getDeclaringClass().getErasure().getQualifiedName();
		
		if (inv.getExpression() != null
				&& ASTUtil.isStringOrStringBuilderOrBuffer(inv.getExpression().resolveTypeBinding())) {
			if (inv.getName().getIdentifier().equals("toString")) {
				return eval(inv.getExpression(), invContext, mode);
			}
			else if (inv.getName().getIdentifier().equals("append")) {
				return new StringSequence(
						ASTUtil.getPosition(inv), 
						eval(inv.getExpression(), invContext, mode),
						eval((Expression)inv.arguments().get(0), invContext, mode));
			}
			else if (inv.getName().getIdentifier().equals("valueOf")) {
				assert (ASTUtil.isString(inv.getExpression().resolveTypeBinding()));
				return eval((Expression)inv.arguments().get(0), invContext, mode);
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
		// enum.name()
		else if (inv.getExpression() != null
				&& inv.getExpression().resolveTypeBinding().isEnum()) {
			
			if (! inv.getName().getIdentifier().equals("name")) {
				throw new UnsupportedStringOpExAtNode("Only 'name' method is supported with enums", inv);
			}
			
			SimpleName name;
			if (inv.getExpression() instanceof SimpleName) {
				name = (SimpleName)inv.getExpression();
			}
			else if (inv.getExpression() instanceof QualifiedName) {
				name = ((QualifiedName)inv.getExpression()).getName();
			}
			else {
				throw new UnsupportedStringOpExAtNode("Unsupported usage of Enum", inv);
			}
			return new StringConstant(ASTUtil.getPosition(name), name.getIdentifier(),
					"\"" + name.getIdentifier() + "\"");
		}
		// handle as general method
		else  {
			String methodName = inv.getName().getIdentifier();
			return new FunctionPatternReference(ASTUtil.getPosition(inv), new FunctionPattern(
					className, methodName, 
					ASTUtil.getSimpleArgumentTypesAsString(binding),
					-1
			), evaluateStringArguments(inv, context, mode));
		}			
	}
	
	private Map<Integer, IAbstractString> evaluateStringArguments(MethodInvocation inv, 
			IntegerList context, ParamEvalMode mode) {
		Map<Integer, IAbstractString> inputArguments = new HashMap<Integer, IAbstractString>();
		
		for (int i = 0; i < inv.arguments().size(); i++) {
			Expression arg = (Expression)inv.arguments().get(i);
			ITypeBinding typ = arg.resolveTypeBinding();
			if (ASTUtil.isStringOrStringBuilderOrBuffer(typ)) {
				// using 1-based indexing
				inputArguments.put(i+1, this.eval(arg, contextOf(inv, context), mode));
			}
		}
		return inputArguments;
	}

	private IAbstractString evalName(Name name, IntegerList context, ParamEvalMode mode) {
//		LOG.message("EVAL NAME: " + name.getFullyQualifiedName() + " at: " + 
//				PositionUtil.getLineString(ASTUtil.getPosition(name)));
		ITypeBinding type = name.resolveTypeBinding();
		if (!ASTUtil.isStringOrStringBuilderOrBuffer(type)) {
			throw new UnsupportedStringOpExAtNode("Unsupported type of Name: " + type.getQualifiedName(), name);
		}
		
		IVariableBinding var = (IVariableBinding)name.resolveBinding();
		if (var.isField()) {
			return new FieldPatternReference(ASTUtil.getPosition(name),
					new FieldPattern(var.getDeclaringClass().getErasure().getQualifiedName(), var.getName()));
		}
		else {
			
			return evalVarBefore((IVariableBinding)name.resolveBinding(), name, context, mode);
		}
	}

	private IAbstractString evalVarBefore(IVariableBinding var, ASTNode target, 
			IntegerList context, ParamEvalMode mode) {
		
		NameUsage usage = VariableTracker.getLastReachingMod(var, target);
		if (usage == null) {
			throw new UnsupportedStringOpEx("internal error: Can't find definition for '" + var + "'", null);
		}
		
		return evalVarAfter(var, usage, context, mode); 
	}

	private IAbstractString evalVarAfter(IVariableBinding var, NameUsage usage, IntegerList context, ParamEvalMode mode) {
		
		// recursion check
		if (context != null && context.contains(usage.hashCode())) { 
			// ie. i'm already computing the effect of this usage somewhere lower in the call stack
			// ie. it's recursion!
			return new StringRecursion(ASTUtil.getPosition(usage.getMainNode()));			
		}
		
		
		if (usage instanceof NameUsageChoice) {
			return evalVarAfterUsageChoice(var, (NameUsageChoice)usage, context, mode);
		}
		else if (usage instanceof NameAssignment) {
			return evalVarAfterAssignment(var, (NameAssignment)usage, context, mode);
		}
		else if (usage instanceof NameInParameter) {
			return evalVarInParameter(var, (NameInParameter)usage, context, mode);
		}
		else if (usage instanceof NameInArgument) {
			return evalVarAfterBeingArgument(var, (NameInArgument)usage, context, mode);
		}
		else if (usage instanceof NameInMethodCallExpression) {
			return evalVarAfterCallingItsMethod(var, (NameInMethodCallExpression)usage, context, mode);
		}
		else if (usage instanceof UsageFilter) {
			UsageFilter filter = (UsageFilter)usage;
			IAbstractString result = evalVarAfter(var, filter.getMainUsage(), context, mode);
			LOG.message("TODO: Filter variable: " + var.getName());
			// TODO filter fullString according to filter
			if (filter.hasNotNullCondition()) {
				result = StringConverter.removeNullChoice(result);
			}
			return result; 
		}
		else {
			throw new IllegalArgumentException();
		}
	}
	
	
	private IAbstractString evalVarInParameter(IVariableBinding var, NameInParameter usage, 
			IntegerList context, ParamEvalMode mode) {
		
		IPosition pos = ASTUtil.getPosition(usage.getParameterNode());
		
		if (mode == ParamEvalMode.AS_PARAM) { // this means that client is constructing a method template
			return new StringParameter(pos, usage.getParameterNo());
		} 
		else {
			IMethodBinding binding = usage.getMethodDecl().resolveBinding();
			return new HotspotPatternReference(pos, new HotspotPattern( 
					binding.getDeclaringClass().getErasure().getQualifiedName(),
					binding.getName(),
					ASTUtil.getSimpleArgumentTypesAsString(binding),
					usage.getParameterNo()
			));
		}
	}

	private IAbstractString evalVarAfterBeingArgument(IVariableBinding var, NameInArgument usage, IntegerList context, ParamEvalMode mode) {
		if (ASTUtil.isString(var.getType())) {
			// this usage doesn't affect it, keep looking
			return evalVarBefore(var, usage.getInvocation(), context, mode);
		}
		else {
			assert ASTUtil.isStringBuilderOrBuffer(var.getType());
			
			// TODO does it need new context ?
			return evalInvocationArgOut(usage.getInvocation(), usage.getArgumentNo(), contextOf(usage, context), mode); 
		}
	}

	private IAbstractString evalVarAfterUsageChoice(IVariableBinding var, NameUsageChoice uc, IntegerList context, ParamEvalMode mode) {
		
		IntegerList newContext = contextOf(uc, context);
		
		IAbstractString thenStr;
		if (uc.getThenUsage() == null) {
			thenStr = this.evalVarBefore(var, uc.getCommonParentNode(), newContext, mode); // eval before if statement
		}
		else {
			 thenStr = this.evalVarAfter(var, uc.getThenUsage(), newContext, mode);
		}
		
		IAbstractString elseStr;
		if (uc.getElseUsage() == null) {
			elseStr = this.evalVarBefore(var, uc.getCommonParentNode(), newContext, mode); // eval before if statement
		}
		else {
			elseStr = this.evalVarAfter(var, uc.getElseUsage(), newContext, mode);
		}
		
		IPosition pos = null;
		if (uc.getCommonParentNode() != null) {
			pos = ASTUtil.getPosition(uc.getCommonParentNode());
		}
		StringChoice result = new StringChoice(pos, thenStr, elseStr); 
		
		if (optimizeChoice) {
			return StringConverter.optimizeChoice(result);
		} else {
			return result;
		}
	}
	

	private IAbstractString evalInfix(InfixExpression expr, IntegerList context, ParamEvalMode mode) {
		if (expr.getOperator() == InfixExpression.Operator.PLUS) {
			List<IAbstractString> ops = new ArrayList<IAbstractString>();
			ops.add(eval(expr.getLeftOperand(), contextOf(expr, context), mode));
			ops.add(eval(expr.getRightOperand(), contextOf(expr, context), mode));
			for (Object operand: expr.extendedOperands()) {
				ops.add(eval((Expression)operand, contextOf(expr, context), mode));
			}
			return new StringSequence(ASTUtil.getPosition(expr), ops);
		}
		else {
			throw new UnsupportedStringOpExAtNode("getValOf( infix op = " + expr.getOperator() + ")", expr);
		}
	}
	
	private IAbstractString evalClassInstanceCreation(ClassInstanceCreation node, IntegerList context, ParamEvalMode mode) {
		if (!ASTUtil.isStringOrStringBuilderOrBuffer(node.resolveTypeBinding())) {
			throw new UnsupportedStringOpExAtNode("Unsupported type in class instance creation: "
					+ node.resolveTypeBinding().getQualifiedName(), node);
		}
		if (node.arguments().size() == 1) {
			Expression arg = (Expression)node.arguments().get(0);
			// string initializer
			if (arg.resolveTypeBinding().getName().equals("String")) {
				return eval(arg, context, mode);
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

	private IAbstractString evalVarAfterAssignment(IVariableBinding var, NameAssignment usage, IntegerList context, ParamEvalMode mode) {
		
		IntegerList assContext = contextOf(usage, context);
		
		if (usage.getOperator() == Assignment.Operator.ASSIGN) {
			return eval(usage.getRightHandSide(), assContext, mode);
		}
		else if (usage.getOperator() == Assignment.Operator.PLUS_ASSIGN) {
			return new StringSequence(
					ASTUtil.getPosition(usage.getAssignmentOrDeclaration()),
					eval(usage.getLeftHandSide(), assContext, mode),
					eval(usage.getRightHandSide(), assContext, mode));
		}
		else {
			throw new UnsupportedStringOpExAtNode("Unknown assignment operator: " + usage.getOperator(), usage.getAssignmentOrDeclaration());
		}
	}
	
	/*
	 * Meant for analyzing mutating method calls on name
	 */
	private IAbstractString evalVarAfterCallingItsMethod(IVariableBinding var, 
			NameInMethodCallExpression usage, IntegerList context, ParamEvalMode mode) {
		IntegerList newContext = contextOf(usage, context);
		
		if (ASTUtil.isString(var.getType())) {
			// this usage doesn't affect it
			return evalVarBefore(var, usage.getInvocation(), newContext, mode);
		}
		
		else {
			assert ASTUtil.isStringBuilderOrBuffer(var.getType());
			MethodInvocation inv = usage.getInvocation();
			String methodName = inv.getName().getIdentifier(); 
			
			if (methodName.equals("append")) {
				return new StringSequence(
						ASTUtil.getPosition(inv),
						eval(inv.getExpression(), newContext, mode),
						eval((Expression)inv.arguments().get(0), newContext, mode));
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
				return eval(inv.getExpression(), newContext, mode);
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
			return new StringHotspotDescriptor(pos, str);
		}
		catch (UnsupportedStringOpEx e) {
			return new UnsupportedHotspotDescriptor(pos, e.getMessage(), e.getPosition());
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
			options.add(eval(ret.getExpression(), null, ParamEvalMode.AS_PARAM));
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
		
		IVariableBinding var = (IVariableBinding)paramName.resolveBinding();
		NameUsage lastMod = VariableTracker.getLastModIn(var, decl);

		return evalVarAfter(var, lastMod, null, ParamEvalMode.AS_PARAM);
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
			
			if (supportRepetition) {
				IAbstractString repStr = RecursionConverter.recursionToRepetition(str);
				// TODO optimize string
				assert ! repStr.containsRecursion();
				return repStr;
				
			}
			else {
				throw new UnsupportedStringOpEx("Unsupported modification scheme in loop", str.getPosition());
			}
		}
		else {
			return str;
		}
	}
	
	private IntegerList contextOf(IPosition pos, IntegerList prevContext) {
		return new IntegerList(pos.hashCode(), prevContext);
	}
	
	private IntegerList contextOf(NameUsage usage, IntegerList prevContext) {
		return new IntegerList(usage.hashCode(), prevContext);
	}
	
	private IntegerList contextOf(ASTNode node, IntegerList prevContext) {
		return contextOf(ASTUtil.getPosition(node), prevContext);
	}
	
}


