package com.zeroturnaround.alvor.crawler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.BindingKey;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.junit.Test;

import com.zeroturnaround.alvor.crawler.util.ASTUtil;
import com.zeroturnaround.alvor.crawler.util.JavaModelUtil;

public class ParsingSpeedTest {
	
	@Test
	public void reportEarvedHotspots() {
		IJavaProject project = JavaModelUtil.getJavaProjectByName("earved");
		Collection<ICompilationUnit> units = JavaModelUtil.getAllCompilationUnits(project, false);
		
		final List<ASTNode> asts = new ArrayList<ASTNode>();
		
		ASTRequestor requestor = new ASTRequestor() {
			@Override
			public void acceptAST(ICompilationUnit source, CompilationUnit ast) {
				//ast.accept(visitor);
				asts.add(ast);
			}
		};
		
		
		String[] bindingKeys = {
//				BindingKey.createTypeBindingKey("java.sql.Connection"),
//				BindingKey.createTypeBindingKey("java.sql.PreparedStatement"),
				};
		
		Iterator<ICompilationUnit> iter = units.iterator();
		
		final ASTVisitor visitor = new ASTVisitor() {
			@Override
			public boolean visit(MethodInvocation node) {
				if (node.getName().getIdentifier().equals("prepareStatement")) {
					//assert node.resolveMethodBinding() != null;
					System.out.println("NODE:" + node);
					System.out.println("--M :" + node.resolveMethodBinding());
//					System.out.println("--T :" + node.resolveTypeBinding());
//					System.out.println("--ET:" + node.getExpression().resolveTypeBinding());
				}
				else {
					//System.out.println("--------" + node);
					
				}
				return false;
			}
		};

		
		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		
		int processed = 0;
		int maxFiles = 1;
		int parseCount = 0;
		while (processed < units.size()) {
			int currentBatchSize = Math.min(maxFiles, units.size() - processed);
			ICompilationUnit[] cUnitArray = new ICompilationUnit[currentBatchSize];
			
			for (int i = 0; i < currentBatchSize; i++) {
				cUnitArray[i] = iter.next();
			}
			asts.clear();
			parser.setProject(project);
			parser.setResolveBindings(true);
			parser.createASTs(cUnitArray, bindingKeys, requestor, null);
			parseCount++;
			processed += currentBatchSize;
			for (ASTNode ast : asts) {
				ast.accept(visitor);
			}
		}
		
		
		
		
		System.out.println("valma, parse count=" + parseCount);
	}
	
	private void reportFileHotspots(ICompilationUnit cUnit) {
		ASTNode ast = ASTUtil.parseCompilationUnit(cUnit, true);
		ASTVisitor visitor = new ASTVisitor() {
			@Override
			public boolean visit(MethodInvocation node) {
				if (node.getName().getIdentifier().equals("query")) {
					System.out.println("          " + node);
				}
				else {
					//System.out.println("--------" + node);
					
				}
				return false;
			}
		};
		
		//ast.accept(visitor);
	}
}
