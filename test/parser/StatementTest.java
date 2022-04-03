package parser;

import org.junit.Test;

public class StatementTest extends ParserTest {
    private static final String START_RULE = "Statement";

    @Test
    public void assignments() {
        noErrors("a[2] = 3;");
    }

    @Override
    protected void noErrors(String code) {
        noErrors(code, START_RULE);
    }

    @Override
    protected void mustFail(String code) {
        mustFail(code, START_RULE);
    }
}
