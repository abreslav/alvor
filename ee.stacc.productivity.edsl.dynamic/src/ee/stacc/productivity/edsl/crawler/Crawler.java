package ee.stacc.productivity.edsl.crawler;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.MethodDeclarationMatch;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

import ee.stacc.productivity.edsl.string.IAbstractString;

public class Crawler {
	private static Hashtable<String, List<IAbstractString>> argStringCache = 
		new Hashtable<String, List<IAbstractString>>();
	private static Hashtable<ICompilationUnit, ASTNode> astCache = 
		new Hashtable<ICompilationUnit, ASTNode>();
	private static IJavaElement[] scopeElems = {null};
	
	public static void clearCache() {
		argStringCache.clear();
		//astCache.clear();
	}
	
	static public List<IAbstractString> findArgumentAbstractValuesAtCallSites(
			final String typeName, final String methodName, 
			final int argNo, IJavaElement searchScope, final int level) {
		
		final String argDescriptor = getArgDescriptor(typeName, methodName, argNo); 
		
		final List<IAbstractString> cacheResult = argStringCache.get(argDescriptor);
		if (cacheResult != null) {
			return cacheResult;
		}
		
		final List<IAbstractString> result = new ArrayList<IAbstractString>();
		
		// FIXME temporary, to speed up a bit
		if (methodName.equals("get")) {
			return new ArrayList<IAbstractString>();
		}
		if (level > 3) {
			throw new UnsupportedStringOpEx("argument searching level too deep");
		}
		
		
		SearchPattern pattern = SearchPattern.createPattern(methodName, 
				IJavaSearchConstants.METHOD, IJavaSearchConstants.REFERENCES, 
				SearchPattern.R_EXACT_MATCH);
		scopeElems[0] = searchScope;
		IJavaElement[] elems = {searchScope};
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(elems);
		
		
		SearchRequestor requestor = new SearchRequestor() {
			public void acceptSearchMatch(SearchMatch match) {
				if (! (match.getElement() instanceof IMethod)) {
					return;
				}
				ASTNode node = getASTNode(match);
				
				if (! (node instanceof MethodInvocation)) {
					System.err.println("Crawler: not MethodInvocation, but: " + node.getClass());
					return;
				}
				
				MethodInvocation invoc = (MethodInvocation)node;
				IMethodBinding methodBinding = invoc.resolveMethodBinding();
				
				// TODO: check that object has that type
				
				if (methodBinding == null || methodBinding.getDeclaringClass() == null) {
					System.err.println("TODO: crawler methodBinding.getDeclaringClass() == null");
					return;
				}
				
				if (!methodBinding.getDeclaringClass().getQualifiedName().equals(typeName)
						&& /* FIXME */ !"prepareStatement".equals(methodName)) {
					System.out.println("Wrong match, want: " + typeName + "." + methodName
							+ ", was: " + methodBinding.getDeclaringClass().getQualifiedName()
							+ "." + methodBinding.getName());
					return;
				}
				
				// TODO overloading may complicate things
				if (invoc.arguments().size() < argNo) {
					throw new UnsupportedStringOpEx("can't find required argument (" + argNo + "), method="
							+ methodBinding.getDeclaringClass().getQualifiedName()
							+ "." + methodBinding.getName());
				}
				
				ASTNode arg = (ASTNode) invoc.arguments().get(argNo-1);
				if (arg instanceof Expression) {
					try {
						IAbstractString aStr = AbstractStringEvaluator.getValOf((Expression)arg, level);
						result.add(aStr);
					} 
					catch (UnsupportedStringOpEx e) {
						System.out.println("_______________________________");
						System.out.println("ArgumentFinder for: " + argDescriptor);
						System.out.println("Level  : " + level);
						System.out.println("Unsupp : " + e.getMessage());
						System.out.println("Arg    : " + arg + ", type: " + arg.getClass());
						System.out.println("File   : " + match.getResource());
						System.out.println("Line   : " + getLineNumber(match, arg));
					}
				}
			}
		};
		
		executeSearch(pattern, requestor, scope);
		argStringCache.put(argDescriptor, result);
		return result;
	}
	
	private static void executeSearch(SearchPattern pattern, SearchRequestor requestor,
			IJavaSearchScope scope) {
		SearchEngine searchEngine = new SearchEngine();
		try {
			searchEngine.search(pattern,
				new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant()}, 
				scope, requestor, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	private static int getLineNumber(SearchMatch match, ASTNode node) {
		if (node.getRoot() instanceof CompilationUnit) {
			return ((CompilationUnit)node.getRoot()).getLineNumber(match.getOffset());
		}
		else {
			return -1;
		}
	}
	
	private static String getArgDescriptor(String typeName, String methodName, int argNo) {
		return typeName + "." + methodName + ":" + argNo; 
	}
	
	public static List<MethodDeclaration> findMethodDeclarations(final MethodInvocation inv) {
		ITypeBinding objectType = inv.getExpression().resolveTypeBinding();
		final List<MethodDeclaration> result = new ArrayList<MethodDeclaration>();
		
		SearchPattern pattern = SearchPattern.createPattern(
				inv.getName().getIdentifier(), IJavaSearchConstants.METHOD, 
				IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);
		
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(scopeElems,
				IJavaSearchScope.SOURCES);		
		
		SearchRequestor requestor = new SearchRequestor() {
			public void acceptSearchMatch(SearchMatch match) {
				ASTNode node = getASTNode(match);
				MethodDeclaration decl = (MethodDeclaration)node.getParent();
				if (declarationIsCompatibleWithInvocation(decl, inv)) {
					System.err.println("decl " + inv.getName().getIdentifier() 
							+ " in " + match.getResource()
							+ ", offset=" + match.getOffset());
				}
				else {
					System.err.println("wrong node: " + node.getClass()
							+ ", parent=" + node.getParent().getClass());
				}
			}
		};
		
		executeSearch(pattern, requestor, scope);
		
		return result;
		//throw new UnsupportedStringOpEx("Crawler.getMethodDeclarations, expr type = "
		//		+ objectType.getName());
	}
	
	private static ICompilationUnit getCompilationUnit(SearchMatch match) {
		if (match.getElement() instanceof IMember) {
			return ((IMember)match.getElement()).getCompilationUnit();
		}
		else {
			throw new IllegalArgumentException("getCompilationUnit: Can't find");
		}
	}
	
	private static ASTNode getASTNode(SearchMatch match) {
		ICompilationUnit cUnit = getCompilationUnit(match);
		assert cUnit != null;
		
		ASTNode ast = astCache.get(cUnit);
		if (ast == null) {
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setResolveBindings(true);
			parser.setSource(cUnit);
			ast = parser.createAST(null);
			astCache.put(cUnit, ast);
		}
		return NodeFinder.perform(ast, match.getOffset(), match.getLength());
	}
	
	private static boolean declarationIsCompatibleWithInvocation
		(MethodDeclaration decl, MethodInvocation inv) {
		return false;
	}
}
