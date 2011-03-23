package com.zeroturnaround.alvor.crawler;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
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
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.zeroturnaround.alvor.cache.CacheService;
import com.zeroturnaround.alvor.common.EmptyStringConstant;
import com.zeroturnaround.alvor.common.HotspotPattern;
import com.zeroturnaround.alvor.common.HotspotDescriptor;
import com.zeroturnaround.alvor.common.RecursionConverter;
import com.zeroturnaround.alvor.common.StringConverter;
import com.zeroturnaround.alvor.common.StringNodeDescriptor;
import com.zeroturnaround.alvor.common.UnsupportedNodeDescriptor;
import com.zeroturnaround.alvor.common.UnsupportedStringOpEx;
import com.zeroturnaround.alvor.common.logging.ILog;
import com.zeroturnaround.alvor.common.logging.Logs;
import com.zeroturnaround.alvor.common.logging.Measurements;
import com.zeroturnaround.alvor.common.logging.Timer;
import com.zeroturnaround.alvor.configuration.ProjectConfiguration;
import com.zeroturnaround.alvor.crawler.util.ASTUtil;
import com.zeroturnaround.alvor.crawler.util.JavaModelUtil;
import com.zeroturnaround.alvor.crawler.util.UnsupportedStringOpExAtNode;
import com.zeroturnaround.alvor.string.IAbstractString;
import com.zeroturnaround.alvor.string.IPosition;
import com.zeroturnaround.alvor.string.StringChoice;
import com.zeroturnaround.alvor.string.StringConstant;
import com.zeroturnaround.alvor.string.StringParameter;
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



// TODO test Expression.resolveConstantExpressionValue

/**
 * evaluateExpression is static counterpart for eval
 * eval is doEval plus cache handling
 */
public class AbstractStringEvaluator {
	private static int maxLevel = 4;
	private static boolean supportInvocations = true;
	private static boolean optimizeChoice = true;
	
	private int ipLevel; // level of interprocedural calls
	private IJavaElement[] scope;
	private boolean templateConstructionMode; // means: don't evaluate parameters, leave them as StringParameter-s 
	
	private static final String RESULT_FOR_SQL_CHECKER = "@ResultForSQLChecker";
	private static final ILog LOG = Logs.getLog(AbstractStringEvaluator.class);
	
	private IProgressMonitor monitor = null;

	/*
	 * Actually returns abstract strings corresponding to hotspots
	 * (or markers for unsupported cases)  
	 * TODO rename?
	 */
	public static List<HotspotDescriptor> findAndEvaluateHotspots(IJavaElement[] scope, ProjectConfiguration conf,
			IProgressMonitor monitor) {
		Measurements.resetAll();
		
		Timer timer = new Timer();
		timer.start("TIMER: string construction");
		List<HotspotPattern> requests = conf.getHotspotPatterns();
		if (requests.isEmpty()) {
			throw new IllegalArgumentException("No hotspot definitions found in options");
		}
		List<HotspotDescriptor> result = AbstractStringEvaluator.evaluateMethodArgumentAtCallSites(requests, scope, 0, null, monitor);
		timer.printTime(); // String construction
		
		LOG.message(Measurements.parseTimer);
		LOG.message(Measurements.methodDeclSearchTimer);
		LOG.message(Measurements.argumentSearchTimer);
		
		return result;
	}
	
	private AbstractStringEvaluator(int level, IJavaElement[] scope, boolean templateConstructionMode, IProgressMonitor monitor) {
		if (level > maxLevel) {
			throw new UnsupportedStringOpEx("Analysis level (" + level + ") too deep", (IPosition)null);
		}
		this.ipLevel = level;
		this.scope = scope;
		this.templateConstructionMode = templateConstructionMode;
		this.monitor = monitor;
	}
	
	/*
	 * Used for debugging
	 */
	public static IAbstractString evaluateExpression(Expression node) {
		AbstractStringEvaluator evaluator = new AbstractStringEvaluator(0, 
				new IJavaElement[] {ASTUtil.getNodeProject(node)}, false, null);
		return evaluator.eval(node, null);
	}
	
