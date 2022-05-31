package pt.up.fe.comp.registerAllocation;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import pt.up.fe.comp.jmm.ollir.OllirResult;

public class AllocateRegisters {
    private OllirResult ollirResult;
    private ClassUnit classUnit;
    final private int maxRegisters;

    public AllocateRegisters(OllirResult ollirResult, int maxRegisters) {
        this.ollirResult = ollirResult;
        this.classUnit = ollirResult.getOllirClass();
        this.maxRegisters = maxRegisters;
    }

    /**
     * This method will build the var table for all the methods.
     */
    public void buildVarTable() {
        for (Method method: this.classUnit.getMethods()) {
            this.allocateMethodRegisters(method);
        }
    }

    private boolean allocateMethodRegisters(Method method) {

    }
}
