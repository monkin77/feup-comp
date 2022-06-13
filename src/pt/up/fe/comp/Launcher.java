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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class Launcher {

    public static void main(String[] args) {
        SpecsSystem.programStandardInit();

        SpecsLogs.info("Executing with args: " + Arrays.toString(args));

        // read the input code
        if (args.length < 1) {
            throw new RuntimeException("Expected the following evocations:\n" +
                    "comp2022-00 [-r=<num>] [-o] [-d] -i=<input_file.jmm>");
        }
        ArrayList<String> argsList = new ArrayList<>(Arrays.asList(args));

        String fileName = null;
        for (String arg : argsList) {
            if (arg.startsWith("-i=")) {
                fileName = arg.substring(3);
                break;
            }
        }
        if (fileName == null) {
            throw new RuntimeException("Expected the following evocations:\n" +
                    "comp2022-00 [-r=<num>] [-o] [-d] -i=<input_file.jmm>");
        }

        File inputFile = new File(fileName);
        if (!inputFile.isFile()) {
            throw new RuntimeException("Expected a path to an existing input file, got '" + args[0] + "'.");
        }
        String input = SpecsIo.read(inputFile);

        // Create config
        Map<String, String> config = new HashMap<>();
        config.put("inputFile", args[0]);
        config.put("optimize", argsList.contains("-o") ? "true" : "false");

        int maxRegisters = -1;
        int registerIdx = IntStream.range(0, argsList.size())
                .filter(i -> argsList.get(i).startsWith("-r=")).findFirst().orElse(-1);
        if (registerIdx != -1){
            maxRegisters = Integer.parseInt(args[registerIdx].split("=")[1]);
        }
        if (maxRegisters < -1) {
            throw new RuntimeException("The -r=<num> option can't be lower than -1");
        } else {
            config.put("registerAllocation", Integer.toString(maxRegisters));
        }

        config.put("debug", argsList.contains("-d") ? "true" : "false");

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

        if (config.get("optimize").equals("true"))
            analysisResult = jmmOptimization.optimize(analysisResult);

        // Optimization stage
        OllirResult ollirResult = jmmOptimization.toOllir(analysisResult);

        // Check if there are optimization errors
        TestUtils.noErrors(ollirResult.getReports());

        // Register Allocation stage
        ollirResult = jmmOptimization.optimize(ollirResult);

        // Instantiate Compilation stage
        JasminBackend jasminBackend = new JasminBackendJmm();

        // Compilation stage
        JasminResult jasminResult = jasminBackend.toJasmin(ollirResult);

        // Check if there are compilation errors
        TestUtils.noErrors(jasminResult.getReports());

        // We can run the code with something like java -cp /tmp/jasmin_bruno/:libs-jmm/compiled/ FindMaximum
        jasminResult.compile();
        jasminResult.run();
    }

}
