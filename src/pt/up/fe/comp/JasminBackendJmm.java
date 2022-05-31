package pt.up.fe.comp;

import org.specs.comp.ollir.ClassUnit;
import pt.up.fe.comp.jasmin.JasminBuilder;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.util.Collections;

public class JasminBackendJmm implements JasminBackend {

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        final ClassUnit classUnit = ollirResult.getOllirClass();
        final String code = new JasminBuilder(classUnit).compile();
        System.out.println("Jasmin Code:");
        System.out.println(code);
        return new JasminResult(classUnit.getClassName(), code, Collections.emptyList());
    }
}
