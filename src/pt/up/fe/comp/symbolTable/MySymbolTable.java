package pt.up.fe.comp.symbolTable;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.*;

public class MySymbolTable implements SymbolTable {
    private final Map<MySymbol, Map<MySymbol, MySymbol>> map; // Map keys are hashes of symbols
    private final Map<MySymbol, List<MySymbol>> methodArgs;

    public MySymbolTable() {
        this.map = new HashMap<>();
        this.methodArgs = new HashMap<>();
    }

    /**
     *
     * @return true if the open scope operation was successful. Returns false, if the scope was already defined.
     */
    public boolean openScope(MySymbol symbol) {
        if (this.map.get(symbol) != null) return false;

        this.map.put(symbol, new HashMap<>());

        return true;
    }

    /**
     *
     * @return true if the put operation was successful. Returns false, if the variable was already declared in the scope.
     */
    public boolean put(MySymbol scope, MySymbol symbol) {
        Map<MySymbol, MySymbol> currScope = this.map.get(scope);
        if (currScope.get(symbol) != null) return false;

        currScope.put(symbol, symbol);
        return true;
    }

    /**
     *
     * @return true if the put operation was successful. Returns false, if the variable was already declared in the scope.
     */
    public boolean createParamScope(MySymbol scope) {
        if (this.methodArgs.containsKey(scope)) return false;
        List<MySymbol> args = new ArrayList<>();
        this.methodArgs.put(scope, args);
        return true;
    }

    /**
     *  Puts an argument in the list of parameters of the function
     * @return true if the put operation was successful. Returns false, if the variable was already declared in the scope.
     */
    public boolean putArgument(MySymbol scope, MySymbol symbol) {
        List<MySymbol> args = this.methodArgs.get(scope);
        if (args.contains(symbol)) return false;
        args.add(symbol);
        this.methodArgs.put(scope, args);
        return true;
    }

    /**
     * Gets a symbol from all scopes
     * @return Symbol if exists in the scope, null otherwise
     */
    public MySymbol get(MySymbol scope, String symbolName) {
        for (MySymbol symbol : this.map.get(scope).values()) {
            if (symbol.getName().equals(symbolName))
                return symbol;
        }

        return null;
    }

    /**
     * Get a symbol from a given scope that belongs to the list of Entities
     * @return Symbol if found, null otherwise
     */
    public MySymbol get(MySymbol scope, String symbolName, List<EntityTypes> entityTypes) {
        for (MySymbol symbol : this.map.get(scope).values()) {
            if (symbol.getName().equals(symbolName) && entityTypes.contains(symbol.getEntity()))
                return symbol;
        }

        return null;
    }

    public boolean hasInheritance() {
        return this.getSuper() != null;
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

    /**
     * Gets the scope of a method
     * @param methodName name of the method
     * @return scope if exists, empty map otherwise
     */
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
        return Objects.requireNonNull(this.getClassSymbol()).getName();
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

    /**
     * Gets a list with all the arguments of a method
     * @return list with args if it exists, null otherwise
     */
    public List<MySymbol> getMethodArguments(String methodSignature) {
        MySymbol classSymbol = this.getClassSymbol();
        Map<MySymbol, MySymbol> currScope = this.map.get(classSymbol);

        for (MySymbol symbol : currScope.values()) {
            if (symbol.getName().equals(methodSignature) && symbol.getEntity() == EntityTypes.METHOD) {
                return this.methodArgs.get(symbol);
            }
        }

        return null;
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
        StringBuilder result = new StringBuilder("{");

        for (MySymbol symbol : scope.values()) {
            result.append(symbol.toString()).append(", ");
        }
        result.append("}");
        return result.toString();
    }
}
