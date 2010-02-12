package ee.stacc.productivity.edsl.crawler;

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
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
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
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import ee.stacc.productivity.edsl.checkers.INodeDescriptor;
import ee.stacc.productivity.edsl.checkers.IStringNodeDescriptor;
import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.StringChoice;
import ee.stacc.productivity.edsl.string.StringConstant;
import ee.stacc.productivity.edsl.string.StringSequence;


public class AbstractStringEvaluator {
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
		ITypeBinding type = node.resolveTypeBinding();
		assert type != null;
		
		if (type.getName().equals("int")) {
			throw new UnsupportedStringOpEx	("TODO: int expression");
		}
		else if (node instanceof SimpleName) {
			IBinding var = ((SimpleName)node).resolveBinding();
			assert var instanceof IVariableBinding;
			return evalVarBefore((IVariableBinding)var, getContainingStmt(node));
		}
		else if (node instanceof StringLiteral) {
			return new StringConstant(((StringLiteral)node).getLiteralValue());
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
		else {
			throw new UnsupportedStringOpEx
				("TODO: getValOf(" + node.getClass().getName() + ")");
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
			return new StringSequence(ops);
		}
		else {
			throw new UnsupportedStringOpEx
				("TODO: getValOf( infix op = " + expr.getOperator() + ")");
		}
	}
	
