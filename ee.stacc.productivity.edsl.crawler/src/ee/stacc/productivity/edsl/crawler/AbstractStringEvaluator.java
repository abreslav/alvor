package ee.stacc.productivity.edsl.crawler;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import ee.stacc.productivity.edsl.checkers.INodeDescriptor;
import ee.stacc.productivity.edsl.checkers.IStringNodeDescriptor;
import ee.stacc.productivity.edsl.common.logging.ILog;
import ee.stacc.productivity.edsl.common.logging.Logs;
import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.StringChoice;
import ee.stacc.productivity.edsl.string.StringConstant;
import ee.stacc.productivity.edsl.string.StringRandomInteger;
import ee.stacc.productivity.edsl.string.StringSequence;


public class AbstractStringEvaluator {
	private static final String RESULT_FOR_SQL_CHECKER = "@ResultForSQLChecker";
	private static final String SIMPLIFIED_BODY_FOR_SC = "@SimplifiedBodyForSQLChecker";
	private static final ILog LOG = Logs.getLog(AbstractStringEvaluator.class);
	private int maxLevel = 2;
	private boolean supportParameters = true;
	private boolean supportInvocations = true;
	
	private int level;
	private MethodInvocation invocationContext;
	private IJavaElement scope;
	
	public static IAbstractString evaluateExpression(Expression node) {
		AbstractStringEvaluator evaluator = 
			new AbstractStringEvaluator(0, null, ASTUtil.getNodeProject(node));
		return evaluator.eval(node);
	}
	
	private AbstractStringEvaluator(int level, MethodInvocation invocationContext,
			IJavaElement scope) {
		
		if (level > maxLevel) {
			throw new UnsupportedStringOpEx("Analysis level (" + level + ") too deep");
		}
		
		this.level = level;
		this.invocationContext = invocationContext;
		this.scope = scope;
	}
	
