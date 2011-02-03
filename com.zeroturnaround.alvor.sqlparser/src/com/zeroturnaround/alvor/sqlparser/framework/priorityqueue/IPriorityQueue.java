package com.zeroturnaround.alvor.sqlparser.framework.priorityqueue;

public interface IPriorityQueue<E>
{
    void enqueue(E item);
    E dequeue();
    boolean isEmpty();
    void handleDecreasedKey(E item);
}


