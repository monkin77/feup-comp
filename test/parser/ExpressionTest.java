package parser;

import org.junit.Test;

public class ExpressionTest extends ParserTest {
    private static final String START_RULE = "Expression";

    @Test
    public void simpleArithmeticExpressions() {
        noErrors("1 + 2");
        noErrors("1 - 2");
        noErrors("1 * 2");
        noErrors("1 / 2");
    }

    @Test
    public void arrayAccess() {
        noErrors("a[1]");
        noErrors("arr[0]");
    }

    @Test
    public void consecutivePlusSigns() {
        mustFail(" 1 ++ 2");
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
