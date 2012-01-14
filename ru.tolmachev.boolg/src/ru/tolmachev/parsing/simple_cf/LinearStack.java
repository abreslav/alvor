package ru.tolmachev.parsing.simple_cf;

import java.util.Stack;

import ru.tolmachev.core.BGTableState;

/**
 * Created by IntelliJ IDEA.
 * User: Дмитрий
 * Date: 27.11.11
 * Time: 1:35
 */

public class LinearStack {

    private Stack<BGTableState> stack = new Stack<BGTableState>();

    public LinearStack(BGTableState start) {
        this.stack.push(start);
    }

    public void push(BGTableState state) {
        stack.push(state);
    }

    public BGTableState pop() {
        return stack.pop();
    }

    public BGTableState peek() {
        return stack.peek();
    }

    public int size() {
        return stack.size();
    }
}
