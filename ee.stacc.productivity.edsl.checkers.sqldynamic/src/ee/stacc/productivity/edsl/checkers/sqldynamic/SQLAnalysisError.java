package ee.stacc.productivity.edsl.checkers.sqldynamic;

import java.sql.SQLException;

public class SQLAnalysisError extends SQLException {
	private static final long serialVersionUID = 3293394230919567750L;
	private int position = 0;
	
	public SQLAnalysisError(String reason, int position) {
		super(reason);
		this.position = position;
	}
	
	int getPosition() {
		return position;
	}
}
