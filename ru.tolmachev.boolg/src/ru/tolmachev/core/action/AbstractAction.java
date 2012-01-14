package ru.tolmachev.core.action;

import com.googlecode.alvor.sqlparser.IAction;

/**
 * Created by IntelliJ IDEA.
 * User: �������
 * Date: 20.12.11
 * Time: 14:43
 */

public abstract class AbstractAction implements IAction {

    public boolean consumes() {
        return false;
    }

    public boolean isError() {
        return false;
    }
}