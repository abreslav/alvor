package ru.tolmachev.core.grammar_elems;


/**
 * Created by IntelliJ IDEA.
 * User: �������
 * Date: 04.12.11
 * Time: 16:41
 */

public class Terminal extends GrammarElement {


    public static final Terminal EOF = new Terminal(
    		"$end",
//    		IAbstractInputItem.EOF, 
    		-1);
    
//    private final IAbstractInputItem inputItem;

    public Terminal(
//    		IAbstractInputItem token,
    		String text,
    		int bgSymbolCode) {
        super(text, bgSymbolCode);
//        this.inputItem = token;
    }

	public boolean isTerminal() {
        return true;
    }
    
//    public IAbstractInputItem getAbstractInputItem() {
//    	return inputItem;
//    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Terminal)) {
            return false;
        }

        return value.equals(((Terminal) o).getValue());
    }

    public String toString() {
        return value;
    }
}
