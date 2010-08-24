package com.zeroturnaround.alvor.checkers;

import com.zeroturnaround.alvor.string.IPosition;


public interface ISQLErrorHandler {
	public void handleSQLError(String errorMessage, IPosition position);
	public void handleSQLWarning(String message, IPosition position);
}
