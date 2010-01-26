package ee.stacc.productivity.edsl.crawler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaElement;
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
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import ee.stacc.productivity.edsl.string.IAbstractString;
import ee.stacc.productivity.edsl.string.StringChoice;
import ee.stacc.productivity.edsl.string.StringConstant;
import ee.stacc.productivity.edsl.string.StringSequence;


public class AbstractStringEvaluator {
	
	public static IAbstractString getValOf(Expression node, int level) {
		ITypeBinding type = node.resolveTypeBinding();
		assert type != null;
		/*
		System.out.println("getValOf: class=" + node.getClass().getSimpleName()
				+ ", toString=" + node.toString()
				+ ", binding=" + type.getName());
		*/
		
		if (type.getName().equals("int")) {
			throw new UnsupportedStringOpEx	("TODO: int expression");
		}
		else if (node instanceof SimpleName) {
			IBinding var = ((SimpleName)node).resolveBinding();
			assert var instanceof IVariableBinding;
			return getVarValBefore((IVariableBinding)var, getContainingStmt(node), level);
		}
		else if (node instanceof StringLiteral) {
			return new StringConstant(((StringLiteral)node).getLiteralValue());
		}
		else if (node instanceof ParenthesizedExpression) {
			return getValOf(((ParenthesizedExpression)node).getExpression(), level);
		}
		else if (node instanceof InfixExpression) {
			return getValOfInfixExpression((InfixExpression)node, level);
		}
		else if (node instanceof MethodInvocation) {
			MethodInvocation inv = (MethodInvocation)node;
				return getValOfMethodInvocation(inv, level);
		}
		else {
			throw new UnsupportedStringOpEx
				("TODO: getValOf(" + node.getClass().getName() + ")");
		}
	}

	private static IAbstractString getValOfInfixExpression(InfixExpression expr,
			int level) {
		if (expr.getOperator() == InfixExpression.Operator.PLUS) {
			List<IAbstractString> ops = new ArrayList<IAbstractString>();
			ops.add(getValOf(expr.getLeftOperand(), level));
			ops.add(getValOf(expr.getRightOperand(), level));
			for (Object operand: expr.extendedOperands()) {
				ops.add(getValOf((Expression)operand, level));
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
	
	private static IAbstractString getVarValAfterIf(IVariableBinding var, IfStatement stmt, int level) {
		IAbstractString ifVal = getVarValAfter(var, stmt.getThenStatement(), level);
		IAbstractString elseVal = null;
		
		if (stmt.getElseStatement() != null) {
			elseVal = getVarValAfter(var, stmt.getElseStatement(), level);
		} else {
			elseVal = getVarValBefore(var, stmt, level);
		}
		
		if (ifVal.equals(elseVal)) {
			return ifVal;
		} else {
			return new StringChoice(ifVal, elseVal);
		}
	}
	
	private static IAbstractString getVarValAfterVarDec(IVariableBinding var,
			VariableDeclarationStatement stmt, int level) {
		
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
								return getValOf(arg, level);
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
					return getValOf(vDec.getInitializer(), level);
				}
			}
		}
		return getVarValBefore(var, stmt, level);
	}
	
	private static IAbstractString getVarValAfterAss(IVariableBinding var, 
			ExpressionStatement stmt, int level) {
		assert stmt.getExpression() instanceof Assignment;
		
		// TODO StringBuilder variable can be assigned also
		
		Assignment ass = (Assignment)stmt.getExpression();
		
		if (ass.getLeftHandSide() instanceof SimpleName
			&& ((SimpleName)ass.getLeftHandSide()).resolveBinding().isEqualTo(var)) {
			
			IAbstractString rhs = getValOf(ass.getRightHandSide(), level);
			
			if (ass.getOperator() == Assignment.Operator.ASSIGN) {
				return rhs;
			}
			else if (ass.getOperator() == Assignment.Operator.PLUS_ASSIGN) {
				return new StringSequence(getVarValBefore(var, stmt, level), rhs);
			}
			else {
				throw new UnsupportedStringOpEx("getVarValAfterAss: unknown operator");
			}
		}
		else { // wrong assignment, this statement doesn't change var (hopefully :)
			return getVarValBefore(var, stmt, level);
		}
	}
	
	private static IAbstractString getVarValAfter(IVariableBinding var, Statement stmt, int level) {
		//System.out.println("getVarValAfter: var=" + var.getName()
		//		+ ", stmt="+ stmt.getClass().getName());
		
		if (stmt instanceof ExpressionStatement) {
			Expression expr = ((ExpressionStatement)stmt).getExpression(); 
			if (expr instanceof Assignment) {
				return getVarValAfterAss(var, (ExpressionStatement)stmt, level);
			}
			else if (expr instanceof MethodInvocation) {
				return getVarValAfterMethodInvStmt(var, (ExpressionStatement)stmt, level);
			}
			else {
				throw new UnsupportedStringOpEx
					("getVarValAfter(_, ExpressionStatement." + expr.getClass() + ")");
			}
		}
		else if (stmt instanceof VariableDeclarationStatement) {
			return getVarValAfterVarDec(var, (VariableDeclarationStatement)stmt, level);
		}
		else if (stmt instanceof IfStatement) {
			return getVarValAfterIf(var, (IfStatement)stmt, level);
		}
		else if (stmt instanceof Block) {
			return getVarValAfter(var, getLastStmt((Block)stmt), level);
		}
		else if (stmt instanceof ReturnStatement) {
			return getVarValBefore(var, stmt, level);
		}
		else { // other kind of statement
			throw new UnsupportedStringOpEx("TODO: getVarValAfter(_, " + stmt.getClass().getName() + ")");
		} 
	}
	
