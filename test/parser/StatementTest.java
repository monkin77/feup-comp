package parser;

import org.junit.Test;

public class StatementTest extends ParserTest {
    private static final String STATEMENT_RULE = "Statement";

    @Test
    public void assignments() {
        noErrors("a[2] = 3;");
    }

    @Test
    public void nestedIfElses() {
        noErrors("if (cond) {if (a) b; else c;} else {if (a) b; else c;}");
    }

    @Test
    public void nestedWhiles() {
        noErrors("while (cond) {while (a) b;}");
    }

    @Test
    public void ifElseAndWhile() {
        noErrors("if (cond) {while (a) b;} else {while (a) b;}");
        noErrors("while (cond) {if (a) b; else c;}");
    }

    @Test
    public void conditionalAssignment() {
        noErrors("if (cond) a = b; else a = c;");
        noErrors("while (cond) a = next;");
    }

    @Test
    public void missingSemicolon() {
        mustFail("statement");
    }

    @Override
    protected String getStartRule() {
        return STATEMENT_RULE;
    }
}
