package com.zeroturnaround.alvor.crawler;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import com.zeroturnaround.alvor.cache.UnsupportedStringOpEx;

public class ASTUtil {
	public static TypeDeclaration getContainingTypeDeclaration(ASTNode node) {
		ASTNode result = node;
		while (result != null && ! (result instanceof TypeDeclaration)) {
			result = result.getParent();
		}
		return (TypeDeclaration)result;
	}
	
	public static MethodDeclaration getMethodDeclarationByName(TypeDeclaration typeDecl,
			String methodName) {
		for (MethodDeclaration method: typeDecl.getMethods()) {
			if (method.getName().getIdentifier().equals(methodName)) {
				return method;
			}
		}
		throw new IllegalArgumentException("Method '" + methodName + "' not found");
	}

	public static boolean varIsUsedIn(IVariableBinding var, Expression expr) {
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
		else if (expr instanceof ConditionalExpression) {
			ConditionalExpression cExp = (ConditionalExpression)expr;
			return varIsUsedIn(var, cExp.getExpression())
				|| varIsUsedIn(var, cExp.getThenExpression())
				|| varIsUsedIn(var, cExp.getElseExpression());
		}
		else if (expr instanceof StringLiteral 
				|| expr instanceof NumberLiteral
				|| expr instanceof BooleanLiteral) {
			return false;
		}
		else {
			throw new UnsupportedStringOpEx("Checking whether var is mentioned. "
					+ "Unsupported expression: "
					+ expr.getClass(), expr);
		}
	}
	
	/*package*/ static int getNodeLineNumber(int offset, ASTNode node) {
		if (node.getRoot() instanceof CompilationUnit) {
			return ((CompilationUnit)node.getRoot()).getLineNumber(offset);
		}
		else {
			return -1;
		}
	}
	