	public static List<HotspotDescriptor> evaluateMethodArgumentAtCallSites
		(Collection<HotspotPattern> requests, IJavaElement[] scope, int level, NodePositionList context,
				IProgressMonitor monitor) {
		logMessage("SEARCHING", level, null);
		for (HotspotPattern hotspotPattern : requests) {
			logMessage(hotspotPattern, level, null);
		}
		
		Collection<IPosition> argumentPositions = NodeSearchEngine.findArgumentNodes(scope, requests, monitor);
		AbstractStringEvaluator evaluator = new AbstractStringEvaluator(level, scope, false, monitor);
		List<HotspotDescriptor> result = new ArrayList<HotspotDescriptor>();
		for (IPosition pos: argumentPositions) {
			try {
				result.add(new StringNodeDescriptor(pos, evaluator.eval(pos, context)));
				evaluator.checkMonitor(true);
			} 
			catch (UnsupportedStringOpEx e) {
				result.add(new UnsupportedNodeDescriptor(pos, e.getMessage(), e.getPosition()));
			} 
		}
		return result;
	}
	
	private IAbstractString eval(IPosition pos, NodePositionList context) {
		
		IAbstractString abstractString = null;
		if (shouldUseCache()) {
			abstractString = CacheService.getCacheService().getAbstractString(pos);
		}

		if (abstractString == null) {
			Expression arg = (Expression) NodeSearchEngine.getASTNode(pos);
			abstractString = eval(arg, context);
		}
		
		return abstractString;
	}
	private IAbstractString eval(Expression node, NodePositionList context) {
		IAbstractString result = null;
		if (shouldUseCache()) {
			// may throw UnsupportedStringOpEx instead returning
			result = CacheService.getCacheService().getAbstractString(ASTUtil.getPosition(node));
		}
		if (result == null) {
			try {
				logMessage("EVALUATING", ipLevel, node);
				result = RecursionConverter.checkRecursionToRepetition(doEval(node, context));
				if (result.getPosition() == null) {
					LOG.exception(new IllegalArgumentException("Got null position"));
				}
				assert result.getPosition() != null;
				if (shouldUseCache() && !StringConverter.includesStringExtensions(result)) {
					CacheService.getCacheService().addAbstractString(ASTUtil.getPosition(node), result);
				}
			} 
			catch (UnsupportedStringOpEx e) {
				logMessage("UNSUPPORTED: " + e.getMessage(), ipLevel, node);
				if (shouldUseCache()) {
					CacheService.getCacheService().addUnsupported(ASTUtil.getPosition(node), e.getMessage());
				}
				throw e;
			}
//			catch (Throwable e) {
//				logMessage("ERROR: " + e.getMessage(), node);
//				throw new UnsupportedStringOpEx("ERROR: " + e.getMessage());
//			}
		}
		return result;
	}
	
