package ee.stacc.productivity.edsl.crawler;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.search.SearchMatch;

public class ASTUtil {
	static TypeDeclaration getContainingTypeDeclaration(ASTNode node) {
		ASTNode result = node;
		while (result != null && ! (result instanceof TypeDeclaration)) {
			result = result.getParent();
		}
		return (TypeDeclaration)result;
	}
	
	static MethodDeclaration getMethodDeclarationByName(TypeDeclaration typeDecl,
			String methodName) {
		for (MethodDeclaration method: typeDecl.getMethods()) {
			if (method.getName().getIdentifier().equals(methodName)) {
				return method;
			}
		}
		throw new IllegalArgumentException("Method '" + methodName + "' not found");
	}

	static boolean varIsUsedIn(IVariableBinding var, Expression expr) {
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
	
	static int getNodeLineNumber(SearchMatch match, ASTNode node) {
		if (node.getRoot() instanceof CompilationUnit) {
			return ((CompilationUnit)node.getRoot()).getLineNumber(match.getOffset());
		}
		else {
			return -1;
		}
	}
	
	static Statement getPrevStmt(Statement node) {
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
	
	
	static Statement getContainingStmt(ASTNode node) {
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
	
	static boolean invocationMayReferToDeclaration (MethodInvocation inv, MethodDeclaration decl) {
		ITypeBinding declType = ASTUtil.getContainingTypeDeclaration(decl).resolveBinding();
		ITypeBinding invExprType;
		if (inv.getExpression() != null) {
			invExprType = inv.getExpression().resolveTypeBinding();
		} else {
			invExprType = ASTUtil.getContainingTypeDeclaration(inv).resolveBinding();
		}
		
		// TODO check also arguments' types
		
		
		// TODO this is not sound
		return declType.isEqualTo(invExprType);
		
		// TODO following seems to be too conservative
		// but actually it returns false even when isEqualTo returns true
		/*
		if (invExprType.isRecovered()) {
			invExprType = invExprType.getDeclaringClass();
			assert (!invExprType.isRecovered());
		}
		if (declType.isRecovered()) {
			declType = declType.getDeclaringClass();
			assert (!declType.isRecovered());
		}
		return invExprType.isAssignmentCompatible(declType)
			|| declType.isAssignmentCompatible(invExprType);
		*/
	}
	
	static IJavaProject getNodeProject(ASTNode node) {
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
	
}
