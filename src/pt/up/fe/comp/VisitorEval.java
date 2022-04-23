package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;

import java.util.Stack;

public class VisitorEval extends AJmmVisitor<Object, Integer> {
    private final MySymbolTable symbolTable;
    private final Stack<Symbol> scopeStack;

    public VisitorEval(MySymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.scopeStack = new Stack<Symbol>();

        Symbol globalScope = new Symbol(new Type(Types.GLOBAL.toString(), false), "global");
        this.scopeStack.push(globalScope);
        this.symbolTable.openScope(globalScope);

        /* addVisit("IntegerLiteral", this::integerVisit);
        addVisit("UnaryOp", this::unaryOpVisit);
        addVisit("BinOp", this::binOpVisit);
        addVisit("SymbolReference", this::symbolReference);
        addVisit("Assign", this::assignVisit); */
        addVisit("ClassDecl", this::classDeclVisit);
        addVisit("MainDecl", this::mainDeclVisit);
        addVisit("_Identifier", this::identifierVisit);
        addVisit("DotMethod", this::dotMethodVisit);
        addVisit("VarDecl", this::varDeclVisit);
        addVisit("IntArray", this::intArrayVisit);

        setDefaultVisit(this::defaultVisit);
    }

    private void createScope(Symbol symbol) {
        this.scopeStack.push(symbol);
        this.symbolTable.openScope(symbol);
    }

    private Integer classDeclVisit(JmmNode node, Object dummy) {
        Symbol classSymbol = new Symbol(new Type(Types.CLASS.toString(), false), node.get("name"));

        // Insert next scope pointer in previous scope
        this.symbolTable.put(this.scopeStack.peek(), classSymbol);

        // Add new scope
        this.createScope(classSymbol);

        Integer visitResult = 0;
        for (int i = 0; i < node.getNumChildren(); ++i) {
            JmmNode childNode = node.getJmmChild(i);
            visitResult = visit(childNode);
            System.out.println("Visited class Decl child: " + i + " with result " + visitResult);
        }

        this.scopeStack.pop();

        return visitResult;
    }

    private Integer mainDeclVisit(JmmNode node, Object dummy) {
        Symbol mainSymbol = new Symbol(new Type(Types.METHOD.toString(), false), "main");

        // Insert next scope pointer in previous scope
        this.symbolTable.put(this.scopeStack.peek(), mainSymbol);

        // Add new scope
        this.createScope(mainSymbol);

        String argName = node.get("mainArgs");
        Symbol argSymbol = new Symbol(new Type(Types.STRING.toString(), true), argName);
        this.symbolTable.put(this.scopeStack.peek(), argSymbol);

        Integer visitResult = 0;
        for (int i = 0; i < node.getNumChildren(); ++i) {
            JmmNode childNode = node.getJmmChild(i);
            visitResult = visit(childNode);
            System.out.println("Visited class Decl child: " + i + " with result " + visitResult);
        }

        this.scopeStack.pop();

        return visitResult;
    }

    private Integer identifierVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() == 0) {
            System.out.println("Analysing the " + node.getKind() + " " + node.get("id"));
            return 0;
        }

        throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
    }

    private Integer dotMethodVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() == 0) {   // Could have children?
            // Check if method exists?
            System.out.println("Analysing the " + node.getKind() + " " + node.get("id"));
            return 0;
        }

        throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
    }

    private Integer varDeclVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() == 1) {
            String varName = node.get("name");
            System.out.println("Analysing the " + node.getKind() + " " + varName);

            String varType = visit(node.getJmmChild(0)) == 1 ? "Int" : "String";    // Create an enum mapping the integer values to each type?
            Pair<String, String> pair = new Pair(varType, null);
            //this.map.put(varName, pair);    // Store new variable in the map


            return 0;
        }

        throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
    }

    private Integer intArrayVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() == 0) {
            System.out.println("Analysing the " + node.getKind());
            return 2;   // Return 2 if IntArray; Return 1 if Int, and so on... ?
        }

        throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
    }

    private Integer assignVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() == 0) {
            return 0;
        }

        throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
    }

    private Integer integerVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() == 0) {

            return Integer.parseInt(node.get("image"));
        }

        throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
    }

    private Integer symbolReference(JmmNode node, Object dummy) {
        if (node.getNumChildren() == 0) {
            // Integer value = this.map.get(String.valueOf(node.get("image")));
            //if (value == null) throw new RuntimeException("Error. Tried to dereference variable" + node.get("image") + "before assignment!");
            // return value;
        }

        throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
    }

    private Integer unaryOpVisit(JmmNode node, Object dummy) {

        String opString = node.get("op");
        if (opString != null) {

            if (node.getNumChildren() != 1) {
                throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
            }

            CalculatorOps op = CalculatorOps.fromName(opString);
            switch (op) {
                case NEG:
                    return -1 * visit(node.getJmmChild(0));

                default:
                    throw new RuntimeException("Illegal operation '" + op + "' in " + node.getKind() + ".");
            }
        }

        if (node.getNumChildren() != 1) {
            throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
        }

        return visit(node.getJmmChild(0));
    }

    private Integer binOpVisit(JmmNode node, Object dummy) {

        String opString = node.get("op");
        if (opString != null) {

            if (node.getNumChildren() != 2) {
                throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
            }

            CalculatorOps op = CalculatorOps.fromName(opString);
            switch (op) {
                case MUL:
                    return visit(node.getJmmChild(0)) * visit(node.getJmmChild(1));

                case DIV:
                    return visit(node.getJmmChild(0)) / visit(node.getJmmChild(1));

                case ADD:
                    return visit(node.getJmmChild(0)) + visit(node.getJmmChild(1));

                case SUB:
                    return visit(node.getJmmChild(0)) - visit(node.getJmmChild(1));

                case EQ:
                    String key = String.valueOf(node.getJmmChild(0).get("image"));
                    Integer value = visit(node.getJmmChild(1));
                    // this.map.put(key, value);
                    return value;

                default:
                    throw new RuntimeException("Illegal operation '" + op + "' in " + node.getKind() + ".");
            }
        }

        if (node.getNumChildren() != 1) {
            throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
        }

        return visit(node.getJmmChild(0));
    }

    private Integer defaultVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() <= 0) {
            throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
        }

        Integer visitResult = 0;
        for (int i = 0; i < node.getNumChildren(); ++i) {
            JmmNode childNode = node.getJmmChild(i);
            visitResult = visit(childNode);
            System.out.println("Intermediate result: " + visitResult);
        }

        return visitResult;
    }
}
