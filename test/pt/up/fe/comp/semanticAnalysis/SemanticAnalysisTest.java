package pt.up.fe.comp.semanticAnalysis;

import org.junit.Test;
import pt.up.fe.comp.JmmAnalyser;
import pt.up.fe.comp.SimpleParser;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.specs.util.SpecsIo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SemanticAnalysisTest {
    private JmmSemanticsResult analysisResult;

    private void execute(String file) {
        String input = file;
        String inputResource = SpecsIo.getResource(input);

        // Create config
        Map<String, String> config = new HashMap<>();
        config.put("inputFile", input);
        config.put("optimize", "false");
        config.put("registerAllocation", "-1");
        config.put("debug", "false");

        // Instantiate JmmParser
        SimpleParser parser = new SimpleParser();

        // Parse stage
        JmmParserResult parserResult = parser.parse(inputResource, config);

        // Check if there are parsing errors
        TestUtils.noErrors(parserResult.getReports());

        // Instantiate JmmAnalysis
        JmmAnalyser analyser = new JmmAnalyser();

        // Semantic Analysis stage
        analysisResult = analyser.semanticAnalysis(parserResult);
    }

    /**
     * Iterates a report list and check if has the error
     * @param reports list of reports
     * @param error error message
     * @return true if reports have the error, false otherwise
     */
    private boolean hasError(List<Report> reports, String error) {
        for (Report report : reports) {
            if (report.getMessage().equals(error))
                return true;
        }
        return false;
    }

    @Test
    public void testFindMaxInput() {
        String file = "pt/up/fe/comp/fixtures/public/FindMaximum.jmm";
        execute(file);
        TestUtils.noErrors(analysisResult.getReports());
    }

    @Test
    public void testHelloFile() {
        String file = "pt/up/fe/comp/fixtures/public/HelloWorld.jmm";
        execute(file);
        TestUtils.noErrors(analysisResult.getReports());
    }

    @Test
    public void testLifeFile() {
        String file = "pt/up/fe/comp/fixtures/public/Life.jmm";
        execute(file);
        TestUtils.noErrors(analysisResult.getReports());
    }

    @Test
    public void varDeclError() {
        String file = "pt/up/fe/comp/fixtures/public/semanticAnalysis/VarDeclError.jmm";
        String error = "Invalid attempt to create a variable of non-existing type Ola.";
        execute(file);

        TestUtils.mustFail(analysisResult.getReports());

        assertTrue(hasError(analysisResult.getReports(), error));
    }

    @Test
    public void noFunctionError() {
        String file = "pt/up/fe/comp/fixtures/public/semanticAnalysis/NoFunctionError.jmm";
        String error = "Unknown reference to method build_test_arr when attempting to call fm.build_test_arr().";

        execute(file);

        TestUtils.mustFail(analysisResult.getReports());
        assertTrue(hasError(analysisResult.getReports(), error));
    }

    @Test
    public void DotLengthNoError() {
        String file = "pt/up/fe/comp/fixtures/public/semanticAnalysis/DotLengthNoError.jmm";
        execute(file);
        TestUtils.noErrors(analysisResult.getReports());
    }

    @Test
    public void DotLengthError() {
        String file = "pt/up/fe/comp/fixtures/public/semanticAnalysis/DotLengthError.jmm";
        String error = "Built-in \"length\" is only valid over arrays.";
        execute(file);

        TestUtils.mustFail(analysisResult.getReports());
        assertTrue(hasError(analysisResult.getReports(), error));
    }

    @Test
    public void DotExpressionError() {
        String file = "pt/up/fe/comp/fixtures/public/semanticAnalysis/DotExpressionError.jmm";
        String error1 = "Invalid method call get_array to element of type int.";
        String error2 = "Invalid method call find_maximum to element of type int[].";

        execute(file);

        TestUtils.mustFail(analysisResult.getReports());
        assertTrue(hasError(analysisResult.getReports(), error1));
        assertTrue(hasError(analysisResult.getReports(), error2));
    }

    @Test
    public void DotExpressionNoError() {
        String file = "pt/up/fe/comp/fixtures/public/semanticAnalysis/DotExpressionNoError.jmm";
        execute(file);
        TestUtils.noErrors(analysisResult.getReports());
    }

    @Test
    public void NewObjectNoError() {
        String file = "pt/up/fe/comp/fixtures/public/semanticAnalysis/NewObjectError.jmm";
        String error1 = "Invalid attempt to create a dynamic variable of type UnknownClass.";
        execute(file);

        TestUtils.mustFail(analysisResult.getReports());
        assertTrue(hasError(analysisResult.getReports(), error1));
    }

    @Test
    public void InvalidTypeAssign() {
        String file = "pt/up/fe/comp/fixtures/public/semanticAnalysis/InvalidTypeAssign.jmm";
        String error1 = "Type error. Attempting to assign value of type int to a variable of type boolean.";
        execute(file);

        TestUtils.mustFail(analysisResult.getReports());
        assertTrue(hasError(analysisResult.getReports(), error1));
    }

    @Test
    public void validConditional() {
        String file = "pt/up/fe/comp/fixtures/public/semanticAnalysis/ConditionalNoError.jmm";
        execute(file);

        TestUtils.noErrors(analysisResult.getReports());
    }

    @Test
    public void InvalidConditional() {
        String file = "pt/up/fe/comp/fixtures/public/semanticAnalysis/InvalidConditional.jmm";
        String error1 = "Type error. Condition is not boolean. Type: 'int'.";
        execute(file);

        System.out.println(analysisResult.getReports());
        TestUtils.mustFail(analysisResult.getReports());
        assertTrue(hasError(analysisResult.getReports(), error1));
    }

    @Test
    public void NestedImportNoErrors() {
        String file = "pt/up/fe/comp/fixtures/public/semanticAnalysis/NestedImportNoError.jmm";
        execute(file);
        TestUtils.noErrors(analysisResult.getReports());
    }
}