	private static Statement getPrevStmt(Statement node) {
		//System.out.println("getPrevStmt: " + node.getClass().getName());
		
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
			throw new UnsupportedStringOpEx("TODO: getPrevStatement(" + node.getClass().getName() 
				+ ", parent is " + node.getParent().getClass().getName() + ")");
		}
	}
	
	
	private static Statement getContainingStmt(ASTNode node) {
		assert node != null;
		
		if (node.getParent() instanceof Statement) {
			return (Statement)node.getParent();
		}
		else {
			return getContainingStmt(node.getParent());
		}
	}
	
	private IAbstractString evalVarAfterIf(IVariableBinding var, IfStatement stmt) {
		IAbstractString ifVal = evalVarAfter(var, stmt.getThenStatement());
		IAbstractString elseVal = null;
		
		if (stmt.getElseStatement() != null) {
			elseVal = evalVarAfter(var, stmt.getElseStatement());
		} else {
			elseVal = evalVarBefore(var, stmt);
		}
		
		if (ifVal.equals(elseVal)) {
			return ifVal;
		} else {
			return new StringChoice(ifVal, elseVal);
		}
	}
	
	private IAbstractString evalVarAfterDecl(IVariableBinding var,
			VariableDeclarationStatement stmt) {
		
		// May include declarations for several variables
		for (int i=stmt.fragments().size()-1; i>=0; i--) {
			VariableDeclaration vDec = (VariableDeclaration)stmt.fragments().get(i);
			
			if (vDec.getName().resolveBinding().isEqualTo(var)) {
				if (isStringBuilderOrBuffer(var.getType())) {
					if (vDec.getInitializer() instanceof ClassInstanceCreation) {
						ClassInstanceCreation cic = (ClassInstanceCreation)vDec.getInitializer();
						assert isStringBuilderOrBuffer(cic.getType().resolveBinding());
						if (cic.arguments().size() == 1) {
							Expression arg = (Expression)cic.arguments().get(0);
							if (arg.resolveTypeBinding().getName().equals("String")) {
								return eval(arg);
							}
							else if (arg.resolveTypeBinding().getName().equals("int")) {
								return new StringConstant("");
							}
							else { // CharSequence
								throw new UnsupportedStringOpEx("Unknown StringBuilder/Buffer constructor: " 
										+ arg.resolveTypeBinding().getName());
							}
						} else {
							return new StringConstant("");
						}
					}
					else {
						throw new UnsupportedStringOpEx("getVarValAfterVarDec: initializer is "
								+ vDec.getInitializer().getClass());
					}
				}
				else {
					assert var.getType().getName().equals("String");
					return eval(vDec.getInitializer());
				}
			}
		}
		return evalVarBefore(var, stmt);
	}
	
	private IAbstractString evalVarAfterAss(IVariableBinding var, 
			ExpressionStatement stmt) {
		assert stmt.getExpression() instanceof Assignment;
		
		// TODO StringBuilder variable can be assigned also
		
		Assignment ass = (Assignment)stmt.getExpression();
		
		if (ass.getLeftHandSide() instanceof SimpleName
			&& ((SimpleName)ass.getLeftHandSide()).resolveBinding().isEqualTo(var)) {
			
			IAbstractString rhs = eval(ass.getRightHandSide());
			
			if (ass.getOperator() == Assignment.Operator.ASSIGN) {
				return rhs;
			}
			else if (ass.getOperator() == Assignment.Operator.PLUS_ASSIGN) {
				return new StringSequence(evalVarBefore(var, stmt), rhs);
			}
			else {
				throw new UnsupportedStringOpEx("getVarValAfterAss: unknown operator");
			}
		}
		else { // wrong assignment, this statement doesn't change var (hopefully :)
			return evalVarBefore(var, stmt);
		}
	}
	
	private IAbstractString evalVarAfter(IVariableBinding var, Statement stmt) {
		//System.out.println("getVarValAfter: var=" + var.getName()
		//		+ ", stmt="+ stmt.getClass().getName());
		
		if (stmt instanceof ExpressionStatement) {
			Expression expr = ((ExpressionStatement)stmt).getExpression(); 
			if (expr instanceof Assignment) {
				return evalVarAfterAss(var, (ExpressionStatement)stmt);
			}
			else if (expr instanceof MethodInvocation) {
				return evalVarAfterMethodInvStmt(var, (ExpressionStatement)stmt);
			}
			else {
				throw new UnsupportedStringOpEx
					("getVarValAfter(_, ExpressionStatement." + expr.getClass() + ")");
			}
		}
		else if (stmt instanceof VariableDeclarationStatement) {
			return evalVarAfterDecl(var, (VariableDeclarationStatement)stmt);
		}
		else if (stmt instanceof IfStatement) {
			return evalVarAfterIf(var, (IfStatement)stmt);
		}
		else if (stmt instanceof Block) {
			return evalVarAfter(var, getLastStmt((Block)stmt));
		}
		else if (stmt instanceof ReturnStatement) {
			return evalVarBefore(var, stmt);
		}
		else { // other kind of statement
			throw new UnsupportedStringOpEx("TODO: getVarValAfter(_, " + stmt.getClass().getName() + ")");
		} 
	}
	
	private IAbstractString evalVarAfterMethodInvStmt(IVariableBinding var,
			ExpressionStatement stmt) {
		MethodInvocation inv = (MethodInvocation)stmt.getExpression();
		Expression expr = inv.getExpression();
		
		if (expr instanceof SimpleName && ((SimpleName)expr).resolveBinding().isEqualTo(var)) {
			assert isStringBuilderOrBuffer(var.getType());
			
			if (inv.getName().getIdentifier().equals("append")) {
				return new StringSequence(
						evalVarBefore(var, stmt),
						eval((Expression)inv.arguments().get(0)));
			}
			else {
				throw new UnsupportedStringOpEx("getVarValAfterMethodInvStmt(StringBuilder/Buffer."
						+ inv.getName().getIdentifier() + ")");
			}
		} 
		else {
			// TODO at the moment just assuming that if i don't understand
			//  the construction then it doesn't modify var
			return evalVarBefore(var, stmt);
		}
	}
	
	private IAbstractString evalInvocation(MethodInvocation inv) {
		if (inv.getExpression() != null
				&& isStringBuilderOrBuffer(inv.getExpression().resolveTypeBinding())) {
			if (!(inv.getExpression() instanceof SimpleName)) {
				throw new UnsupportedStringOpEx("MethodInvocation, SB, expression not SimpleName");
			}
			if (! "toString".equals(inv.getName().getIdentifier())) {
				throw new UnsupportedStringOpEx("MethodInvocation, StringBuilder/Buffer, method not toString");
			}
			IBinding var = ((SimpleName)inv.getExpression()).resolveBinding();
			return evalVarBefore((IVariableBinding)var, getContainingStmt(inv));
		}
		else  {
			if (! supportInvocations) {
				throw new UnsupportedStringOpEx("Method call");
			}
			AbstractStringEvaluator evaluatorWithNewContext = 
				new AbstractStringEvaluator(level, inv, scope);

			if (inv.getExpression() == null || inv.getExpression() instanceof ThisExpression) {
				MethodDeclaration decl = getMethodDeclarationByName(NodeSearchEngine.getContainingTypeDeclaration(inv), 
						inv.getName().getIdentifier());
				return evaluatorWithNewContext.getMethodReturnValue(decl);
			}
			else if (!(inv.getExpression() instanceof SimpleName)) {
				throw new UnsupportedStringOpEx("MethodInvocation, expression not SimpleName");
			} 
			else {
				List<MethodDeclaration> decls = NodeSearchEngine.findMethodDeclarations(inv);
				List<IAbstractString> choices = new ArrayList<IAbstractString>();
				for (MethodDeclaration decl: decls) {
					choices.add(evaluatorWithNewContext.getMethodReturnValue(decl));
				}
				return new StringChoice(choices);
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
		return new StringChoice(options);
	}
	
	private static Statement getLastStmt(Block block) {
		return (Statement)block.statements().get(block.statements().size()-1);
	}
	
	public static List<IStringNodeDescriptor> evaluateMethodArgumentAtCallSites
			(Collection<NodeRequest> requests,
					IJavaElement scope, int level) {
		String levelPrefix = "";
		for (int i = 0; i < level; i++) {
			levelPrefix += "    ";
		}

		
		AbstractStringEvaluator evaluator = 
			new AbstractStringEvaluator(level, null, scope);
		
		System.out.println(levelPrefix + "###########################################");
		System.out.println(levelPrefix + "searching: ");
		for (NodeRequest nodeRequest : requests) {
			System.out.println(nodeRequest);
		}
		
		// find value from all call-sites
		List<NodeDescriptor> argumentNodes = NodeSearchEngine.findArgumentNodes
			(scope, requests);
		
		List<IStringNodeDescriptor> result = new ArrayList<IStringNodeDescriptor>();
		for (INodeDescriptor sr: argumentNodes) {
			Expression arg = (Expression)sr.getNode();
			StringNodeDescriptor desc = new StringNodeDescriptor(arg, sr.getFile(),
					sr.getLineNumber(), sr.getCharStart(), sr.getCharLength(), null);
			try {
				desc.setAbstractValue(evaluator.eval(arg));
				result.add(desc);
			} catch (UnsupportedStringOpEx e) {
				System.out.println(levelPrefix + "UNSUPPORTED: " + e.getMessage());
				System.out.println(levelPrefix + "    file: " + sr.getFile() + ", line: " 
						+ sr.getLineNumber());	
			} catch (Throwable t) {
				System.out.println(levelPrefix + "PROGRAM ERROR: " + t.getMessage());
				System.out.println(levelPrefix + "    file: " + sr.getFile() + ", line: " 
						+ sr.getLineNumber());	
			}
		}
		return result;
	}
	
	private IAbstractString evalVarBefore(IVariableBinding var, Statement stmt) {
		Statement prevStmt = getPrevStmt(stmt);
		if (prevStmt == null) {
			// no previous statement, must be beginning of method declaration
			if (var.isField()) {
				throw new UnsupportedStringOpEx("getVarValBefore field");
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
					List<IStringNodeDescriptor> descList = 
						AbstractStringEvaluator.evaluateMethodArgumentAtCallSites(
								Collections.singleton(
										new NodeRequest(
												getMethodClassName(method), 
												method.getName().toString(),
												paramIndex)), 
							this.scope, this.level + 1);
					
					List<IAbstractString> choices = new ArrayList<IAbstractString>();
					for (IStringNodeDescriptor choiceDesc: descList) {
						choices.add(choiceDesc.getAbstractValue());
					}
					return new StringChoice(choices);
				}
			}
			else {
				throw new UnsupportedStringOpEx
					("getVarValBefore: not param, not field, kind=" + var.getKind());
			}
		}
		else {
			return evalVarAfter(var, prevStmt);
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