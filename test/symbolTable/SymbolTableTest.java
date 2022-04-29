package symbolTable;

import org.junit.Test;
import pt.up.fe.comp.JmmAnalyser;
import pt.up.fe.comp.SimpleParser;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsStrings;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class SymbolTableTest {

    @Test
    public void testFileInput() {
        String input = "./fixtures/public/FindMaximum.jmm";
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
        JmmSemanticsResult analysisResult = analyser.semanticAnalysis(parserResult);
    }
}
