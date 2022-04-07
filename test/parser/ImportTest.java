package parser;

import org.junit.Test;

public class ImportTest extends ParserTest {
    private static final String IMPORT_RULE = "ImportRegion";

    @Test
    public void multipleSimpleImports() {
        noErrors("import a; import b; import c;");
    }

    @Test
    public void multipleChainedImports() {
        noErrors("import a.b; import c; import d.e.f.g;");
    }

    @Test
    public void missingSemicolon() {
        mustFail("import a");
        mustFail("import a import b;");
    }

    @Override
    protected String getStartRule() {
        return IMPORT_RULE;
    }
}
