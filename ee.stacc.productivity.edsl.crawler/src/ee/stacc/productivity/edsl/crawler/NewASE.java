package ee.stacc.productivity.edsl.crawler;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import ee.stacc.productivity.edsl.cache.CacheService;
import ee.stacc.productivity.edsl.cache.UnsupportedStringOpEx;
import ee.stacc.productivity.edsl.checkers.INodeDescriptor;
import ee.stacc.productivity.edsl.checkers.IStringNodeDescriptor;
import ee.stacc.productivity.edsl.common.logging.ILog;
import ee.stacc.productivity.edsl.common.logging.Logs;
import ee.stacc.productivity.edsl.string.AbstractStringCollection;
import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.IPosition;
import ee.stacc.productivity.edsl.string.StringCharacterSet;
import ee.stacc.productivity.edsl.string.StringChoice;
import ee.stacc.productivity.edsl.string.StringConstant;
import ee.stacc.productivity.edsl.string.StringParameter;
import ee.stacc.productivity.edsl.string.StringRandomInteger;
import ee.stacc.productivity.edsl.string.StringRepetition;
import ee.stacc.productivity.edsl.string.StringSequence;
import ee.stacc.productivity.edsl.string.util.ArgumentApplier;
import ee.stacc.productivity.edsl.tracker.NameAssignment;
import ee.stacc.productivity.edsl.tracker.NameInArgument;
import ee.stacc.productivity.edsl.tracker.NameInParameter;
import ee.stacc.productivity.edsl.tracker.NameInMethodCallExpression;
import ee.stacc.productivity.edsl.tracker.NameUsage;
import ee.stacc.productivity.edsl.tracker.NameUsageChoice;
import ee.stacc.productivity.edsl.tracker.NameUsageLoopChoice;
import ee.stacc.productivity.edsl.tracker.VariableTracker;


// TODO test Expression.resolveConstantExpressionValue

public class NewASE {
	private int maxLevel = 3;
	private boolean supportParameters = true;
	private boolean supportInvocations = true;
	
	private int level;
	private IJavaElement[] scope;
	private boolean templateConstructionMode;
	
	private static final String RESULT_FOR_SQL_CHECKER = "@ResultForSQLChecker";
	private static final ILog LOG = Logs.getLog(NewASE.class);

	
	private NewASE(int level, IJavaElement[] scope, boolean templateConstructionMode) {
		
		if (level > maxLevel) {
			throw new UnsupportedStringOpEx("Analysis level (" + level + ") too deep");
		}
		
		this.level = level;
		this.scope = scope;
		this.templateConstructionMode = templateConstructionMode;
	}
	
	public static IAbstractString evaluateExpression(Expression node) {
		NewASE evaluator = 
			new NewASE(0, new IJavaElement[] {ASTUtil.getNodeProject(node)}, false);
		return evaluator.eval(node);
	}
	
	public static List<INodeDescriptor> evaluateMethodArgumentAtCallSites
	(Collection<NodeRequest> requests,
			IJavaElement[] scope, int level) {
		String levelPrefix = "";
		for (int i = 0; i < level; i++) {
			levelPrefix += "    ";
		}

		LOG.message(levelPrefix + "###########################################");
		LOG.message(levelPrefix + "searching: ");
		for (NodeRequest nodeRequest : requests) {
			LOG.message(nodeRequest);
		}
/*
		System.out.println(levelPrefix + "###########################################");
		System.out.println(levelPrefix + "searching: ");
		for (NodeRequest nodeRequest : requests) {
			System.out.println(levelPrefix + "NR: " + nodeRequest);
		}
*/
		
		// find value from all call-sites
		Collection<IPosition> argumentPositions = NodeSearchEngine.findArgumentNodes
		(scope, requests);

		List<INodeDescriptor> result = new ArrayList<INodeDescriptor>();
		for (IPosition sr: argumentPositions) {

			try {
				IAbstractString abstractString = CacheService.getCacheService().getAbstractString(sr);

				if (abstractString == null) {
					System.out.println(levelPrefix + "EVALUATING file: " 
							+ sr.getPath() + ", line: "
							+ PositionUtil.getLineNumber(sr));
					
					Expression arg = (Expression) NodeSearchEngine.getASTNode(sr);
					NewASE evaluator = new NewASE(level, scope, false);
					
					abstractString = evaluator.eval(arg);
				}
				result.add(new StringNodeDescriptor(sr, abstractString));

			} catch (UnsupportedStringOpEx e) {
				/*
				LOG.message(levelPrefix + "UNSUPPORTED: " + e.getMessage());
				LOG.message(levelPrefix + "    file: " + sr.getPath() + ", line: "
				+ sr.getLineNumber()
				);
				*/ 
				System.out.println(levelPrefix + "UNSUPPORTED: " + e.getMessage());
				System.out.println(levelPrefix + "    file: " + sr.getPath() + ", line: "
						+ PositionUtil.getLineNumber(sr));
				result.add(new UnsupportedNodeDescriptor(sr, 
						"Unsupported SQL construction: " + e.getMessage() + " at " + PositionUtil.getLineString(sr)));
			} /* catch (Throwable e) {
				System.err.println(levelPrefix + "ERROR: " + e.getMessage());
				System.err.println(levelPrefix + "    file: " + sr.getPath() + ", line: "
						+ PositionUtil.getLineNumber(sr));
				result.add(new UnsupportedNodeDescriptor(sr, 
						"ERROR when analyzing SQL construction: " + e.getMessage() + " at " + PositionUtil.getLineString(sr)));
			} */

		}
		return result;
	}


