package parser;

import org.junit.Test;

public class ClassDeclarationTest extends ParserTest {
    private static final String CLASS_DECLARATION_RULE = "ClassDeclaration";

    @Test
    public void independentClass() {
        noErrors("class MyClass{}");
    }

    @Test
    public void extendedClass() {
        noErrors("class NewClass extends MyClass{}");
    }

    @Test
    public void missingCurlyBraces() {
        mustFail("class MyClass");
        mustFail("class NewClass extends MyClass");
    }

    @Override
    protected String getStartRule() {
        return CLASS_DECLARATION_RULE;
    }
}
