package parser;

import org.junit.Test;

public class ExpressionTest extends ParserTest {
    private static final String EXPRESSION_RULE = "Expression";

    @Test
    public void expressionTerminal() {
        noErrors("23"); noErrors("a"); noErrors("abc");
        noErrors("true"); noErrors("false"); noErrors("this");
    }

    @Test
    public void binaryOperations() {
        noErrors("1 + 2"); noErrors("1 - 2");
        noErrors("1 * 2"); noErrors("1 / 2");
        noErrors("1 && 2"); noErrors("1 < 2");
    }

    @Test
    public void chainedExpressions() {
        noErrors("1 + 2 + 3"); noErrors("1 + 2 - 3");
        noErrors("1 + 2 * 3"); noErrors("1 / 2 * 3");
        noErrors("1 + 2 && 3 + 4"); noErrors("1 * 2 < 3 / 4");
        noErrors("!1 && !2"); noErrors("(1 + 3) < (3 * 5)");
        noErrors("!(1 && 2).length"); noErrors("new obj().ola()");
        noErrors("!(new int[5])"); noErrors("this.method()");
    }

    @Test
    public void arrayAccess() {
        noErrors("a[1]"); noErrors("arr[0]");
    }

    @Test
    public void notExpression() {
        noErrors("!hey"); noErrors("!2");
        noErrors("!!hey"); noErrors("!!a[3]");
        noErrors("!!!a.length");
    }

    @Test
    public void dotExpression() {
        noErrors("var.length"); noErrors("obj.func()");
        noErrors("obj.func(arg)"); noErrors("obj.func(a, b)");
        noErrors("obj.func(a[3])");
    }

    @Test
    public void consecutiveBinaryOperations() {
        mustFail(" 1 ++ 2"); mustFail(" 1 -- 2");
        mustFail(" 1 // 2"); mustFail(" 1 ** 2");
        mustFail(" 1 && && 2"); mustFail(" 1 < < 2");
    }

    @Test
    public void newExpression() {
        noErrors("new obj()"); noErrors("new int[5]");
        noErrors("new int[random_expr]");
        noErrors("new int[a[3]]");
    }

    @Test
    public void parenthesisExpression() {
        noErrors("(2)"); noErrors("(new int[5])");
        noErrors("(new obj())"); noErrors("(1 + 2) - (3 * 5)");
    }

    @Override
    protected String getStartRule() {
        return EXPRESSION_RULE;
    }
}
