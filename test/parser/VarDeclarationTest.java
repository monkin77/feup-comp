package parser;

import org.junit.Test;

public class VarDeclarationTest extends ParserTest {
    private static final String VAR_DECLARATION_RULE = "VarDeclarations";

    @Test
    public void singleVarDeclaration() {
        noErrors("int var;");
    }

    @Test
    public void multipleVarDeclarations() {
        noErrors("int[] a; boolean _b; String $c2; MyType d;");
    }

    @Test
    public void missingSemicolon() {
        mustFail("int var");
    }

    @Test
    public void missingVariableName() {
        mustFail("int;");
    }

    @Test
    public void missingType() {
        mustFail("var;");
    }

    @Test
    public void withComments() {
        noErrors("int var; // This is an integer");
        noErrors("int[] a; // Array \n boolean b; // Is it true??");
    }

    @Override
    protected String getStartRule() {
        return VAR_DECLARATION_RULE;
    }
}
