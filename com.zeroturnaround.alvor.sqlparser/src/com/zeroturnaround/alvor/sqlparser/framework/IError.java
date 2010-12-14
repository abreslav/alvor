package com.zeroturnaround.alvor.sqlparser.framework;

public interface IError {

	IError NO_ERROR = new IError() {
		@Override
		public String toString() {
			return "<NO ERROR>";
		}
	};
}
