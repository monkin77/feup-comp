package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;

import javax.swing.text.html.parser.Entity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySymbolTable implements SymbolTable {
    private Map<MySymbol, Map<MySymbol, MySymbol>> map; // Map keys are hashes of symbols

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
        this.map.get(scope).put(symbol, symbol);
        //System.out.print("Inserted new symbol " + symbol.getName() + " in scope. Current Scope:" + this.map.get(scope).toString());
    }

    private MySymbol getClassSymbol() {
        MySymbol globalScope = new MySymbol(new Type(Types.NONE.toString(), false), "global", EntityTypes.GLOBAL);
        Map<MySymbol, MySymbol> currTable = this.map.get(globalScope);

        for (MySymbol scope : currTable.values()) {
            if (scope.getEntity() == EntityTypes.CLASS ) {
                return scope;
            }
        }

        return null;
    }

    @Override
    public List<String> getImports() {
        MySymbol globalScope = new MySymbol(new Type(Types.NONE.toString(), false), "global", EntityTypes.GLOBAL);
        Map<MySymbol, MySymbol> currTable = this.map.get(globalScope);
        List<String> imports = new ArrayList<>();

        for (MySymbol symbol : currTable.values()) {
            if (symbol.getEntity() == EntityTypes.IMPORT ) {
                imports.add(symbol.getName());
            }
        }

        return imports;
    }

    @Override
    public String getClassName() {
        return this.getClassSymbol().getName();
    }

    @Override
    public String getSuper() {
        MySymbol classSymbol = this.getClassSymbol();
        Map<MySymbol, MySymbol> currScope = this.map.get(classSymbol);

        for (MySymbol symbol : currScope.values()) {
            if (symbol.getEntity() == EntityTypes.EXTENDS)
                return symbol.getName();
        }

        return null;
    }

    @Override
    public List<Symbol> getFields() {
        List<Symbol> fields = new ArrayList<>();
        MySymbol classSymbol = this.getClassSymbol();
        Map<MySymbol, MySymbol> currScope = this.map.get(classSymbol);

        for (MySymbol symbol : currScope.values()) {
            // TODO the fields are just variables or also methods??
            if (symbol.getEntity() == EntityTypes.VARIABLE)
                fields.add(symbol);
        }

        return fields;
    }

    @Override
    public List<String> getMethods() {
        return null;
    }

    @Override
    public Type getReturnType(String methodSignature) {
        MySymbol classSymbol = this.getClassSymbol();
        Map<MySymbol, MySymbol> currScope = this.map.get(classSymbol);


        for (MySymbol symbol : currScope.values()) {
            if (symbol.getName().equals(methodSignature) && symbol.getEntity() == EntityTypes.METHOD) {
                return symbol.getType();
            }
        }

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
        for (Map.Entry<MySymbol, Map<MySymbol, MySymbol>> entry : this.map.entrySet()) {
            System.out.println(entry.getKey().getName() + ": " + this.buildMapString(entry.getValue()));
        }
    }

    public String buildMapString(Map<MySymbol, MySymbol> scope) {
        String result = "{";

        for (MySymbol symbol : scope.values()) {
            result = result + symbol.toString() + ", ";
        }
        result = result + "}";
        return result;
    }
}