	public static Statement getPrevStmt(Statement node) {
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
				+ ", parent is " + node.getParent().getClass().getName() + ")", node);
		}
	}
	
	
	public static Statement getContainingStmt(ASTNode node) {
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
	
	public static boolean invocationMayUseDeclaration (MethodInvocation inv, MethodDeclaration decl) {
		// exclude abstract methods
		if (inv.getName().getIdentifier().contains("getSequenceNextValueFunction")
				|| inv.getName().getIdentifier().contains("getNextValueSQL")) {
		}
		
		if (decl.getBody() == null) {
			return false;
		}
		
		IMethodBinding declMethodBinding = decl.resolveBinding();
		IMethodBinding invMethodBinding = inv.resolveMethodBinding();
		ITypeBinding invType = invMethodBinding.getDeclaringClass();
		ITypeBinding declType = declMethodBinding.getDeclaringClass();
		
		// easy case
		if (invMethodBinding.isEqualTo(declMethodBinding)) {
			return true;
		}
		
		if (!inv.getName().getIdentifier().equals(decl.getName().getIdentifier())) {
			throw new IllegalStateException("INV: " + inv.getName().getIdentifier()
					+ ", DECL: " + decl.getName().getIdentifier());
		}
		
		if (inv.arguments().size() != decl.parameters().size()) {
			return false;
		}
		
		// finally compare parameter types
		ITypeBinding[] invPTypes = inv.resolveMethodBinding().getParameterTypes();
		ITypeBinding[] declPTypes = decl.resolveBinding().getParameterTypes();
		
		for (int i = 0; i < invPTypes.length; i++) {
			if (!invPTypes[i].isEqualTo(declPTypes[i])) {
				return false;
			}
		}
		
		// FIXME: need proper checks for expression type here
		// not working if units are not parsed together
		// if (! declType.isSubTypeCompatible(invType)) {
		//	return false;
		//}
		return isSubtypeOf(declType, invType);		
		
		// Approach 2
		// Does not work if units are not parsed together
		//return invBinding.isEqualTo(declBinding) || declBinding.overrides(invBinding);
	}
	
	private static boolean isSubtypeOf(ITypeBinding sub, ITypeBinding sup) {
		
		// FIXME it's too simplistic at the moment, should do recursion 
		
		if (sub.isEqualTo(sup)) {
			return true;
		}
		
		if (sub.getSuperclass() != null && sub.getSuperclass().isEqualTo(sup)) {
			return true;
		}

		for (ITypeBinding iFace : sub.getInterfaces()) {
			if (iFace.isEqualTo(sup)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static TagElement getJavadocTag(Javadoc javadoc, String name) {
		if (javadoc == null) {
			return null;
		}
		for (Object element : javadoc.tags()) {
			TagElement tag = (TagElement)element;
			if (tag != null && name.equals(tag.getTagName())) {
				return tag;
			}
		}
		return null;
	}
	
	public static String getTagElementText(TagElement tag) {
		if (tag.fragments().size() == 1 
				&& tag.fragments().get(0) instanceof TextElement) {
			TextElement textElement = (TextElement)tag.fragments().get(0);
			return textElement.getText();
		} else {
			return null;
		}
	}
	
	public static IJavaProject getNodeProject(ASTNode node) {
		assert node.getRoot() instanceof CompilationUnit;
		CompilationUnit cUnit = (CompilationUnit)node.getRoot();
		return cUnit.getJavaElement().getJavaProject();
	}

	public static Statement getLastStmt(Block block) {
		return (Statement)block.statements().get(block.statements().size()-1);
	}
	
	public static String getMethodClassName(MethodDeclaration method) {
		assert (method.getParent() instanceof TypeDeclaration);
		TypeDeclaration typeDecl = (TypeDeclaration)method.getParent();
		ITypeBinding classBinding = typeDecl.resolveBinding();
		return classBinding.getQualifiedName();
	}
	
	public static MethodDeclaration getContainingMethodDeclaration(ASTNode node) {
		ASTNode result = node;
		while (result != null && ! (result instanceof MethodDeclaration)) {
			result = result.getParent();
		}
		return (MethodDeclaration)result;
	}
	
	// NB! uses 0-based indexing
	public static int getParamIndex0(MethodDeclaration method, IBinding param) {
		int i = 0;
		for (Object elem: method.parameters()) {
			SingleVariableDeclaration decl = (SingleVariableDeclaration)elem;
			if (decl.resolveBinding().isEqualTo(param)) {
				return i;
			}
			i++;
		}
		return -1;
	}
	
	// NB! uses 0-based indexing
	public static int getArgumentIndex0(MethodInvocation inv, IBinding var) {
		int i = 0;
		for (Object elem: inv.arguments()) {
			if (elem instanceof Name 
					&& ((Name)elem).resolveBinding().isEqualTo(var)) {
				return i;
			}
			i++;
		}
		return -1;
	}
	
	public static VariableDeclaration getVarDeclFragment(VariableDeclarationStatement stmt, Name name) {
		for (Object frag : stmt.fragments()) {
			VariableDeclaration vDec = (VariableDeclaration)frag;
			
			if (sameBinding(vDec.getName(), name)) {
				return vDec;
			}
		}
		return null;
	}
	
	public static CompilationUnit getCompilationUnit(ASTNode node) {
		assert node != null;
		return (CompilationUnit)node.getRoot();
	}
	
	public static ICompilationUnit getICompilationUnit(ASTNode node) {
		return (ICompilationUnit)getCompilationUnit(node).getJavaElement();
	}
	
	public static boolean sameBinding(Expression exp, Name name) {
		return sameBinding(exp, name.resolveBinding());
	}
	
	public static boolean sameBinding(Expression exp, IBinding var) {
		return (exp instanceof Name)
			&& ((Name)exp).resolveBinding().isEqualTo(var);
	}
	
	/*
	 * Return true if a is in a loop and b is not in this loop (body)
	 */
	public static boolean inALoopSeparatingFrom(ASTNode nodeA, ASTNode nodeB) {
		assert nodeA != null;
		assert nodeB != null;
		
		if (nodeA == nodeB) {
			return false;
		}
		else if (nodeA instanceof MethodDeclaration) {
			return false;
		}
		else if (!isLoopStatement(nodeA)) {
			// go up with A until reach a loop
			return inALoopSeparatingFrom(nodeA.getParent(), nodeB);
		}
		// here nodeA is loop
		else if (nodeB instanceof MethodDeclaration) {
			return true;
		}
		else {
			return inALoopSeparatingFrom(nodeA, nodeB.getParent());
		}
	}
	
	public static ASTNode getContainingLoop(ASTNode node) {
		if (node == null) {
			return null;
		}
		else if (isLoopStatement(node)) {
			return node;
		}
		else {
			return getContainingLoop(node.getParent());
		}
	}
	
	public static boolean containsConditional(ASTNode node) {
		// TODO a hack
		if (node == null) {
			return false;
		}
		else if (node instanceof IfStatement) {
			return true;
		}
		else if (node instanceof Block) {
			for (Object stmt : ((Block)node).statements()) {
				if (containsConditional((ASTNode)stmt)) {
					return true;
				}
			}
			return false;
		}
		else if (isLoopStatement(node)) {
			return containsConditional(getLoopBody(node));
		}
		else if (node instanceof TryStatement) {
			TryStatement tStmt = (TryStatement)node;
			return containsConditional(tStmt.getBody());
		}
		else {
			return false;
		}
	}
	
	public static boolean isLoopStatement(ASTNode node) {
		return node instanceof WhileStatement
			|| node instanceof ForStatement
			|| node instanceof EnhancedForStatement
			|| node instanceof DoStatement; 
	}
	
	public static Statement getLoopBody(ASTNode loop) {
		if (loop instanceof ForStatement) {
			return ((ForStatement)loop).getBody();
		}
		else if (loop instanceof EnhancedForStatement) {
			return ((EnhancedForStatement)loop).getBody();
		}
		else if (loop instanceof WhileStatement) {
			return ((WhileStatement)loop).getBody();
		}
		else if (loop instanceof DoStatement) {
			return ((DoStatement)loop).getBody();
		}
		else {
			throw new IllegalArgumentException();
		}
	}
	
	/*
	private static IFile getNodeFile(ASTNode node) {
		assert node.getRoot() instanceof CompilationUnit;
		CompilationUnit cUnit = (CompilationUnit)node.getRoot();
		return (IFile)cUnit.getTypeRoot().getResource();
	}
	*/
	
	public static boolean isSimpleNode(ASTNode node) {
		return node instanceof Name
			|| node instanceof NullLiteral
			|| node instanceof NumberLiteral
			|| node instanceof StringLiteral
			|| node instanceof BooleanLiteral
			|| node instanceof CharacterLiteral
			|| node instanceof TypeLiteral
			|| node instanceof Annotation
			|| node instanceof ThisExpression
			|| node instanceof BreakStatement
			|| node instanceof ContinueStatement;
	}
	
	public static  String getErasedSignature(IMethodBinding m) {
		String paramString;
		ITypeBinding[] parameterTypes = m.getParameterTypes();
		if (parameterTypes.length == 1) {
			paramString = parameterTypes[0].getQualifiedName();
		} else if (parameterTypes.length > 0) {
			StringBuilder params = new StringBuilder();
			for (ITypeBinding t : parameterTypes) {
				params.append(t.getQualifiedName()).append(" ");
			}
			paramString = params.toString();
		} else {
			paramString = "";
		}
		return m.getDeclaringClass().getQualifiedName()
			+ "." + m.getName()
			+ "(" + paramString + ")"
			;
	}

	public static boolean isString(ITypeBinding typeBinding) {
		return typeBinding.getQualifiedName().equals("java.lang.String");
	}

	public static boolean isStringBuilderOrBuffer(ITypeBinding typeBinding) {
		return typeBinding.getQualifiedName().equals("java.lang.StringBuffer")
		|| typeBinding.getQualifiedName().equals("java.lang.StringBuilder");
	}

	public static boolean isIntegral(ITypeBinding typeBinding) {
		return typeBinding.getQualifiedName().equals("java.lang.Integer")
		|| typeBinding.getQualifiedName().equals("java.lang.Long");
	}

	public static boolean isStringOrStringBuilderOrBuffer(ITypeBinding typeBinding) {
		return isString(typeBinding) || isStringBuilderOrBuffer(typeBinding);
	}
	
	public static String getArgumentTypesString(IMethodBinding binding) {
		String result = "(";
		for (int i = 0; i < binding.getParameterTypes().length; i++) {
			if (i > 0) {
				result += ',';
			}
			//result += binding.getParameterTypes()[i].getQualifiedName();
			result += binding.getParameterTypes()[i].getName();
		}
		result += ")";
		
		return result;
	}

}
