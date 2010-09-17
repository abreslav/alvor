package com.zeroturnaround.alvor.crawler;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NodeFinder;

import com.zeroturnaround.alvor.cache.PositionUtil;

public class ASTTransformer {
	
	public static ASTNode patchAndReParse(ASTNode node, String replacement) 
	throws JavaModelException, ParseException {
	ICompilationUnit iCUnit = ASTUtil.getICompilationUnit(node);
	String src = iCUnit.getSource();
	String newSource = src.substring(0, node.getStartPosition())
		+ replacement
		+ src.substring(node.getStartPosition() + node.getLength(), src.length());
	
	ASTParser parser = ASTParser.newParser(AST.JLS3);
	parser.setKind(ASTParser.K_COMPILATION_UNIT);
	parser.setResolveBindings(true);
	parser.setProject(iCUnit.getJavaProject());
	parser.setUnitName(PositionUtil.getFileString(node));
	parser.setSource(newSource.toCharArray());
	CompilationUnit newCUnit = (CompilationUnit)parser.createAST(null);
	
	// need to provide some link for original resource
	// TODO: encode also source position difference (code moved from javadoc to body)
	// TODO check if javadoc is still there in copy
//	newCUnit.setProperty(ASTUtil.ORIGINAL_I_COMPILATION_UNIT, iCUnit);
	
	NodeFinder nf = new NodeFinder(newCUnit, node.getStartPosition(), replacement.length());
	ASTNode resultNode = nf.getCoveredNode();
	
	if (resultNode == null && newCUnit.getProblems().length > 0) {
		String problemsStr = "";
		for (IProblem pr : newCUnit.getProblems()) {
			if (pr.isError()) {
				problemsStr += pr + " | ";
			}
		}
		if (problemsStr.length() > 0) {
			throw new ParseException(problemsStr);
		}
	}
	return resultNode;
}


}
