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
        addVisit("ImportDecl", this::importDeclVisit);
        addVisit("VarDecl", this::varDeclVisit);
        addVisit("PublicMethod", this::publicMethodVisit);

        addVisit("DotExpression", this::dotExpressionVisit);
        addVisit("_Identifier", this::identifierVisit);
        addVisit("DotMethod", this::dotMethodVisit);
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

    private Integer importDeclVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() >= 1) {
            JmmNode childNode = node.getJmmChild(0);
            String importName = childNode.get("id");
            for (int i = 1; i < node.getNumChildren(); ++i) {
                childNode = node.getJmmChild(i);
                importName = importName + "." + childNode.get("id");
            }

            Symbol importSymbol = new Symbol(new Type(Types.IMPORT.toString(), false), importName);
            this.symbolTable.put(this.scopeStack.peek(), importSymbol);

            return 0;
        }

        throw new RuntimeException("Illegal number of children in node " + "." + node.getKind());
    }

    private Integer varDeclVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() == 1) {
            String varName = node.get("name");

            Types varType = Types.getType(node.getJmmChild(0).getKind());
            boolean isArray = varType.getIsArray();
            String returnType = varType.toString();

            Symbol varSymbol = new Symbol(new Type(returnType, isArray), varName);

            this.symbolTable.put(this.scopeStack.peek(), varSymbol);

            return 0;
        }

        throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
    }

    private Integer publicMethodVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() <= 0) throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");

        Types varType = Types.getType(node.getJmmChild(0).getKind());
        boolean isArray = varType.getIsArray();
        String returnType = varType.toString();

        // We need to store the return type of the function
        // Best way is probably to extend the Symbol class to have another property for the return type in the case of methods
        Symbol methodSymbol = new Symbol(new Type(Types.METHOD.toString(), false), node.get("name"));

        // Insert next scope pointer in previous scope
        this.symbolTable.put(this.scopeStack.peek(), methodSymbol);

        // Add new scope
        this.createScope(methodSymbol);

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

    private Integer dotExpressionVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() == 2) {

            return 0;
        }

        throw new RuntimeException("Illegal number of children in node " + "." + node.getKind());
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
            System.out.println("Analysing the " + node.getKind() + " " + node.get("method"));
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
