package pt.up.fe.comp.parser;

import org.junit.Test;
import pt.up.fe.specs.util.SpecsIo;

public class GlobalTest extends ParserTest {
    /*
     * Code that must be successfully parsed
     */

    @Test
    public void helloWorld() {
        noErrors(SpecsIo.getResource("pt/up/fe/comp/fixtures/public/HelloWorld.jmm"));
    }

    @Test
    public void findMaximum() {
        noErrors(SpecsIo.getResource("pt/up/fe/comp/fixtures/public/FindMaximum.jmm"));
    }

    @Test
    public void lazysort() {
        noErrors(SpecsIo.getResource("pt/up/fe/comp/fixtures/public/Lazysort.jmm"));
    }

    @Test
    public void life() {
        noErrors(SpecsIo.getResource("pt/up/fe/comp/fixtures/public/Life.jmm"));
    }

    @Test
    public void quickSort() {
        noErrors(SpecsIo.getResource("pt/up/fe/comp/fixtures/public/QuickSort.jmm"));
    }

    @Test
    public void simple() {
        noErrors(SpecsIo.getResource("pt/up/fe/comp/fixtures/public/Simple.jmm"));
    }

    @Test
    public void ticTacToe() {
        noErrors(SpecsIo.getResource("pt/up/fe/comp/fixtures/public/TicTacToe.jmm"));
    }

    @Test
    public void whileAndIf() {
        noErrors(SpecsIo.getResource("pt/up/fe/comp/fixtures/public/WhileAndIf.jmm"));
    }

    /*
     * Code with errors
     */

    @Test
    public void blowUp() {
        mustFail(SpecsIo.getResource("pt/up/fe/comp/fixtures/public/fail/syntactical/BlowUp.jmm"));
    }

    @Test
    public void completeWhileTest() {
        mustFail(SpecsIo.getResource("pt/up/fe/comp/fixtures/public/fail/syntactical/CompleteWhileTest.jmm"));
    }

    @Test
    public void lengthError() {
        mustFail(SpecsIo.getResource("pt/up/fe/comp/fixtures/public/fail/syntactical/LengthError.jmm"));
    }

    @Test
    public void missingRightPar() {
        mustFail(SpecsIo.getResource("pt/up/fe/comp/fixtures/public/fail/syntactical/MissingRightPar.jmm"));
    }

    @Test
    public void multipleSequential() {
        mustFail(SpecsIo.getResource("pt/up/fe/comp/fixtures/public/fail/syntactical/MultipleSequential.jmm"));
    }

    @Test
    public void nestedLoop() {
        mustFail(SpecsIo.getResource("pt/up/fe/comp/fixtures/public/fail/syntactical/NestedLoop.jmm"));
    }
}
