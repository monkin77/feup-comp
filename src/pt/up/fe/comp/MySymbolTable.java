package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;

import javax.swing.text.html.parser.Entity;
import java.util.*;

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

    /**
     *
     * @return Symbol of the Class
     */
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

    private Map<MySymbol, MySymbol> getMethodScope(String methodName) {
        MySymbol classSymbol = this.getClassSymbol();
        Map<MySymbol, MySymbol> currScope = this.map.get(classSymbol);


        for (MySymbol symbol : currScope.values()) {
            if (symbol.getName().equals(methodName) && symbol.getEntity() == EntityTypes.METHOD) {
                return this.map.get(symbol);
            }
        }

        return Collections.emptyMap();
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
        List<String> methods = new ArrayList<>();
        MySymbol classSymbol = this.getClassSymbol();
        Map<MySymbol, MySymbol> currScope = this.map.get(classSymbol);

        for (MySymbol symbol : currScope.values()) {
            if (symbol.getEntity() == EntityTypes.METHOD)
                methods.add(symbol.getName());
        }

        return methods;
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
        List<Symbol> params = new ArrayList<>();
        Map<MySymbol, MySymbol> methodScope = this.getMethodScope(methodSignature);

        for (MySymbol symbol : methodScope.values()) {
            if (symbol.getEntity() == EntityTypes.ARG) {
                params.add(symbol);
            }
        }

        return params;
    }

    @Override
    public List<Symbol> getLocalVariables(String methodSignature) {
        List<Symbol> localVars = new ArrayList<>();
        Map<MySymbol, MySymbol> methodScope = this.getMethodScope(methodSignature);

        for (MySymbol symbol : methodScope.values()) {
            if (symbol.getEntity() == EntityTypes.VARIABLE) {
                localVars.add(symbol);
            }
        }

        return localVars;
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
