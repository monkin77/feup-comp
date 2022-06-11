package pt.up.fe.comp.optimization;

import org.junit.Test;
import pt.up.fe.comp.CpUtils;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.specs.util.SpecsIo;

import java.util.HashMap;
import java.util.Map;

public class IfNegationTest {
    private JasminResult getJasminResult(String filename) {
        /*Map<String, String> config = new HashMap<>();
        config.put("optimize", "true");*/
        return TestUtils.backend(SpecsIo.getResource("pt/up/fe/comp/fixtures/public/" + filename));
    }

    @Test
    public void testEmptyElseBlock() {
        String filename = "EmptyElseBlock.jmm";
        int expectedIf = 1;
        int expectedGoto = 0;

        JasminResult result = getJasminResult(filename);

        var ifOccurOpt = CpUtils.countOccurences(result, "if_icmp");
        var gotoOccurOpt = CpUtils.countOccurences(result, "goto");

        CpUtils.assertEquals("Expected exactly " + expectedIf + " if instruction", expectedIf, ifOccurOpt, result);
        CpUtils.assertEquals("Expected exactly " + expectedGoto + " goto instructions", expectedGoto, gotoOccurOpt, result);
    }

    @Test
    public void nestedConditions() {
        String filename = "Conditionals.jmm";
        int expectedIfGe = 3;
        int expectedIfLt = 0;
        int expectedGoto = 3;

        JasminResult jasminResult = getJasminResult(filename);

        var ifGeOccurOpt = CpUtils.countOccurences(jasminResult, "if_icmpge");
        var ifLtOccurOpt = CpUtils.countOccurences(jasminResult, "if_icmplt");
        var gotoOccurOpt = CpUtils.countOccurences(jasminResult, "goto");

        CpUtils.assertEquals("Expected exactly " + expectedIfGe + " if instruction", expectedIfGe, ifGeOccurOpt, jasminResult);
        CpUtils.assertEquals("Expected exactly " + expectedIfLt + " if instruction", expectedIfLt, ifLtOccurOpt, jasminResult);
        CpUtils.assertEquals("Expected exactly " + expectedGoto + " goto instructions", expectedGoto, gotoOccurOpt, jasminResult);

        String result = jasminResult.run();
        CpUtils.assertEquals("Expected conditions 2 and 4 to to execute", "2\n4\n", result, jasminResult);
    }

    @Test
    public void simpleIfStructure() {
        String filename = "SimpleIf.jmm";
        JasminResult jasminResult = getJasminResult(filename);
        String expectedCode = """
                if_icmpge ifbody_0
                    iconst_1
                invokestatic io/println(I)V
                    goto endif_0
                    ifbody_0:
                    iconst_2
                invokestatic io/println(I)V
                    endif_0:
                """;
        int occurences = CpUtils.countOccurences(jasminResult, expectedCode);
        CpUtils.assertEquals("Expected the code to include:\n" + expectedCode, 1, occurences, jasminResult);
    }
}
