package ru.tolmachev.parsing.simple_cf;

import java.util.Stack;

import ru.tolmachev.core.State;

/**
 * Created by IntelliJ IDEA.
 * User: Дмитрий
 * Date: 27.11.11
 * Time: 1:35
 */

public class LinearStack {

    private Stack<State> stack = new Stack<State>();

    public LinearStack(State start) {
        this.stack.push(start);
    }

    public void push(State state) {
        stack.push(state);
    }

    public State pop() {
        return stack.pop();
    }

    public State peek() {
        return stack.peek();
    }

    public int size() {
        return stack.size();
    }
}
