package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Launcher {

    public static void main(String[] args) {
        SpecsSystem.programStandardInit();

        SpecsLogs.info("Executing with args: " + Arrays.toString(args));

        // CHECK THE REMAINING WORK TO BE DONE IN THE PROJECT SPECIFICATION
        String numRegisters = "-2";
        // read the input code
        if (args.length < 1) {
            throw new RuntimeException("Expected a single argument, a path to an existing input file.");
        } else {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-r")) {
                    int numRegIdx = i+1;
                    if (numRegIdx < args.length) {
                        numRegisters = args[numRegIdx];
                        if (numRegisters.equals("-1"))
                            numRegisters = "65535"; // TODO: CHECK IF IT SHOULD BE DONE THIS WAY
                    } else {
                        throw new RuntimeException("Missing argument for the number of register allocated.");
                    }
                }
            }
        }

        // Create config
        Map<String, String> config = new HashMap<>();
        config.put("inputFile", args[0]);
        config.put("optimize", "false");
        config.put("registerAllocation", numRegisters);
        config.put("debug", "false");

        File inputFile = new File(args[0]);
        if (!inputFile.isFile()) {
            throw new RuntimeException("Expected a path to an existing input file, got '" + args[0] + "'.");
        }
        String input = SpecsIo.read(inputFile);

        // Instantiate JmmParser
        SimpleParser parser = new SimpleParser();

        // Parse stage
        JmmParserResult parserResult = parser.parse(input, config);

        // Check if there are parsing errors
        TestUtils.noErrors(parserResult.getReports());

        // Instantiate JmmAnalysis
        JmmAnalyser analyser = new JmmAnalyser();

        // Semantic Analysis stage
        JmmSemanticsResult analysisResult = analyser.semanticAnalysis(parserResult);

        // Check if there are analysis errors
        TestUtils.noErrors(analysisResult.getReports());

        System.out.println("AST:");
        System.out.println(analysisResult.getRootNode().toTree());

        // Instantiate Optimization stage
        JmmOptimization jmmOptimization = new JmmOptimizer();

        // Create Ollir code
        OllirResult ollirResult = jmmOptimization.toOllir(analysisResult);

        // Check if there are optimization errors
        TestUtils.noErrors(ollirResult.getReports());

        // Optimization stage
        OllirResult optimizedOllirResult = jmmOptimization.optimize(ollirResult);

        // Check if there are optimization errors
        TestUtils.noErrors(optimizedOllirResult.getReports());

        // Instantiate Compilation stage
        JasminBackend jasminBackend = new JasminBackendJmm();

        // Compilation stage
        JasminResult jasminResult = jasminBackend.toJasmin(optimizedOllirResult);

        // Check if there are compilation errors
        TestUtils.noErrors(jasminResult.getReports());

        // We can run the code with something like java -cp /tmp/jasmin_bruno/:libs-jmm/compiled/ FindMaximum
        jasminResult.compile();

        System.out.println("JASMIN:\n" + jasminResult.getJasminCode());
        System.out.println("JASMIN RUN:\n" + jasminResult.run());
    }

}