	private boolean shouldUseCache() {
		return !this.templateConstructionMode;
	}
	
	
	private IAbstractString doEval(Expression node, NodePositionList context) {
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
	
	private IAbstractString evalName(Name name, NodePositionList context) {
		IVariableBinding var = (IVariableBinding)name.resolveBinding();
		
		if (var.isField()) {
			return evalField(var, context);
		}
		else {
			return evalNameBefore(name, name, context);
		}
	}

	private IAbstractString evalField(IVariableBinding var, NodePositionList context) {
		assert var.isField();
		
		if ((var.getModifiers() & Modifier.FINAL) == 0) {
			// TODO should support non-final String-s
			
			throw new UnsupportedStringOpEx("Non-final fields are not supported: "
					+ var.getDeclaringClass().getName() + '.' + var.getName(), (IPosition)null);
		}
		VariableDeclarationFragment frag = NodeSearchEngine.findFieldDeclarationFragment(
					scope, 
					var.getDeclaringClass().getErasure().getQualifiedName() 
					+ "." + var.getName());
		
		if (frag.getInitializer() == null) {
			// TODO should check for initializer in constructor
			throw new UnsupportedStringOpEx("Final fields without initializer are not supported"
					+ var.getDeclaringClass().getName() + '.' + var.getName(), (IPosition)null);
		}
		return eval(frag.getInitializer(), context);
	}

	private IAbstractString evalNameBefore(Name name, ASTNode target, NodePositionList context) {
		NameUsage usage = VariableTracker.getLastReachingMod
			((IVariableBinding)name.resolveBinding(), target);
		return evalNameAfter(name, usage, new NodePositionList(name, context)); 
	}

	private IAbstractString evalInvocationResult(MethodInvocation inv, NodePositionList context) {
//		if (inv.toString().contains("Integer.toString")) {
//			ITypeBinding typ = inv.getExpression().resolveTypeBinding();
//			LOG.error(typ.getQualifiedName());
//		}

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
		if (inv.getExpression() != null
				&& ASTUtil.isIntegral(inv.getExpression().resolveTypeBinding())
				&& inv.getName().getIdentifier().equals("toString")) {
			return new StringRandomInteger(ASTUtil.getPosition(inv));
		}
		else  {
			return evalInvocationResultOrArgOut(inv, -1, context);
		}			
	}
	
	private IAbstractString evalInvocationArgOut(MethodInvocation inv,
			int argumentIndex, NodePositionList context) {
		return evalInvocationResultOrArgOut(inv, argumentIndex, context); 
	}

	private IAbstractString evalInvocationResultOrArgOut(MethodInvocation inv,
			int argumentIndex, NodePositionList context) {
		if (! supportInvocations) {
			throw new UnsupportedStringOpExAtNode("Method call", inv);
		}
		
		assert LOG.message("evalInvocationResultOrArgOut: " + inv.resolveMethodBinding()
				+ ":" + argumentIndex);
		
		MethodTemplateSearcher templateSearcher = new MethodTemplateSearcher(this);
		List<IAbstractString> templates = 
			templateSearcher.findMethodTemplates(
					JavaModelUtil.scopeToProjectAndRequiredProjectsScope(scope),
					inv, argumentIndex);
		
		if (templates.size() == 0) {
			throw new UnsupportedStringOpExAtNode("No declarations found for: " + inv.toString(), inv);
		}
		
		AbstractStringEvaluator argEvaluator = new AbstractStringEvaluator(this.ipLevel+1, this.scope, 
				this.templateConstructionMode, this.monitor);
		
		// evaluate argumets
		List<IAbstractString> arguments = new ArrayList<IAbstractString>();
		for (Object item : inv.arguments()) {
			Expression arg = (Expression)item;
			ITypeBinding typ = arg.resolveTypeBinding();
			if (ASTUtil.isStringOrStringBuilderOrBuffer(typ)) {
				arguments.add(argEvaluator.eval(arg, new NodePositionList(inv, context)));
			}
			else {
				arguments.add(null);
			}
		}
		
		// apply arguments to each template
		List<IAbstractString> choices = new ArrayList<IAbstractString>();
		for (IAbstractString template: templates) {
			assert LOG.message("METHOD STRING for " + inv.getName().getFullyQualifiedName() + ": " + template);
			choices.add(ArgumentApplier.applyArgumentsList(template, arguments));
		}
		
		if (choices.size() == 1) {
			return choices.get(0);
		}
		else {
			return new StringChoice(ASTUtil.getPosition(inv), choices);
		}
	}

	public IAbstractString getMethodTemplate(MethodDeclaration decl, int argIndex) {
		if (argIndex == -1) {
			return getMethodReturnTemplate(decl);
		}
		else {
			return getMethodArgOutTemplate(decl, argIndex);
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
		// Need new evaluator because mode changes to template construction
		AbstractStringEvaluator evaluator = new AbstractStringEvaluator(ipLevel+1, scope, 
				true, this.monitor);
		
		List<IAbstractString> options = new ArrayList<IAbstractString>();
		for (ReturnStatement ret: returnStmts) {
			options.add(evaluator.eval(ret.getExpression(), null));
		}
		if (options.size() == 1) {
			return options.get(0);
		}
		else {
			return new StringChoice(ASTUtil.getPosition(decl), options);
		}
	}
	
	private IAbstractString getMethodArgOutTemplate(MethodDeclaration decl,
			int argumentIndex) {
		// TODO: at first look for javadoc annotation for this arg
		Name paramName = 
			((SingleVariableDeclaration)decl.parameters().get(argumentIndex)).getName();
		
		NameUsage lastMod = VariableTracker.getLastModIn(
				(IVariableBinding)paramName.resolveBinding(), decl);

		// need new evaluator because mode changes to template construction
		AbstractStringEvaluator evaluator = new AbstractStringEvaluator(ipLevel+1, scope, true,
				this.monitor);
		return evaluator.evalNameAfter(paramName, lastMod, null);
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
			return evalNameAfterMethodEntryInParameter((NameInParameter)usage, context);
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

 
	private IAbstractString evalNameAfterBeingArgument(Name name, NameInArgument usage, NodePositionList context) {
		if (ASTUtil.isString(name.resolveTypeBinding())) {
			// this usage doesn't affect it, keep looking
			return evalNameBefore(name, usage.getNode(), context);
		}
		else {
			if (!ASTUtil.isStringBuilderOrBuffer(name.resolveTypeBinding())) {
				assert LOG.message(name.resolveTypeBinding());
			}
			assert ASTUtil.isStringBuilderOrBuffer(name.resolveTypeBinding());
			return evalInvocationArgOut(usage.getInv(), usage.getIndex(), context); 
		}
	}

//	private boolean isInsideBadLoop(ASTNode node) {
//		// FIXME
//		return false; 
//		/*
//		ASTNode loop = ASTUtil.getContainingLoop(node);
//		if (loop == null) {
//			return false;
//		}
//		else {
//			return ASTUtil.containsConditional(loop);
//		}
//		*/
//	}
	
//	@Deprecated
//	private IAbstractString evalNameInLoopChoice(Name name, NameUsageLoopChoice usage, ContextLink context) {
//		if (!supportLoops 
//				// TODO temporary guards
//				|| isInsideBadLoop(usage.getNode())
//				|| usage.getBaseUsage() == null
//				|| usage.getLoopUsage() == null
//				|| isInsideBadLoop(usage.getBaseUsage().getNode())
//				|| isInsideBadLoop(usage.getLoopUsage().getNode())
//				) {
//			throw new UnsupportedStringOpEx("Unsupported modification scheme in loop", usage.getNode());
//		}
//		
//		assert usage.getBaseUsage() != null;
//		assert usage.getLoopUsage() != null;
//		
//		IAbstractString baseString = evalNameAfterUsage(name, usage.getBaseUsage(), context);
//		// FIXME recursion is not necessary here, can be more indirect
//		return new RecursiveStringChoice(
//				PositionUtil.getPosition(usage.getNode()),
//				baseString, 
//				usage.getLoopUsage().getNode()); // null means outermost string. TODO too ugly
//		/*
//		if (loopChoice.getLoopUsage() == this.startingPlace) {
//			IAbstractString baseString = evalNameAfterUsage(loopChoice.getBaseUsage());
//			return new RecursiveStringChoice(baseString, null); // null means outermost string TODO too ugly
//		}
//		else {
//			throw new UnsupportedStringOpEx("NameUsageLoopChoice weird case");
//		}
//		*/
//	}

	private IAbstractString evalNameAfterMethodEntryInParameter(NameInParameter usage, NodePositionList context) {
		if (this.templateConstructionMode) {
			return new StringParameter(
					ASTUtil.getPosition(usage.getNode()),
					usage.getIndex());
		}
		else {
			MethodDeclaration method = usage.getMethodDecl();
			String methodName = method.getName().toString();
			methodName += ASTUtil.getArgumentTypesStringOld(method.resolveBinding());
			
			List<HotspotDescriptor> descList = evaluateMethodArgumentAtCallSites(
					Collections.singleton(
							(HotspotPattern)new HotspotPattern(
									ASTUtil.getMethodClassName(method), 
									methodName, "*",
									usage.getIndex()+1)), 
					scope, // FIXME should be widened to all required projects 
					ipLevel+1,
					new NodePositionList(usage.getNode(), context),
					this.monitor);
			
			List<IAbstractString> choices = new ArrayList<IAbstractString>();
			
			for (HotspotDescriptor choiceDesc: descList) {
				if (choiceDesc instanceof StringNodeDescriptor) {
					choices.add(((StringNodeDescriptor)choiceDesc).getAbstractValue());
				}
				else if (choiceDesc instanceof UnsupportedNodeDescriptor) {
					throw new UnsupportedStringOpExAtNode(((UnsupportedNodeDescriptor)choiceDesc).getProblemMessage(),
							usage.getNode());
				}
				else {
					throw new IllegalStateException();
				}
			}
			if (descList.size() == 0) {
				throw new UnsupportedStringOpExAtNode("Possible problem, no callsites found in current project for: "
						+ method.resolveBinding().getDeclaringClass().getQualifiedName() + "."
						+ method.getName() + ASTUtil.getArgumentTypesStringOld(method.resolveBinding()),
						usage.getNode());
			}
			return new StringChoice(
					ASTUtil.getPosition(
					(ASTNode)method.parameters().get(usage.getIndex())),
					choices);
		}
	}
	
	private static void logMessage(Object msg, int level, ASTNode node) {
		String finalMsg = "";
		for (int i = 0; i < level; i++) {
			finalMsg += "    ";
		}
		
		finalMsg += msg;
		if (node != null) {
			finalMsg += ", file: " + ASTUtil.getFileString(node) 
					+ ", line: " + ASTUtil.getLineNumber(node); 
		}
		assert LOG.message(finalMsg);
	}
	
	private void checkMonitor(boolean makeStep) {
		if (this.monitor != null) {
			if (this.monitor.isCanceled()) {
				throw new OperationCanceledException(); 
			}
			else {
				this.monitor.worked(1);
			}
		}
	}
}
