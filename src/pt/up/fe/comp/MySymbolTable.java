package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySymbolTable implements SymbolTable {
    private Map<Symbol, Map<String, MySymbol>> map; // Map keys are hashes of symbols

    public MySymbolTable() {
        this.map = new HashMap<>();
    }

    public void openScope(MySymbol symbol) {
        this.map.put(symbol, new HashMap<>());
        System.out.println("----------------------------------------");
        System.out.println("Opened scope " + symbol.getName() + " Current map: ");
        this.myPrint();
        System.out.println("----------------------------------------\n");
    }

    public void put(MySymbol scope, MySymbol symbol) {
        this.map.get(scope).put(symbol.getName(), symbol);
        //System.out.print("Inserted new symbol " + symbol.getName() + " in scope. Current Scope:" + this.map.get(scope).toString());
    }

    @Override
    public List<String> getImports() {
        MySymbol globalScope = new MySymbol(new Type(Types.NONE.toString(), false), "global", EntityTypes.GLOBAL);
        Map<String, MySymbol> currTable = this.map.get(globalScope);
        return null;
    }

    @Override
    public String getClassName() {
        return null;
    }

    @Override
    public String getSuper() {
        return null;
    }

    @Override
    public List<Symbol> getFields() {
        return null;
    }

    @Override
    public List<String> getMethods() {
        return null;
    }

    @Override
    public Type getReturnType(String methodSignature) {
        return null;
    }

    @Override
    public List<Symbol> getParameters(String methodSignature) {
        return null;
    }

    @Override
    public List<Symbol> getLocalVariables(String methodSignature) {
        return null;
    }

    public void myPrint() {
        for (Map.Entry<Symbol, Map<String, MySymbol>> entry : this.map.entrySet()) {
            System.out.println(entry.getKey().getName() + ": " + entry.getValue().toString());
        }
    }
}
