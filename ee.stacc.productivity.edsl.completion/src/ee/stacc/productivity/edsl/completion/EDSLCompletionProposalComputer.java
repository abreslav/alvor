package ee.stacc.productivity.edsl.completion;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import ee.stacc.productivity.edsl.common.logging.ILog;
import ee.stacc.productivity.edsl.common.logging.Logs;

public class EDSLCompletionProposalComputer implements IJavaCompletionProposalComputer {

	private static final ILog LOG = Logs.getLog(EDSLCompletionProposalComputer.class);
	
	@Override
	public List<?> computeCompletionProposals(
			ContentAssistInvocationContext context, IProgressMonitor monitor) {
		println("p");
		try {
			println(context.computeIdentifierPrefix());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

		JavaContentAssistInvocationContext jContext = (JavaContentAssistInvocationContext) context;
		println(jContext.getCompilationUnit());
		
		return Arrays.asList(
				new SimpleCompletionProposal(context.getInvocationOffset(), 0, "asdasd"),
				new SimpleCompletionProposal(context.getInvocationOffset(), 0, "asdasd1")
		);
	}

	@Override
	public List<?> computeContextInformation(
			ContentAssistInvocationContext context, IProgressMonitor monitor) {
		println("c");
		return null;
	}

	@Override
	public String getErrorMessage() {
		return "Error123";
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
		
		public SimpleCompletionProposal(int offset, int length, String replace) {
			this.offset = offset;
			this.length = length;
			this.replace = replace;
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
		public String getAdditionalProposalInfo() {
			return replace;
		}

		@Override
		public IContextInformation getContextInformation() {
			return new IContextInformation() {
				
				@Override
				public String getInformationDisplayString() {
					return replace;
				}
				
				@Override
				public Image getImage() {
					return null;
				}
				
				@Override
				public String getContextDisplayString() {
					return "CDS";
				}
			};
		}

		@Override
		public String getDisplayString() {
			return replace;
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
