package ee.stacc.productivity.edsl.completion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import ee.stacc.productivity.edsl.checkers.sqlstatic.PositionedCharacterUtil;
import ee.stacc.productivity.edsl.common.logging.ILog;
import ee.stacc.productivity.edsl.common.logging.Logs;
import ee.stacc.productivity.edsl.completion.TokenLocator.CompletionContext;
import ee.stacc.productivity.edsl.crawler.PositionUtil;
import ee.stacc.productivity.edsl.lexer.alphabet.Token;
import ee.stacc.productivity.edsl.lexer.automata.State;
import ee.stacc.productivity.edsl.lexer.automata.Transition;
import ee.stacc.productivity.edsl.lexer.sql.SQLLexer;

public class EDSLCompletionProposalComputer implements IJavaCompletionProposalComputer {

	private static final ILog LOG = Logs.getLog(EDSLCompletionProposalComputer.class);

	private String errorMessage;
	
	@Override
	public List<?> computeCompletionProposals(
			ContentAssistInvocationContext context, IProgressMonitor monitor) {
		errorMessage = "";
		JavaContentAssistInvocationContext jContext = (JavaContentAssistInvocationContext) context;
		println(jContext.getCompilationUnit());

		int invocationOffset = context.getInvocationOffset();
		try {
			CompletionContext completionContext = TokenLocator.INSTANCE.findCompletionContext(PositionUtil.getFileString(jContext.getCompilationUnit().getCorrespondingResource()), invocationOffset);
			
			if (completionContext == null) {
				errorMessage = "No SQL tokens under cursor";
				return Collections.emptyList();
			}
			
			State automaton = completionContext.getAutomaton();
			Set<String> ids = getAllIds(automaton, new HashSet<String>());
			Set<String> proposed = new HashSet<String>();
			List<ICompletionProposal> result = new ArrayList<ICompletionProposal>();
			for (Entry<Transition, String> entry : completionContext.getIncompleteIdsToTheLeft().entrySet()) {
//				Token token = TokenLocator.getToken(entry.getKey());
				String tokenStart = entry.getValue();
				int length = tokenStart.length();
				for (String id : ids) {
					if (!proposed.contains(id) && !id.equals(tokenStart) && id.startsWith(tokenStart)) {
						result.add(new SimpleCompletionProposal(invocationOffset, 0, id.substring(length), id));
						proposed.add(id);
					}
				}
				
			}
			
			
			return result;
		} catch (JavaModelException e) {
			LOG.exception(e);
			errorMessage = e.getClass().getCanonicalName() + ":" + e.getMessage();
			return Collections.emptyList();
		}
		
//		Arrays.asList(
//				new SimpleCompletionProposal(invocationOffset, 0, "asdasd"),
//				new SimpleCompletionProposal(invocationOffset, 0, "asdasd1")
//		);
	}

	private Set<String> getAllIds(State state, Set<String> result) {
		return doGetAllIds(state, result, new HashSet<Transition>());
	}

	private Set<String> doGetAllIds(State state, Set<String> result,
			Set<Transition> visited) {
		for (Transition transition : state.getOutgoingTransitions()) {
			if (visited.contains(transition)) {
				continue;
			}
			visited.add(transition);
			Token token = TokenLocator.getToken(transition);
			if (token != null && SQLLexer.isIdentifier(token.getCode())) {
				result.add(PositionedCharacterUtil.render(token));
			}
			getAllIds(transition.getTo(), result);
		}
		return result;
	}

	@Override
	public List<?> computeContextInformation(
			ContentAssistInvocationContext context, IProgressMonitor monitor) {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

	@Override
	public void sessionEnded() {
		println("Ended");
	}

	@Override
	public void sessionStarted() {
		println("Started");
	}

	private void println(Object string) {
		System.out.println(string);
	}

	private static final class SimpleCompletionProposal implements ICompletionProposal {

		private final int offset;
		private final int length;
		private final String replace;
		private final String display;
		
		public SimpleCompletionProposal(int offset, int length, String replace, String display) {
			this.offset = offset;
			this.length = length;
			this.replace = replace;
			this.display = display;
		}

		@Override
		public void apply(IDocument document) {
			try {
				document.replace(offset, length, replace);
			} catch (BadLocationException e) {
				LOG.exception(e);
			}
		}

		@Override
		public String getDisplayString() {
			return display;
		}
		
		@Override
		public String getAdditionalProposalInfo() {
			return display;
		}

		@Override
		public IContextInformation getContextInformation() {
			return null;
		}

		@Override
		public Image getImage() {
			return null;
		}

		@Override
		public Point getSelection(IDocument document) {
			return null;
		}
		
	}
	
}
