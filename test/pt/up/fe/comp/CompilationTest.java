package pt.up.fe.comp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.specs.util.SpecsIo;

import java.util.HashMap;
import java.util.Map;

public class CompilationTest {
    @Test
    public void findMaximum() {
        testCompilation("pt/up/fe/comp/fixtures/public/FindMaximum.jmm", """
                3
                3
                4
                5
                6
                7
                8
                9
                10
                69420
                69420
                69420
                69420
                15
                225
                196
                169
                144
                121
                100
                81
                64
                49
                36
                25
                16
                9
                4
                1
                16
                69420
                69420
                8
                250
                """);
    }

    @Test
    public void helloWorld() {
        testCompilation("pt/up/fe/comp/fixtures/public/HelloWorld.jmm", "Hello, World!\n");
    }

    /*@Test
    public void lazySort() {
        testCompilation("pt/up/fe/comp/fixtures/public/Lazysort.jmm", """
                8
                4
                3
                8
                10
                8
                9
                10
                10
                2
                """);
    }*/

    @Test
    public void life() {
        testCompilation("pt/up/fe/comp/fixtures/public/Life.jmm");
    }

    /*@Test
    public void monteCarloPi() {
        testCompilation("pt/up/fe/comp/fixtures/public/MonteCarloPi.jmm");
    }

    @Test
    public void quickSort() {
        testCompilation("pt/up/fe/comp/fixtures/public/QuickSort.jmm");
    }*/

    @Test
    public void simple() {
        testCompilation("pt/up/fe/comp/fixtures/public/Simple.jmm", "30\n");
    }

    @Test
    public void ticTacToe() {
        testCompilation("pt/up/fe/comp/fixtures/public/TicTacToe.jmm");
    }

    @Test
    public void whileAndIf() {
        testCompilation("pt/up/fe/comp/fixtures/public/WhileAndIf.jmm", """
                10
                10
                10
                10
                10
                10
                10
                10
                10
                10
                """);
    }

    private void testCompilation(String file, String executionResult) {
        Map<String, String> config = new HashMap<>();
        config.put("inputFile", file);
        config.put("optimize", "false");
        config.put("debug", "false");

        // Parse stage
        JmmParserResult parserResult = new SimpleParser().parse(SpecsIo.getResource(file), config);

        // Check if there are parsing errors
        TestUtils.noErrors(parserResult.getReports());

        // Semantic Analysis stage
        JmmSemanticsResult semanticsResult = new JmmAnalyser().semanticAnalysis(parserResult);

        // Check if there are semantics errors
        TestUtils.noErrors(semanticsResult.getReports());

        // Ollir compilation stage
        OllirResult ollirResult = new JmmOptimizer().toOllir(semanticsResult);

        // Check if there are errors in ollir stage
        TestUtils.noErrors(ollirResult.getReports());

        // Jasmin compilation stage
        JasminResult jasminResult = new JasminBackendJmm().toJasmin(ollirResult);

        // Check if there are errors in jasmin stage
        TestUtils.noErrors(jasminResult.getReports());

        // Execute compiled program
        String realResult = jasminResult.run();

        if (executionResult != null)
            assertEquals(executionResult, realResult);
    }

    private void testCompilation(String file) {
        testCompilation(file, null);
    }
}
