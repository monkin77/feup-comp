package pt.up.fe.comp.jasmin;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import pt.up.fe.comp.JasminBackendJmm;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;

import java.util.Collections;

public class OllirTest {
    @Test
    public void testOllir() {
        String ollirCode = SpecsIo.getResource("pt/up/fe/comp/fixtures/public/ollir/myclass3.ollir");
        OllirResult ollirResult = new OllirResult(ollirCode, Collections.emptyMap());

        JasminBackend backend = new JasminBackendJmm();
        JasminResult jasminResult = backend.toJasmin(ollirResult);

        System.out.println(jasminResult.getJasminCode());
        //assertEquals(jasminResult.getJasminCode(), "    .class public Fac\n    .super java/lang/Object\n");
    }
}
