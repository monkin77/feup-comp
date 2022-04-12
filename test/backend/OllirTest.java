package backend;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import pt.up.fe.comp.MyJasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;

import java.util.Collections;

public class OllirTest {
    @Test
    public void testOllir() {
        String ollirCode = SpecsIo.getResource("fixtures/public/ollir/myclass3.ollir");
        OllirResult ollirResult = new OllirResult(ollirCode, Collections.emptyMap());

        JasminBackend backend = new MyJasminBackend();
        JasminResult jasminResult = backend.toJasmin(ollirResult);

        System.out.println(jasminResult.getJasminCode());
        //assertEquals(jasminResult.getJasminCode(), "    .class public Fac\n    .super java/lang/Object\n");
    }
}
