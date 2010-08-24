package ee.stacc.productivity.edsl.sqlparser;

/**
 * A builder interface to construct parsers from Bison XML format (see the Builder pattern in GoF)
 * 
 * @author abreslav
 *
 */
public interface ILRParserBuilder {

	void addRule(int number, String lhs, int rhsLength, String text);

	void addTerminal(int symbolNumber, int tokenNumber, String name);

	void addNonterminal(int symbolNumber, String name);

	void createState(int number);

	void addGotoAction(int stateNumber, String symbol, int toState);

	void addShiftAction(int stateNumber, String symbol, int toState);

	void addReduceAction(int stateNumber, String symbol, int rule);

	void addAcceptAction(int stateNumber, String symbol);
	
}