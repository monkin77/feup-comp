package parser;

import org.junit.Test;

public class MethodDeclarationTest extends ParserTest {
    private static final String METHOD_DECLARATION_RULE = "MethodDeclaration";

    @Test
    public void mainDeclaration() {
        noErrors("public static void main(String[] whatever) {a; a = 2;}");
    }

    @Test
    public void noArgsMethod() {
        noErrors("public int method(){return 2;}");
    }

    @Test
    public void singleArgMethod() {
        noErrors("public int _method(String arg){return 2;}");
    }

    @Override
    protected String getStartRule() {
        return METHOD_DECLARATION_RULE;
    }
}
