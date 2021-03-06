package com.googlecode.alvor.crawler.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayAccess;
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
import org.eclipse.jdt.core.dom.NodeFinder;
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

import com.googlecode.alvor.common.PositionUtil;
import com.googlecode.alvor.string.IPosition;
import com.googlecode.alvor.string.Position;


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
		else if (expr instanceof ArrayAccess) {
			// not exactly sound, but hopefully there is no modification 
			return false;
		}
		else {
			throw new UnsupportedStringOpExAtNode("Checking whether var is mentioned. "
					+ "Unsupported expression: "
					+ expr.getClass(), expr);
		}
	}
	
	public static int getNodeLineNumber(int offset, ASTNode node) {
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
			throw new UnsupportedStringOpExAtNode("getPrevStatement(" + node.getClass().getName() 
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
	
	public static VariableDeclaration getVarDeclFragment(VariableDeclarationStatement stmt, IVariableBinding var) {
		for (Object frag : stmt.fragments()) {
			VariableDeclaration vDec = (VariableDeclaration)frag;
			
			if (sameBinding(vDec.getName(), var)) {
				return vDec;
			}
		}
		return null;
	}
	
	public static CompilationUnit getCompilationUnit(ASTNode node) {
		assert node != null;
		return (CompilationUnit)node.getRoot();
	}
	
	public static boolean sameBinding(Expression exp, IBinding var) {
		return (exp instanceof Name)
			&& ((Name)exp).resolveBinding().isEqualTo(var);
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
	
	public static ASTNode parseCompilationUnit(ICompilationUnit cUnit, boolean requireBindings) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(requireBindings);
		parser.setSource(cUnit);
		ASTNode ast = parser.createAST(null);
		return ast;

	}

	public static IPosition getPosition(ASTNode node) {
		return new Position(ASTUtil.getFileString(node), node.getStartPosition(), node.getLength());
	}

	public static IFile getFile(ASTNode node) {
		try {
			ICompilationUnit unit = (ICompilationUnit)((CompilationUnit)node.getRoot()).getJavaElement();
			IFile correspondingResource = (IFile) unit.getCorrespondingResource();
			return correspondingResource;
		} catch (JavaModelException e) {
			throw new IllegalStateException(e);
		}
	}

	public static String getFileString(ASTNode node) {
		IResource file = getFile(node);
		return PositionUtil.getFileString(file);
	}

	/** 
	 * output should match with getArgumentTypesAsString(IMethod method)
	 * @param method
	 * @return
	 */
	public static String getSimpleArgumentTypesAsString(IMethodBinding methodBinding) {
		StringBuilder result = new StringBuilder("");
		for (ITypeBinding type : methodBinding.getParameterTypes()) {
			if (result.length() > 0) {
				result.append(","); 
			}
			result.append(type.getErasure().getName());
		}
		
		return result.toString();
	}

	/** 
	 * output should match with getArgumentTypesAsString(IMethodBinding methodBinding)
	 * @param method
	 * @return
	 */
	public static String getSimpleArgumentTypesAsString(IMethod method) {
		StringBuilder result = new StringBuilder("");
		for (String type : method.getParameterTypes()) {
			if (result.length() > 0) {
				result.append(","); 
			}
			String simple = Signature.toString(type);
			simple = Signature.getSimpleName(simple);
			simple = Signature.getTypeErasure(simple);
			result.append(simple);
		}
		
		return result.toString();
	}

	public static ASTNode getASTNode(IPosition position) {
		IFile file = PositionUtil.getFile(position);
		ICompilationUnit cUnit = JavaCore.createCompilationUnitFrom(file);
		int start = position.getStart();
		int length = position.getLength();
		
		if (cUnit == null) {
			throw new IllegalArgumentException("Compilation unit is null for the position: " + position);
		}
		
		return getASTNode(cUnit, start, length);
	}

	private static ASTNode getASTNode(ICompilationUnit cUnit, int start, int length) {
		assert cUnit != null;
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		parser.setSource(cUnit);
		ASTNode ast = parser.createAST(null);
		return NodeFinder.perform(ast, start, length);
	}
	
	
}