	private IAbstractString eval(Expression node) {
		IAbstractString result = null;//CacheService.getCacheService().getAbstractString(PositionUtil.getPosition(node));
		if (result == null) {
			result = doEval(node);
//			CacheService.getCacheService().addAbstractString(PositionUtil.getPosition(node), result);
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
		else if (node instanceof ParenthesizedExpression) {
			return eval(((ParenthesizedExpression)node).getExpression());
		}
		else if (node instanceof InfixExpression) {
			return evalInfix((InfixExpression)node);
		}
		else if (node instanceof MethodInvocation) {
			MethodInvocation inv = (MethodInvocation)node;
				return evalInvocation(inv);
		}
		else if (node instanceof ClassInstanceCreation) {
			
			assert (isStringBuilderOrBuffer(node.resolveTypeBinding()));
			ClassInstanceCreation cic = (ClassInstanceCreation)node;
			if (cic.arguments().size() == 1) {
				Expression arg = (Expression)cic.arguments().get(0);
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
				assert cic.arguments().size() == 0;
				StringConstant stringConstant = new StringConstant(PositionUtil.getPosition(node), 
						"", "");
				return stringConstant;
			}
		}
		else {
			throw new UnsupportedStringOpEx
				("getValOf(" + node.getClass().getName() + ")");
		}
	}

	private IAbstractString evalName(Name node) {
		// can be SimpleName or QualifiedName
		Statement stmt = ASTUtil.getContainingStmt(node);
		if (stmt == null) {
			assert ((IVariableBinding)node.resolveBinding()).isField();
			return evalField(node);
		} else {
			// TODO this statement can modify this var 
			return evalVarBefore(node, stmt);
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
	
	private IAbstractString evalVarAfterIf(Name name, IfStatement stmt) {
		IAbstractString ifVal = evalVarAfter(name, stmt.getThenStatement());
		IAbstractString elseVal = null;
		
		if (stmt.getElseStatement() != null) {
			elseVal = evalVarAfter(name, stmt.getElseStatement());
		} else {
			elseVal = evalVarBefore(name, stmt);
		}
		
		if (ifVal.equals(elseVal)) {
			return ifVal;
		} else {
			return new StringChoice(PositionUtil.getPosition(stmt), ifVal, elseVal);
		}
	}
	
	private IAbstractString evalVarAfterDecl(Name name,
			VariableDeclarationStatement stmt) {
		IVariableBinding var = (IVariableBinding) name.resolveBinding();
		
		// May include declarations for several variables
		for (int i=stmt.fragments().size()-1; i>=0; i--) {
			VariableDeclaration vDec = (VariableDeclaration)stmt.fragments().get(i);
			
			if (vDec.getName().resolveBinding().isEqualTo(var)) {
				return eval(vDec.getInitializer());
			}
		}
		return evalVarBefore(name, stmt);
	}
	
	private IAbstractString evalVarAfterAss(Name name, 
			ExpressionStatement stmt) {
		assert stmt.getExpression() instanceof Assignment;

		IVariableBinding var = (IVariableBinding) name.resolveBinding();
		
		// TODO StringBuilder variable can be assigned also
		
		Assignment ass = (Assignment)stmt.getExpression();
		
		if (ass.getLeftHandSide() instanceof SimpleName
			&& ((SimpleName)ass.getLeftHandSide()).resolveBinding().isEqualTo(var)) {
			
			IAbstractString rhs = eval(ass.getRightHandSide());
			
			if (ass.getOperator() == Assignment.Operator.ASSIGN) {
				return rhs;
			}
			else if (ass.getOperator() == Assignment.Operator.PLUS_ASSIGN) {
				return new StringSequence(PositionUtil.getPosition(name), evalVarBefore(name, stmt), rhs);
			}
			else {
				throw new UnsupportedStringOpEx("getVarValAfterAss: unknown operator");
			}
		}
		else { // wrong assignment, this statement doesn't change var (hopefully :)
			return evalVarBefore(name, stmt);
		}
	}
	
	private IAbstractString evalVarAfter(Name name, Statement stmt) {
		//IVariableBinding var = (IVariableBinding) name.resolveBinding();
		//LOG.message("getVarValAfter: var=" + var.getName()
		//		+ ", stmt="+ stmt.getClass().getName());
		
		if (stmt instanceof ExpressionStatement) {
			Expression expr = ((ExpressionStatement)stmt).getExpression(); 
			if (expr instanceof Assignment) {
				return evalVarAfterAss(name, (ExpressionStatement)stmt);
			}
			else if (expr instanceof MethodInvocation) {
				return evalVarAfterMethodInvStmt(name, (ExpressionStatement)stmt);
			}
			else {
				throw new UnsupportedStringOpEx
					("getVarValAfter(_, ExpressionStatement." + expr.getClass() + ")");
			}
		}
		else if (stmt instanceof VariableDeclarationStatement) {
			return evalVarAfterDecl(name, (VariableDeclarationStatement)stmt);
		}
		else if (stmt instanceof IfStatement) {
			return evalVarAfterIf(name, (IfStatement)stmt);
		}
		else if (stmt instanceof Block) {
			return evalVarAfter(name, ASTUtil.getLastStmt((Block)stmt));
		}
		else if (stmt instanceof ReturnStatement) {
			return evalVarBefore(name, stmt);
		}
		else { // other kind of statement
			throw new UnsupportedStringOpEx("getVarValAfter(var, " + stmt.getClass().getName() + ")");
		} 
	}
	
	private IAbstractString evalField(Name node) {
		IVariableBinding var = (IVariableBinding) node.resolveBinding();
		VariableDeclarationFragment frag = NodeSearchEngine
			.findFieldDeclarationFragment(scope, var.getDeclaringClass().getErasure().getQualifiedName() 
				+ "." + var.getName());
	
		FieldDeclaration decl = (FieldDeclaration)frag.getParent();
		if ((decl.getModifiers() & Modifier.FINAL) == 0) {
			throw new UnsupportedStringOpEx("Only final fields are supported");
			// TODO create option with initalizer and AnyString
		}
		return eval(frag.getInitializer());
	}
	
	private IAbstractString evalVarAfterMethodInvStmt(Name name,
			ExpressionStatement stmt) {
		IVariableBinding var = (IVariableBinding) name.resolveBinding();
		
		if (isStringBuilderOrBuffer(var.getType())) {
			MethodInvocation inv = (MethodInvocation)stmt.getExpression();
			
			// TODO: check that it's really chain of sb.append("...").append("...")
			// and nothing else
			if (isStringBuilderOrBuffer(stmt.getExpression().resolveTypeBinding())
					&& builderChainIsStartedByVar(inv, var)) {
				return eval(inv);
			}
			else if (ASTUtil.varIsUsedIn(var, inv)) {
				throw new UnsupportedStringOpEx(
						"Var '" + var.getName() + "' (possibly) used in unsupported construct");
			}
			else { // SB is not changed in this statement
				return evalVarBefore(name, stmt);
			}
		}
		else { // variable is of type String
			// it cannot be changed here  
			return evalVarBefore(name, stmt);
		}
	}
	
	
	
	private boolean builderChainIsStartedByVar(Expression node, IVariableBinding var) {
		assert isStringBuilderOrBuffer(node.resolveTypeBinding());
		if (node instanceof SimpleName) {
			return ((SimpleName) node).resolveBinding().isEqualTo(var);
		}
		else if (node instanceof MethodInvocation) {
			return builderChainIsStartedByVar(((MethodInvocation)node).getExpression(), var);
		}
		else {
			throw new UnsupportedStringOpEx("unknown construction in builderChain: " 
					+ node.getClass());
		}
	}
	
	private IAbstractString evalInvocation(MethodInvocation inv) {
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
			if (! supportInvocations) {
				throw new UnsupportedStringOpEx("Method call");
			}
			AbstractStringEvaluator evaluatorWithNewContext = 
				new AbstractStringEvaluator(level+1, inv, scope);

			List<MethodDeclaration> decls = NodeSearchEngine.findMethodDeclarations(scope, inv);
			
			if (decls.size() == 1) {
				return evaluatorWithNewContext.getMethodReturnValue
					(simplifyMethodDeclaration(decls.get(0)));
			}
			else if (decls.size() == 0) {
				throw new UnsupportedStringOpEx("Possible problem, no declarations found for: " + inv.toString());
			}
			else {
				List<IAbstractString> choices = new ArrayList<IAbstractString>();
				for (MethodDeclaration decl: decls) {
					choices.add(evaluatorWithNewContext.getMethodReturnValue
							(simplifyMethodDeclaration(decl)));
				}
				return new StringChoice(PositionUtil.getPosition(inv), choices);
			}
		}			
	}
	
	/**
	 * If decl has special annotations then return patched and reparsed version
	 * of the method
	 * 
	 * @param decl
	 * @return
	 */
	private MethodDeclaration simplifyMethodDeclaration(MethodDeclaration decl) {
		TagElement tag = ASTUtil.getJavadocTag(decl.getJavadoc(), SIMPLIFIED_BODY_FOR_SC);
		if (tag == null) {
			return decl; // can't simplify
		}
		String tagText = ASTUtil.getTagElementText(tag);
		if (tagText == null) {
			throw new UnsupportedStringOpEx("Problem reading "+ SIMPLIFIED_BODY_FOR_SC);
		}
		
		// replace method body with given string and reparse
		try {
			Block newBody = (Block)ASTTransformer.patchAndReParse(decl.getBody(), tagText);
			MethodDeclaration newDecl = (MethodDeclaration)newBody.getParent();
		
			// remove annotation from new version to avoid recursion, TODO test
			newDecl.getJavadoc().delete();
			return newDecl;
		} catch (ParseException e) {
			throw new UnsupportedStringOpEx("Reparsing declaration. ParseException: "
					+ e.toString());
		} catch (JavaModelException e) {
			throw new UnsupportedStringOpEx("Reparsing declaration. JavaModelException: "
					+ e.toString());
		}
	}
	
	private IAbstractString getMethodReturnValue(MethodDeclaration decl) {
		// if it has @ResultForSQLChecker in JAVADOC then return this
		IAbstractString javadocResult = getMethodReturnValueFromJavadoc(decl);
		if (javadocResult != null) {
			return javadocResult;
		}
		// TODO: if ResultForSQLChecker is specified in the configuration file ...
		
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
			options.add(eval(ret.getExpression()));
		}
		return new StringChoice(PositionUtil.getPosition(decl), options);
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
				return new StringConstant(tagText);
			}
		}
		else {
			return null;
		}
	}
	
	public static List<INodeDescriptor> evaluateMethodArgumentAtCallSites
			(Collection<NodeRequest> requests,
					IJavaElement scope, int level) {
		String levelPrefix = "";
		for (int i = 0; i < level; i++) {
			levelPrefix += "    ";
		}

		
		LOG.message(levelPrefix + "###########################################");
		LOG.message(levelPrefix + "searching: ");
		for (NodeRequest nodeRequest : requests) {
			LOG.message(nodeRequest);
		}
		
		// find value from all call-sites
		List<NodeDescriptor> argumentNodes = NodeSearchEngine.findArgumentNodes
			(scope, requests);
		
		List<INodeDescriptor> result = new ArrayList<INodeDescriptor>();
		for (INodeDescriptor sr: argumentNodes) {

			Expression arg = (Expression)sr.getNode();
			StringNodeDescriptor desc = new StringNodeDescriptor(arg, sr.getLineNumber(), null);
			try {
				AbstractStringEvaluator evaluator = 
					new AbstractStringEvaluator(level, null, scope);
				
				//LOG.message(levelPrefix + "EVALUATING: file=" + desc.getFile()
				//		+ ", line=" + desc.getLineNumber());
				desc.setAbstractValue(evaluator.eval(arg));
				result.add(desc);
			} catch (UnsupportedStringOpEx e) {
				LOG.message(levelPrefix + "UNSUPPORTED: " + e.getMessage());
				LOG.message(levelPrefix + "    file: " + sr.getPosition().getPath() + ", line: " 
						+ sr.getLineNumber());
				result.add(new UnsupportedNodeDescriptor(arg, sr.getLineNumber(), 
						"Unsupported SQL construction: " + e.getMessage()));
			}
			/* catch (Exception e) {
				if (catchAllExceptions) {
					LOG.message(levelPrefix + "PROGRAM ERROR: " + e.getMessage());
					LOG.message(levelPrefix + "    file: " + sr.getFile() + ", line: " 
						+ sr.getLineNumber());	
				} else {
					throw e;
				}
			} */ 
			
		}
		return result;
	}
	
	private IAbstractString evalVarBefore(Name name, Statement stmt) {
		IVariableBinding var = (IVariableBinding) name.resolveBinding();
		Statement prevStmt = ASTUtil.getPrevStmt(stmt);
		if (prevStmt == null) {
			// no previous statement, must be beginning of method declaration
			if (var.isField()) {
				return evalField(name);
			}
			else if (var.isParameter()) {
				if (! supportParameters) {
					throw new UnsupportedStringOpEx("eval Parameter");
				}
				MethodDeclaration method = ASTUtil.getContainingMethodDeclaration(stmt);
				int paramIndex = ASTUtil.getParamIndex(method, var);
				
				if (this.invocationContext != null) {
					// TODO: check that invocation context matches
					AbstractStringEvaluator nextLevelEvaluator = 
						new AbstractStringEvaluator(level+1, null, scope);
					
					return nextLevelEvaluator.eval
						((Expression)this.invocationContext.arguments().get(paramIndex));
				}
				else {
					List<INodeDescriptor> descList = 
						AbstractStringEvaluator.evaluateMethodArgumentAtCallSites(
								Collections.singleton(
										new NodeRequest(
												ASTUtil.getMethodClassName(method), 
												method.getName().toString(),
												paramIndex)), 
							this.scope, this.level + 1);
					
					List<IAbstractString> choices = new ArrayList<IAbstractString>();
					for (INodeDescriptor choiceDesc: descList) {
						if (choiceDesc instanceof IStringNodeDescriptor) {
							choices.add(((IStringNodeDescriptor)choiceDesc).getAbstractValue());
						}
					}
					return new StringChoice(PositionUtil.getPosition(name), choices);
				}
			}
			else {
				throw new UnsupportedStringOpEx
					("getVarValBefore: not param, not field, kind=" + var.getKind());
			}
		}
		else {
			return evalVarAfter(name, prevStmt);
		}
	}
	
	private static boolean isStringBuilderOrBuffer(ITypeBinding typeBinding) {
		return typeBinding.getQualifiedName().equals("java.lang.StringBuffer")
		|| typeBinding.getQualifiedName().equals("java.lang.StringBuilder");
	}
	
}