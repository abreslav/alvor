package ru.tolmachev.parsing.simple_cf;

import java.util.Stack;

import ru.tolmachev.core.BGState;

/**
 * Created by IntelliJ IDEA.
 * User: Дмитрий
 * Date: 27.11.11
 * Time: 1:35
 */

public class LinearStack {

    private Stack<BGState> stack = new Stack<BGState>();

    public LinearStack(BGState start) {
        this.stack.push(start);
    }

    public void push(BGState state) {
        stack.push(state);
    }

    public BGState pop() {
        return stack.pop();
    }

    public BGState peek() {
        return stack.peek();
    }

    public int size() {
        return stack.size();
    }
}
