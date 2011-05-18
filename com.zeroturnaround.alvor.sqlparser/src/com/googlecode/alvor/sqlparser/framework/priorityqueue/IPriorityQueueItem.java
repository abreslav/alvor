package com.googlecode.alvor.sqlparser.framework.priorityqueue;

public interface IPriorityQueueItem<T> extends Comparable<IPriorityQueueItem<T>> {
	int getIndex();
	void setIndex(int index);
	T getContents();
}
