package ee.stacc.productivity.edsl.crawler;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import ee.stacc.productivity.edsl.cache.CacheService;
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
	private static final ILog LOG = Logs.getLog(AbstractStringEvaluator.class);
	private int maxLevel = 1;
	private boolean supportParameters = true;
	private boolean supportInvocations = true;
	
	private int level;
	private MethodInvocation invocationContext;
	private IJavaElement scope;
	
	public static IAbstractString evaluateExpression(Expression node) {
		AbstractStringEvaluator evaluator = 
			new AbstractStringEvaluator(0, null, getNodeProject(node));
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
		Statement stmt = getContainingStmt(node);
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
	
	private static Statement getPrevStmt(Statement node) {
		//LOG.message("getPrevStmt: " + node.getClass().getName());
		
		if (node.getParent() instanceof Block) {
			Block block = (Block) node.getParent();
			int i = block.statements().indexOf(node);
			
			if (i == 0) { // this is first in block, eg. this block is done
				return getPrevStmt(block);
			} else {
				return (Statement)block.statements().get(i-1);
			}
		} 
		else if (node.getParent() instanceof MethodDeclaration) {
			return null;
		}
		else if (node.getParent() instanceof IfStatement) {
			return getPrevStmt((IfStatement)node.getParent());
		}
		else if (node.getParent() instanceof TryStatement) {
			return getPrevStmt((TryStatement)node.getParent());
		}
		else { 
			throw new UnsupportedStringOpEx("getPrevStatement(" + node.getClass().getName() 
				+ ", parent is " + node.getParent().getClass().getName() + ")");
		}
	}
	
	
	private static Statement getContainingStmt(ASTNode node) {
		assert node != null;
		
		if (node.getParent() instanceof Statement) {
			return (Statement)node.getParent();
		}
		else {
			ASTNode parent = node.getParent();
			if (parent == null) {
				return null;
			}
			return getContainingStmt(parent);
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
		IVariableBinding var = (IVariableBinding) name.resolveBinding();
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
			return evalVarAfter(name, getLastStmt((Block)stmt));
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
			else if (varIsUsedIn(var, inv)) {
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
	
	private boolean varIsUsedIn(IVariableBinding var, Expression expr) {
		if (expr instanceof MethodInvocation) {
			MethodInvocation inv = (MethodInvocation) expr;
			if (inv.getExpression() != null && varIsUsedIn(var, inv.getExpression())) {
				return true;
			}
			else {
				for (Object arg : inv.arguments()) {
					if (varIsUsedIn(var, (Expression) arg)) {
						return true;
					}
				}
				return false;
			}
		}
		else if (expr instanceof Name) {
			return ((Name) expr).resolveBinding().isEqualTo(var);
		}
		else if (expr instanceof InfixExpression) {
			InfixExpression inf = (InfixExpression) expr;
			if (varIsUsedIn(var, inf.getLeftOperand())) {
				return true;
			}
			if (varIsUsedIn(var, inf.getRightOperand())) {
				return true;
			}
			for (Object o : inf.extendedOperands()) {
				if (varIsUsedIn(var, (Expression)o)) {
					return true;
				}
			}
			return false;
		}
		else if (expr instanceof StringLiteral 
				|| expr instanceof NumberLiteral
				|| expr instanceof BooleanLiteral) {
			return false;
		}
		else {
			throw new UnsupportedStringOpEx("Checking whether var is mentioned. "
					+ "Unsupported expression: "
					+ expr.getClass());
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
				throw new UnsupportedStringOpEx("MethodInvocation, StringBuilder/Buffer, method not toString");
			}
		}
		else  {
			if (! supportInvocations) {
				throw new UnsupportedStringOpEx("Method call");
			}
			AbstractStringEvaluator evaluatorWithNewContext = 
				new AbstractStringEvaluator(level+1, inv, scope);

			if (inv.getExpression() == null || inv.getExpression() instanceof ThisExpression) {
				MethodDeclaration decl = getMethodDeclarationByName(NodeSearchEngine.getContainingTypeDeclaration(inv), 
						inv.getName().getIdentifier());
				return evaluatorWithNewContext.getMethodReturnValue(decl);
			}
			else {
				List<MethodDeclaration> decls = NodeSearchEngine.findMethodDeclarations(scope, inv);
				List<IAbstractString> choices = new ArrayList<IAbstractString>();
				for (MethodDeclaration decl: decls) {
					choices.add(evaluatorWithNewContext.getMethodReturnValue(decl));
				}
				return new StringChoice(PositionUtil.getPosition(inv), choices);
			}
		}			
	}
	
	private IAbstractString getMethodReturnValue(MethodDeclaration decl) {
		
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
	
	private static Statement getLastStmt(Block block) {
		return (Statement)block.statements().get(block.statements().size()-1);
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
		Statement prevStmt = getPrevStmt(stmt);
		if (prevStmt == null) {
			// no previous statement, must be beginning of method declaration
			if (var.isField()) {
				return evalField(name);
			}
			else if (var.isParameter()) {
				if (! supportParameters) {
					throw new UnsupportedStringOpEx("eval Parameter");
				}
				MethodDeclaration method = getContainingMethodDeclaration(stmt);
				int paramIndex = getParamIndex(method, var);
				
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
												getMethodClassName(method), 
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
	
	private static IJavaProject getNodeProject(ASTNode node) {
		assert node.getRoot() instanceof CompilationUnit;
		CompilationUnit cUnit = (CompilationUnit)node.getRoot();
		return cUnit.getJavaElement().getJavaProject();
	}
	
	/*
	private static IFile getNodeFile(ASTNode node) {
		assert node.getRoot() instanceof CompilationUnit;
		CompilationUnit cUnit = (CompilationUnit)node.getRoot();
		return (IFile)cUnit.getTypeRoot().getResource();
	}
	*/
	
	private static MethodDeclaration getContainingMethodDeclaration(ASTNode node) {
		ASTNode result = node;
		while (result != null && ! (result instanceof MethodDeclaration)) {
			result = result.getParent();
		}
		return (MethodDeclaration)result;
	}
	
	private static int getParamIndex(MethodDeclaration method, IBinding param) {
		int i = 1;
		for (Object elem: method.parameters()) {
			SingleVariableDeclaration decl = (SingleVariableDeclaration)elem;
			if (decl.resolveBinding().equals(param)) {
				return i;
			}
			i++;
		}
		return -1;
	}
	
	private static String getMethodClassName(MethodDeclaration method) {
		assert (method.getParent() instanceof TypeDeclaration);
		TypeDeclaration typeDecl = (TypeDeclaration)method.getParent();
		ITypeBinding classBinding = typeDecl.resolveBinding();
		return classBinding.getQualifiedName();
	}
	
	private static boolean isStringBuilderOrBuffer(ITypeBinding typeBinding) {
		return typeBinding.getQualifiedName().equals("java.lang.StringBuffer")
		|| typeBinding.getQualifiedName().equals("java.lang.StringBuilder");
	}
	
	private static MethodDeclaration getMethodDeclarationByName(TypeDeclaration typeDecl,
			String methodName) {
		for (MethodDeclaration method: typeDecl.getMethods()) {
			if (method.getName().getIdentifier().equals(methodName)) {
				return method;
			}
		}
		throw new IllegalArgumentException("Method '" + methodName + "' not found");
	}
}