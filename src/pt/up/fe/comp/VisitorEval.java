package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;

import java.util.Stack;

public class VisitorEval extends AJmmVisitor<Object, Integer> {
    private final MySymbolTable symbolTable;
    private final Stack<MySymbol> scopeStack;

    public VisitorEval(MySymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.scopeStack = new Stack<>();

        MySymbol globalScope = new MySymbol(new Type(Types.NONE.toString(), false), "global", EntityTypes.GLOBAL);
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
        addVisit("Argument", this::argumentVisit);
        addVisit("AssignmentExpr", this::assignExprVisit);
        addVisit("WhileSt", this::whileStVisit);
        addVisit("IntegerLiteral", this::integerVisit);

        addVisit("DotExpression", this::dotExpressionVisit);
        addVisit("_Identifier", this::identifierVisit);
        addVisit("DotMethod", this::dotMethodVisit);
        addVisit("IntArray", this::intArrayVisit);

        setDefaultVisit(this::defaultVisit);
    }

    private void createScope(MySymbol symbol) {
        this.scopeStack.push(symbol);
        this.symbolTable.openScope(symbol);
    }

    private Integer classDeclVisit(JmmNode node, Object dummy) {
        MySymbol classSymbol = new MySymbol(new Type(Types.NONE.toString(), false), node.get("name"), EntityTypes.CLASS);

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
        MySymbol mainSymbol = new MySymbol(new Type(Types.VOID.toString(), false), "main", EntityTypes.METHOD);

        // Insert next scope pointer in previous scope
        this.symbolTable.put(this.scopeStack.peek(), mainSymbol);

        // Add new scope
        this.createScope(mainSymbol);

        String argName = node.get("mainArgs");
        MySymbol argSymbol = new MySymbol(new Type(Types.STRING.toString(), true), argName, EntityTypes.ARG);
        this.symbolTable.put(this.scopeStack.peek(), argSymbol);

        Integer visitResult = 0;
        for (int i = 0; i < node.getNumChildren(); ++i) {
            JmmNode childNode = node.getJmmChild(i);
            visitResult = visit(childNode);
            System.out.println("Visited main Decl child: " + i + " with result " + visitResult);
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

            MySymbol importSymbol = new MySymbol(new Type(Types.NONE.toString(), false), importName, EntityTypes.IMPORT);
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

            MySymbol varSymbol = new MySymbol(new Type(varType.toString(), isArray), varName, EntityTypes.VARIABLE);

            this.symbolTable.put(this.scopeStack.peek(), varSymbol);

            return 0;
        }

        throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
    }

    private Integer publicMethodVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() <= 0)
            throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");

        // Get the return type of the method
        Types varType = Types.getType(node.getJmmChild(0).getKind());
        boolean isArray = varType.getIsArray();

        MySymbol methodSymbol = new MySymbol(new Type(varType.toString(), isArray), node.get("name"), EntityTypes.METHOD);

        // Insert next scope pointer in previous scope
        this.symbolTable.put(this.scopeStack.peek(), methodSymbol);

        // Add new scope
        this.createScope(methodSymbol);

        Integer visitResult = 0;
        for (int i = 1; i < node.getNumChildren(); ++i) {
            JmmNode childNode = node.getJmmChild(i);
            visitResult = visit(childNode);
            System.out.println("Visited method Decl child: " + i + " with result " + visitResult);
        }

        // Pop current scope
        this.scopeStack.pop();

        return visitResult;
    }

    private Integer whileStVisit(JmmNode node, Object dummy) {
        // Children: Expr and While Block
        if (node.getNumChildren() < 2) {
            throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
        }

        //TODO Should we be checking anything here? I think we can't declare variables
        // inside while loops; We can iterate the children and visit them in order to
        // accomplish this
        return 0;
    }

    private Integer argumentVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() == 1) {
            Types varType = Types.getType(node.getJmmChild(0).getKind());
            boolean isArray = varType.getIsArray();

            String argName = node.get("arg");
            MySymbol argSymbol = new MySymbol(new Type(varType.toString(), isArray), argName, EntityTypes.ARG);
            this.symbolTable.put(this.scopeStack.peek(), argSymbol);
            return 0;
        }

        throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
    }

    private Integer assignExprVisit(JmmNode node, Object dummy) {
        // TODO: Confirm we don't need to store anything
        if (node.getNumChildren() == 2) {
            System.out.println("Assign Expr with " + node.getNumChildren() + " children");
            return 0;
        }

        throw new RuntimeException("Illegal number of children in node " + "." + node.getKind());
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

    private Integer integerVisit(JmmNode node, Object dummy) {
        if (node.getNumChildren() == 0) {
            return Integer.parseInt(node.get("value"));
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

        System.out.println("Currently in node: " + node.getKind());
        Integer visitResult = 0;
        for (int i = 0; i < node.getNumChildren(); ++i) {
            JmmNode childNode = node.getJmmChild(i);
            visitResult = visit(childNode);
            //System.out.println("Intermediate result: " + visitResult);
        }

        return visitResult;
    }
}