	private static IAbstractString getVarValAfterMethodInvStmt(IVariableBinding var,
			ExpressionStatement stmt, int level) {
		MethodInvocation inv = (MethodInvocation)stmt.getExpression();
		Expression expr = inv.getExpression();
		
		if (expr instanceof SimpleName && ((SimpleName)expr).resolveBinding().isEqualTo(var)) {
			assert isStringBuilderOrBuffer(var.getType());
			
			if (inv.getName().getIdentifier().equals("append")) {
				return new StringSequence(
						getVarValBefore(var, stmt, level),
						getValOf((Expression)inv.arguments().get(0), level));
			}
			else {
				throw new UnsupportedStringOpEx("getVarValAfterMethodInvStmt(StringBuilder/Buffer."
						+ inv.getName().getIdentifier() + ")");
			}
		} 
		else {
			// TODO at the moment just assuming that if i don't understand
			//  the construction then it doesn't modify var
			return getVarValBefore(var, stmt, level);
		}
	}
	
	private static IAbstractString getValOfMethodInvocation(MethodInvocation inv, int level) {
		ITypeBinding binding = inv.getExpression().resolveTypeBinding();

		if (isStringBuilderOrBuffer(binding)) {
			if (!(inv.getExpression() instanceof SimpleName)) {
				throw new UnsupportedStringOpEx("MethodInvocation, expression not SimpleName");
			}
			if (! "toString".equals(inv.getName().getIdentifier())) {
				throw new UnsupportedStringOpEx("MethodInvocation, StringBuilder/Buffer, method not toString");
			}
			IBinding var = ((SimpleName)inv.getExpression()).resolveBinding();
			return getVarValBefore((IVariableBinding)var, getContainingStmt(inv), level);
		}
		else if (inv.getExpression() == null || inv.getExpression() instanceof ThisExpression) {
			MethodDeclaration decl = getMethodDeclaration(getContainingTypeDeclaration(inv), 
					inv.getName().getIdentifier()); 
			return getMethodReturnValueForInvocation(decl, inv, level);
		}
		else {
			//throw new UnsupportedStringOpEx("too complex method invocation");
			List<MethodDeclaration> decls = Crawler.findMethodDeclarations(inv);
			List<IAbstractString> choices = new ArrayList<IAbstractString>();
			for (MethodDeclaration decl: decls) {
				choices.add(getMethodReturnValueForInvocation(decl, inv, level));
			}
			return new StringChoice(choices);
		}			
	}
	
	private static IAbstractString getMethodReturnValueForInvocation(MethodDeclaration decl, 
			MethodInvocation inv, int level) {
		assert inv.resolveTypeBinding().getName().equals("String");
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
		// TODO for evaluating arguments it should use only given invocation, not all of them
		
		List<IAbstractString> options = new ArrayList<IAbstractString>();
		for (ReturnStatement ret: returnStmts) {
			options.add(getValOf(ret.getExpression(), level));
		}
		return new StringChoice(options);
	}
	
	private static Statement getLastStmt(Block block) {
		return (Statement)block.statements().get(block.statements().size()-1);
	}
	
	private static IAbstractString getVarValBefore(IVariableBinding var, Statement stmt, int level) {
		//System.out.println("getVarValBefore: var=" + var.getName()
		//		+ ", stmt="+ stmt.getClass().getName());
		
		Statement prevStmt = getPrevStmt(stmt);
		if (prevStmt == null) {
			// no prev statement, must be beginning of method declaration
			// and value should come from parameter
			
			// TODO: can be also object field
			
			MethodDeclaration method = getContainingMethodDeclaration(stmt);
			int argIndex = getParamIndex(method, var);
			/*
			List<IAbstractString> choices = Crawler.findArgumentAbstractValuesAtCallSites
				(getMethodClassName(method) , method.getName().toString(), 
						argIndex, getNodeProject(stmt), level+1);
			return new StringChoice(choices);
			*/
			throw new UnsupportedStringOpEx("######### TODO: analyze argument '" + var.getName() 
					+ "'(" +argIndex+ ") at all call sites of method '" + method.getName() + "'");
		}
		else {
			return getVarValAfter(var, prevStmt, level);
		}
	}
	
	private static IFile getNodeFile(ASTNode node) {
		CompilationUnit unit = (CompilationUnit)node.getRoot();
		return (IFile)unit.getTypeRoot().getResource();
	}
	
	private static IJavaElement getNodeProject(ASTNode node) {
		assert node.getRoot() instanceof CompilationUnit;
		CompilationUnit cUnit = (CompilationUnit)node.getRoot();
		return cUnit.getJavaElement().getJavaProject();
	}
	
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
	
	private static TypeDeclaration getContainingTypeDeclaration(ASTNode node) {
		ASTNode result = node;
		while (result != null && ! (result instanceof TypeDeclaration)) {
			result = result.getParent();
		}
		return (TypeDeclaration)result;
	}
	
	private static MethodDeclaration getMethodDeclaration(TypeDeclaration typeDecl,
			String methodName) {
		for (MethodDeclaration method: typeDecl.getMethods()) {
			if (method.getName().getIdentifier().equals(methodName)) {
				return method;
			}
		}
		throw new IllegalArgumentException("Method '" + methodName + "' not found");
	}
}