	private IAbstractString eval(Expression node) {
		IAbstractString result = CacheService.getCacheService().getAbstractString(PositionUtil.getPosition(node));
		if (result == null) {
			try {
				result = doEval(node);
				assert result.getPosition() != null;
				CacheService.getCacheService().addAbstractString(PositionUtil.getPosition(node), result);
			} catch (UnsupportedStringOpEx e) {
				CacheService.getCacheService().addUnsupported(PositionUtil.getPosition(node), e.getMessage());
				throw e;
			}
		}
		return result;
	}
	
	
	
	private IAbstractString doEval(Expression node) {
		
		ITypeBinding type = node.resolveTypeBinding();
		assert type != null;
		
		if (type.getName().equals("int")) {
			return new StringRandomInteger(PositionUtil.getPosition(node));
		}
		else if (node instanceof StringLiteral) {
			StringLiteral stringLiteral = (StringLiteral)node;
			return new StringConstant(PositionUtil.getPosition(node), 
					stringLiteral.getLiteralValue(), stringLiteral.getEscapedValue());
		}
		else if (node instanceof CharacterLiteral) {
			CharacterLiteral characterLiteral = (CharacterLiteral)node;
			return new StringConstant(PositionUtil.getPosition(node), 
					String.valueOf(characterLiteral.charValue()), characterLiteral.getEscapedValue());
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
	
	private IAbstractString evalName(Name name) {
		IVariableBinding var = (IVariableBinding)name.resolveBinding();
		
		if (var.isField()) {
			return evalField(var);
		}
		else {
			return evalNameBefore(name, name);
		}
	}

	private IAbstractString evalField(IVariableBinding var) {
		assert var.isField();
		
		if ((var.getModifiers() & Modifier.FINAL) == 0) {
			throw new UnsupportedStringOpEx("Non-final fields are not supported");
		}
		VariableDeclarationFragment frag = NodeSearchEngine.findFieldDeclarationFragment(
					scope, 
					var.getDeclaringClass().getErasure().getQualifiedName() 
					+ "." + var.getName());

		return eval(frag.getInitializer());
	}

	private IAbstractString evalNameBefore(Name name, ASTNode target) {
		NameUsage usage = VariableTracker.getLastReachingMod
			((IVariableBinding)name.resolveBinding(), target);
		return evalNameAfterUsage(name, usage); 
	}

	private IAbstractString evalInvocationResult(MethodInvocation inv) {
		if (inv.getExpression() != null
				&& isStringBuilderOrBuffer(inv.getExpression().resolveTypeBinding())) {
			if (inv.getName().getIdentifier().equals("toString")) {
				return eval(inv.getExpression());
			}
			else if (inv.getName().getIdentifier().equals("append")) {
				return new StringSequence(
						PositionUtil.getPosition(inv), 
						eval(inv.getExpression()),
						eval((Expression)inv.arguments().get(0)));
			}
			else {
				throw new UnsupportedStringOpEx("StringBuilder/Buffer, method=" 
						+ inv.getName().getIdentifier(),
						PositionUtil.getPosition(inv)); 
			}
		}
		else  {
			return evalInvocationResultOrArgOut(inv, -1);
		}			
	}
	
	private IAbstractString evalInvocationArgOut(MethodInvocation inv,
			int argumentIndex) {
		return evalInvocationResultOrArgOut(inv, argumentIndex); 
	}

	/**
	 * 
	 * @param inv 
	 * @param argumentIndex -1 means return value
	 * @return
	 */
	private IAbstractString evalInvocationResultOrArgOut(MethodInvocation inv,
			int argumentIndex) {
		if (! supportInvocations) {
			throw new UnsupportedStringOpEx("Method call");
		}

		List<MethodDeclaration> decls = 
			NodeSearchEngine.findMethodDeclarations(scope, inv);
		
		if (decls.size() == 0) {
			throw new UnsupportedStringOpEx("No declarations found for: " + inv.toString());
		}
		
		NewASE argEvaluator = new NewASE(this.level+1, this.scope, this.templateConstructionMode);
		
		// evaluate argumets
		List<IAbstractString> arguments = new ArrayList<IAbstractString>();
		for (Object item : inv.arguments()) {
			Expression arg = (Expression)item;
			ITypeBinding typ = arg.resolveTypeBinding();
			if (isString(typ) || isStringBuilderOrBuffer(typ)) {
				arguments.add(argEvaluator.eval(arg));
			}
			else {
				arguments.add(null);
			}
		}
		
		// evaluate method bodies and apply arguments
		List<IAbstractString> choices = new ArrayList<IAbstractString>();
		for (MethodDeclaration decl: decls) {
			IAbstractString methodString;
			if (argumentIndex == -1) {
				methodString = getMethodReturnTemplate(decl);
			}
			else {
				methodString = getMethodArgOutTemplate(decl, argumentIndex);
			}
			System.out.println("METHOD STRING for " + decl.getName().getFullyQualifiedName() + ": " + methodString);
			choices.add(ArgumentApplier.applyArguments(methodString, arguments));
		}
		
		// return single result or list
		if (choices.size() == 1) {
			return choices.get(0);
		}
		else {
			return new StringChoice(PositionUtil.getPosition(inv), choices);
		}
	}

	IAbstractString getMethodReturnTemplate(MethodDeclaration decl) {
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
		NewASE evaluator = new NewASE(level+1, scope, true);
		
		List<IAbstractString> options = new ArrayList<IAbstractString>();
		for (ReturnStatement ret: returnStmts) {
			options.add(evaluator.eval(ret.getExpression()));
		}
		return new StringChoice(PositionUtil.getPosition(decl), options);
	}

	private IAbstractString getMethodArgOutTemplate(MethodDeclaration decl,
			int argumentIndex) {
		// TODO: at first look for javadoc annotation for this arg
		Name paramName = 
			((SingleVariableDeclaration)decl.parameters().get(argumentIndex)).getName();
		
		NameUsage lastMod = VariableTracker.getLastModIn(
				(IVariableBinding)paramName.resolveBinding(), decl);

		// need new evaluator because mode changes to template construction
		NewASE evaluator = new NewASE(level+1, scope, true);
		return evaluator.evalNameAfterUsage(paramName, lastMod);
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
				throw new UnsupportedStringOpEx("Problem reading " + RESULT_FOR_SQL_CHECKER);
			} else {
				//return new StringConstant(tagText);
				return new StringConstant(PositionUtil.getPosition(tag), 
						tagText, '"'+tagText+'"');
			}
		}
		else {
			return null;
		}
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
				return new StringConstant(PositionUtil.getPosition(node), "", "\"\"");
			}
			else { // CharSequence
				throw new UnsupportedStringOpEx("Unknown StringBuilder/Buffer constructor: " 
						+ arg.resolveTypeBinding().getName());
			}
		}
		else {
			assert node.arguments().size() == 0;
			return new StringConstant(PositionUtil.getPosition(node), "", "\"\"");
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
	
	private static boolean isString(ITypeBinding typeBinding) {
		return typeBinding.getQualifiedName().equals("java.lang.String");
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
	
	private IAbstractString evalNameAfterUsage(Name name, NameUsage usage) {
		assert usage != null;
		
		
		// if usage in loop and name outside the loop then
		// wrap result into loop-wrapper
		// alternatively, resolve recursion
		
		if (ASTUtil.inALoopSeparatingFrom(usage.getNode(), name)) {
			throw new UnsupportedStringOpEx("modifications in loop not supported");
//			return new NamedString(usage.getASTNode(), evalNameAfterUsageWithoutLoopCheck(name, usage));
		}
		else {
			return evalNameAfterUsageWithoutLoopCheck(name, usage);
		}
	}
	
	
	private IAbstractString evalNameAfterUsageWithoutLoopCheck(Name name, NameUsage usage) {
		// check, if it's necessary to start new evaluator (entering the loop from below)
		/* TODO
		if (usage.getMainStatement() != this.mainBlock
				// and if usage.mainStmt inside this.mainBlock and it's a loop then 
				) {
			NewASE newEval = new NewASE(level, invocationContext, scope);
			newEval.mainBlock = usage.getMainStatement();
			newEval.startingPlace = usage;
			return widenToRegular(newEval.evalNameAfterUsage(usage));
		}
		*/
		
		
		// can use this evaluator
		if (usage instanceof NameUsageChoice) {
			NameUsageChoice uc = (NameUsageChoice)usage;
			return new StringChoice(
					PositionUtil.getPosition(uc.getNode()),
					this.evalNameAfterUsage(name, uc.getThenUsage()),
					this.evalNameAfterUsage(name, uc.getElseUsage())); 
		}
		else if (usage instanceof NameUsageLoopChoice) {
			return evalNameInLoopChoice(name, (NameUsageLoopChoice)usage);
		}
		else if (usage instanceof NameAssignment) {
			return evalNameAssignment(name, (NameAssignment)usage);
		}
		else if (usage instanceof NameInParameter) {
			return evalNameInParameter((NameInParameter)usage);
		}
		else if (usage instanceof NameInArgument) {
			return evalNameInArgument(name, (NameInArgument)usage);
		}
		else if (usage instanceof NameInMethodCallExpression) {
			return evalNameInCallExpression(name, (NameInMethodCallExpression)usage);
		}
		else {
			throw new UnsupportedStringOpEx("Unsupported NameUsage: " + usage.getClass());
		}
	}

	private IAbstractString evalNameInCallExpression(Name name, 
			NameInMethodCallExpression usage) {
		if (isString(name.resolveTypeBinding())) {
			// this usage doesn't affect it
			return evalNameBefore(name, usage.getNode());
		}
		
		else {
			assert isStringBuilderOrBuffer(name.resolveTypeBinding());
			MethodInvocation inv = usage.getInv();
			if (inv.getName().getIdentifier().equals("append")) {
				return new StringSequence(
						PositionUtil.getPosition(inv),
						eval(inv.getExpression()),
						eval((Expression)inv.arguments().get(0)));
			}
			else if (inv.getName().getIdentifier().equals("toString")) {
				return eval(inv.getExpression());
			}
			else {
				throw new UnsupportedStringOpEx("Unknown method called on StringBuilder: " 
						+ inv.getName());
			}
		}
	}

	private IAbstractString evalNameAssignment(Name name, NameAssignment usage) {
		if (usage.getOperator() == Assignment.Operator.ASSIGN) {
			return eval(usage.getRightHandSide());
		}
		else if (usage.getOperator() == Assignment.Operator.PLUS_ASSIGN) {
			return new StringSequence(
					PositionUtil.getPosition(usage.getNode()),
					eval(usage.getName()),
					eval(usage.getRightHandSide()));
		}
		else {
			throw new UnsupportedStringOpEx("Unknown assignment operator: " + usage.getOperator());
		}
	}

	private IAbstractString evalNameInArgument(Name name, NameInArgument usage) {
		if (isString(name.resolveTypeBinding())) {
			// this usage doesn't affect it, keep looking
			return evalNameBefore(name, usage.getNode());
		}
		else {
			assert isStringBuilderOrBuffer(name.resolveTypeBinding());
			return evalInvocationArgOut(usage.getInv(), usage.getIndex()); 
		}
	}

	private IAbstractString evalNameInLoopChoice(Name name, NameUsageLoopChoice usage) {
		assert usage.getBaseUsage() != null;
		assert usage.getLoopUsage() != null;
		
		IAbstractString baseString = evalNameAfterUsage(name, usage.getBaseUsage());
		return new RecursiveStringChoice(
				PositionUtil.getPosition(usage.getNode()),
				baseString, 
				usage.getLoopUsage().getNode()); // null means outermost string TODO too ugly
		/*
		if (loopChoice.getLoopUsage() == this.startingPlace) {
			IAbstractString baseString = evalNameAfterUsage(loopChoice.getBaseUsage());
			return new RecursiveStringChoice(baseString, null); // null means outermost string TODO too ugly
		}
		else {
			throw new UnsupportedStringOpEx("NameUsageLoopChoice weird case");
		}
		*/
	}

	private IAbstractString evalNameInParameter(NameInParameter usage) {
		if (this.templateConstructionMode) {
			return new StringParameter(
					PositionUtil.getPosition(usage.getNode()),
					usage.getIndex());
		}
		else {
			MethodDeclaration method = usage.getMethodDecl();
			List<INodeDescriptor> descList = evaluateMethodArgumentAtCallSites(
					Collections.singleton(
							new NodeRequest(
									ASTUtil.getMethodClassName(method), 
									method.getName().toString(),
									usage.getIndex()+1)), 
					scope, // FIXME should be widened to ... ? 
					level+1);
			
			List<IAbstractString> choices = new ArrayList<IAbstractString>();
			
			for (INodeDescriptor choiceDesc: descList) {
				if (choiceDesc instanceof IStringNodeDescriptor) {
					choices.add(((IStringNodeDescriptor)choiceDesc).getAbstractValue());
				}
				else {
					// FIXME what about the rest ???
				}
			}
			if (choices.size() == 0) {
				throw new UnsupportedStringOpEx("Possible problem, no callsites found for: "
						+ method.getName());
			}
			return new StringChoice(
					PositionUtil.getPosition(
					(ASTNode)method.parameters().get(usage.getIndex())),
					choices);
		}
	}

	

}
