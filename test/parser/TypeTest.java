package parser;

import org.junit.Test;

public class TypeTest extends ParserTest {
    private final String TYPE_RULE = "Type";

    @Test
    public void intType() {
        noErrors("int");
    }

    @Test
    public void intArrayType() {
        noErrors("int[]");
    }

    @Test
    public void boolType() {
        noErrors("boolean");
    }

    @Test
    public void customType() {
        noErrors("String"); noErrors("MyType");
        noErrors("myType"); noErrors("$mytype");
    }

    @Test
    public void invalidType() {
        mustFail("123"); mustFail("_");
        mustFail("import"); mustFail("void");
    }

    @Override
    protected String getStartRule() {
        return TYPE_RULE;
    }
}
