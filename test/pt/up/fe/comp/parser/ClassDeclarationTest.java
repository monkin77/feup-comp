package pt.up.fe.comp.parser;

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
        mustFail("class MyClass // {}");
    }

    @Test
    public void withComments() {
        noErrors("class MyClass{} // this is a nice class\n // Agreed!");
    }

    @Override
    protected String getStartRule() {
        return CLASS_DECLARATION_RULE;
    }
}
