package Parser;

import org.junit.Test;
import pt.up.fe.specs.util.SpecsIo;

public class GlobalTest extends ParserTest {
    /*
     * Code that must be successfully parsed
     */

    @Test
    public void helloWorld() {
        noErrors(SpecsIo.getResource("fixtures/public/HelloWorld.jmm"));
    }

    @Test
    public void findMaximum() {
        noErrors(SpecsIo.getResource("fixtures/public/FindMaximum.jmm"));
    }

    @Test
    public void lazysort() {
        noErrors(SpecsIo.getResource("fixtures/public/Lazysort.jmm"));
    }

    @Test
    public void life() {
        noErrors(SpecsIo.getResource("fixtures/public/Life.jmm"));
    }

    @Test
    public void quickSort() {
        noErrors(SpecsIo.getResource("fixtures/public/QuickSort.jmm"));
    }

    @Test
    public void simple() {
        noErrors(SpecsIo.getResource("fixtures/public/Simple.jmm"));
    }

    @Test
    public void ticTacToe() {
        noErrors(SpecsIo.getResource("fixtures/public/TicTacToe.jmm"));
    }

    @Test
    public void whileAndIf() {
        noErrors(SpecsIo.getResource("fixtures/public/WhileAndIf.jmm"));
    }

    /*
     * Code with errors
     */

    @Test
    public void blowUp() {
        mustFail(SpecsIo.getResource("fixtures/public/fail/syntactical/BlowUp.jmm"));
    }

    @Test
    public void completeWhileTest() {
        mustFail(SpecsIo.getResource("fixtures/public/fail/syntactical/CompleteWhileTest.jmm"));
    }

    @Test
    public void lengthError() {
        mustFail(SpecsIo.getResource("fixtures/public/fail/syntactical/LengthError.jmm"));
    }

    @Test
    public void missingRightPar() {
        mustFail(SpecsIo.getResource("fixtures/public/fail/syntactical/MissingRightPar.jmm"));
    }

    @Test
    public void multipleSequential() {
        mustFail(SpecsIo.getResource("fixtures/public/fail/syntactical/MultipleSequential.jmm"));
    }

    @Test
    public void nestedLoop() {
        mustFail(SpecsIo.getResource("fixtures/public/fail/syntactical/NestedLoop.jmm"));
    }
}
