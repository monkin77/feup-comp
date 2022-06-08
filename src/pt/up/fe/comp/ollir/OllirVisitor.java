package pt.up.fe.comp.ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.visitors.Utils;

import java.util.List;

import static pt.up.fe.comp.ollir.OllirUtils.getSymbol;

public class OllirVisitor extends AJmmVisitor<ArgumentPool, VisitResult> {
    private final SymbolTable symbolTable;
    private int tempCounter;
    private String currentMethod;

    public OllirVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        tempCounter = 0;

        addVisit("Start", this::startVisit);
        addVisit("MainDecl", this::mainDeclVisit);
        addVisit("PublicMethod", this::publicMethodVisit);
        addVisit("AssignmentExpr", this::assignExprVisit);
        addVisit("DotExpression", this::dotExpressionVisit);
        addVisit("DotMethod", this::dotMethodVisit);
        addVisit("DotLength", this::dotLengthVisit);

        addVisit("IntegerLiteral", this::integerLiteralVisit);
        addVisit("BooleanLiteral", this::booleanLiteralVisit);
        addVisit("_Identifier", this::identifierVisit);

        addVisit("AddExpr", this::addExprVisit);
        addVisit("SubExpr", this::subExprVisit);
        addVisit("MultExpr", this::mulExprVisit);
        addVisit("DivExpr", this::divExprVisit);
        addVisit("LessExpr", this::lessExprVisit);
        addVisit("LessEqualExpr", this::lessEqualExprVisit);
        addVisit("GreaterExpr", this::greaterExprVisit);
        addVisit("GreaterEqualExpr", this::greaterEqualExprVisit);
        addVisit("EqualExpr", this::equalExprVisit);
        addVisit("NotEqualExpr", this::notEqualExprVisit);


        addVisit("AndExpr", this::andExprVisit);
        addVisit("OrExpr", this::orExprVisit);
        addVisit("NotExpr", this::notExprVisit);
        addVisit("ImportRegion", this::ignore);
        addVisit("VarDecl", this::ignore);
        addVisit("_This", this::thisVisit);
        addVisit("ArrayExpr", this::arrayExprVisit);
        addVisit("IfElse", this::conditionalVisit);
        addVisit("WhileSt", this::whileVisit);
        addVisit("NewArrayExpr", this::newArrayExprVisit);
        addVisit("NewObjExpr", this::newObjExprVisit);
        addVisit("ReturnExpr", this::returnExprVisit);
        addVisit("BooleanCondition", this::booleanConditionVisit);
        setDefaultVisit(this::defaultVisit);
    }

    private VisitResult notEqualExprVisit(JmmNode node, ArgumentPool argumentPool) {
        return binOpVisit(node, "!=", "bool");
    }

    private VisitResult equalExprVisit(JmmNode node, ArgumentPool argumentPool) {
        return binOpVisit(node, "==", "bool");
    }

    private VisitResult greaterEqualExprVisit(JmmNode node, ArgumentPool argumentPool) {
        return binOpVisit(node, ">=", "bool");
    }

    private VisitResult greaterExprVisit(JmmNode node, ArgumentPool argumentPool) {
        return binOpVisit(node, ">", "bool");
    }

    private VisitResult lessEqualExprVisit(JmmNode node, ArgumentPool argumentPool) {
        return binOpVisit(node, "<=", "bool");
    }

    private VisitResult orExprVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        return binOpVisit(jmmNode, "||", "bool");
    }

    private VisitResult booleanConditionVisit(JmmNode node, ArgumentPool argumentPool) {
        String kind = node.getJmmChild(0).getKind();
        boolean isNotTerminalNode = !(kind.equals("ArrayExpr") || kind.equals("NotExpr") || kind.equals("LessExpr") || kind.equals("LessEqualExpr") || kind.equals("GreaterExpr") || kind.equals("GreaterEqualExpr") || kind.equals("EqualExpr") || kind.equals("NotEqualExpr")
                                      || kind.equals("AndExpr") || kind.equals("OrExpr") || kind.equals("_Identifier") || kind.equals("BooleanLiteral"));

        // boolean isNotTerminalNode = OllirUtils.isNotTerminalNode(node.getJmmChild(0));
        final VisitResult result = visit(node.getJmmChild(0), new ArgumentPool(null, isNotTerminalNode));

        boolean needsBooleanType = isNotTerminalNode || kind.equals("BooleanLiteral") || kind.equals("_Identifier")
                                   || kind.equals("ArrayExpr");
        final String code = result.code + (needsBooleanType ? ".bool" : "");
        return new VisitResult(result.preparationCode, code, result.finalCode, "");
    }

    private VisitResult newObjExprVisit(JmmNode node, ArgumentPool argumentPool) {
        final String className = node.get("object");
        String code = "new(%s).%s".formatted(className, className);
        String finalCode = "invokespecial(%s.%s,\"<init>\").V;\n".formatted(argumentPool.getId(), className);
        return new VisitResult("", code, finalCode, className);
    }

    private VisitResult returnExprVisit(JmmNode node, ArgumentPool argumentPool) {
        final JmmNode expr = node.getJmmChild(0);
        final VisitResult exprResult = visit(expr, new ArgumentPool(null, OllirUtils.isNotTerminalNode(expr)));
        final String exprId = exprResult.code;
        final String returnType = OllirUtils.convertType(symbolTable.getReturnType(currentMethod));
        final String code = "ret.%s %s.%s;".formatted(returnType, exprId, exprResult.returnType);
        return new VisitResult(exprResult.preparationCode, code, "", returnType);
    }

    private VisitResult newArrayExprVisit(JmmNode node, ArgumentPool argumentPool) {
        final JmmNode size = node.getJmmChild(0);
        final VisitResult sizeResult = visit(size, new ArgumentPool(null, true));
        // COMBACK: Even though we only have int[], this feels weird.
        final String arrayElementType = "i32";
        final String returnType = "array" + "." + arrayElementType;
        final String code = "new(array, %s.%s).%s".formatted(sizeResult.code, sizeResult.returnType, returnType);
        return new VisitResult(sizeResult.preparationCode, code, "", returnType);
    }

    private VisitResult whileVisit(JmmNode node, ArgumentPool argumentPool) {
        final VisitResult conditionResult = visit(node.getJmmChild(0));
        final VisitResult loopResult = visit(node.getJmmChild(1));

        // COMBACK: Optimization: condition should probably be negated beforehand to avoid jumps
        final String bodyCode = loopResult.preparationCode + loopResult.code + conditionResult.preparationCode;
        int tempCounter1 = tempCounter++;
        final String code = ("""
                %sif (%s) goto whilebody_%d;
                goto endwhile_%d;
                whilebody_%d:
                %sif (%s) goto whilebody_%d;
                endwhile_%d:
                """).formatted(conditionResult.preparationCode, conditionResult.code, tempCounter1, tempCounter1, tempCounter1, bodyCode, conditionResult.code, tempCounter1, tempCounter1);
        return new VisitResult("", code, "");
    }

    private VisitResult conditionalVisit(JmmNode node, ArgumentPool argumentPool) {
        final VisitResult conditionResult = visit(node.getJmmChild(0));
        final VisitResult ifBodyResult = visit(node.getJmmChild(1));
        final VisitResult elseBodyResult = visit(node.getJmmChild(2));
        // COMBACK: Optimization: ondition should probably be negated beforehand
        final String elseCode = elseBodyResult.preparationCode + elseBodyResult.code;
        final String ifCode = ifBodyResult.preparationCode + ifBodyResult.code;
        int tempCounter1 = tempCounter++;
        final String code = """
                %s
                if (%s) goto ifbody_%d;
                    %sgoto endif_%d;
                ifbody_%d:
                    %sendif_%d:
                    """.formatted(conditionResult.preparationCode, conditionResult.code, tempCounter1, elseCode, tempCounter1, tempCounter1, ifCode, tempCounter1);
        return new VisitResult("", code, "");
    }

    private VisitResult arrayExprVisit(JmmNode node, ArgumentPool argumentPool) {
        final ArgumentPool leftArgumentPool = new ArgumentPool(null, OllirUtils.isNotTerminalNode(node.getJmmChild(0)));
        final ArgumentPool rightArgumentPool = new ArgumentPool(null, true);
        final VisitResult lhsResult = visit(node.getJmmChild(0), leftArgumentPool);
        final VisitResult rhsResult = visit(node.getJmmChild(1), rightArgumentPool);
        final String arrayElementType = lhsResult.returnType.split("\\.", 2)[1];
        final String preparationCode = rhsResult.preparationCode + lhsResult.preparationCode;
        final String code = "%s[%s.%s]".formatted(lhsResult.code, rhsResult.code, rhsResult.returnType);
        return new VisitResult(preparationCode, code, "", arrayElementType);
    }

    private VisitResult dotExpressionVisit(JmmNode node, ArgumentPool argumentPool) {
        final ArgumentPool leftArg = new ArgumentPool(null, OllirUtils.isNotTerminalNode(node.getJmmChild(0)));
        final VisitResult lhsResult = visit(node.getJmmChild(0), leftArg);

        final ArgumentPool rightArg = new ArgumentPool(lhsResult.code);
        rightArg.setExpectedReturnType(argumentPool.getExpectedReturnType());
        final VisitResult rhsResult = visit(node.getJmmChild(1), rightArg);

        final String type = argumentPool.getType() == null ? rhsResult.returnType : argumentPool.getType();
        final String preparationCode = rhsResult.preparationCode + lhsResult.preparationCode + lhsResult.finalCode + rhsResult.finalCode;
        final String code = rhsResult.code + (argumentPool.getType() == null ? "" : "." + type);
        return new VisitResult(preparationCode, code, "", type);
    }

    private VisitResult dotMethodVisit(JmmNode node, ArgumentPool argumentPool) {
        final String id = argumentPool.getId();
        final Symbol symbol = getSymbol(id, currentMethod, symbolTable);

        final StringBuilder preparationBuilder = new StringBuilder();
        final StringBuilder codeBuilder = new StringBuilder();
        final String method = "\"" + node.get("method") + "\"";
        final String returnType;
        if (symbol == null) {
            if (Utils.hasImport(id, symbolTable)) {
                codeBuilder.append("invokestatic(").append(id);
                returnType = "V";
            } else {
                // Assume it's a symbol from our class
                codeBuilder.append("invokevirtual(").append(id);
                if (!id.equals("this")) codeBuilder.append(".").append(this.symbolTable.getClassName());
                returnType = OllirUtils.convertType(this.symbolTable.getReturnType(node.get("method")));
            }
        } else {
            codeBuilder.append("invokevirtual(").append(symbol.getName()).append(".").append(OllirUtils.convertType(symbol.getType()));

            if (symbol.getType().getName().equals(this.symbolTable.getClassName())) {
                // variable of class type
                returnType = OllirUtils.convertType(this.symbolTable.getReturnType(node.get("method")));
            } else {
                returnType = "V";
            }
        }

        codeBuilder.append(", ").append(method);
        for (int i = 0; i < node.getNumChildren(); ++i) {
            JmmNode childNode = node.getJmmChild(i);
            codeBuilder.append(", ");
            // TODO: There is probably a better way to differentiate what can and can't be inlined.
            boolean needsVariable = !(childNode.getKind().equals("_Identifier") || childNode.getKind().equals("IntegerLiteral") || childNode.getKind().equals("BooleanLiteral"));
            final VisitResult result = visit(childNode, new ArgumentPool(null, needsVariable));
            preparationBuilder.append(result.preparationCode);
            codeBuilder.append("%s.%s".formatted(result.code, result.returnType));
        }
        // return will be appended in DotExpression

        codeBuilder.append(")");
        if (argumentPool.getExpectedReturnType() == null) codeBuilder.append(".%s".formatted(returnType));
        else codeBuilder.append(".%s".formatted(argumentPool.getExpectedReturnType()));
        final String code = codeBuilder.toString();
        final String preparationCode = preparationBuilder.toString();
        return new VisitResult(preparationCode, code, "", returnType);
    }

    private VisitResult dotLengthVisit(JmmNode node, ArgumentPool argumentPool) {
        // TODO: This node will be interpreted as non-terminal node, and temporary variables will have arraylength(this, x).type.type.
        //  Will it be problematic?
        final String code = "arraylength(%s.array.i32).i32".formatted(argumentPool.getId());
        return new VisitResult("", code, "", "i32");
    }

    private VisitResult assignExprVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        final JmmNode lhs = jmmNode.getJmmChild(0);
        final JmmNode rhs = jmmNode.getJmmChild(1);

        final ArgumentPool lhsArgs = new ArgumentPool(null, OllirUtils.isNotTerminalNode(lhs));
        lhsArgs.setTarget(true);
        final VisitResult lhsResult = visit(lhs, lhsArgs);
        final String[] split = lhsResult.code.split("\\.", 2);
        final String assignTarget = split[0];
        final String assignType = lhsResult.returnType;

        final boolean isClassField = OllirUtils.isClassField(assignTarget, currentMethod, symbolTable);
        ArgumentPool rightArgs = new ArgumentPool(assignTarget);
        rightArgs.setExpectedReturnType(assignType);
        if (isClassField) rightArgs.setNotTerminal(OllirUtils.isNotTerminalNode(rhs));
        VisitResult rhsResult = visit(rhs, rightArgs);
        if (isClassField) {
            final String preparationCode = rhsResult.preparationCode + lhsResult.preparationCode;
            final String code = "putfield(this, %s.%s, %s.%s).V".formatted(lhsResult.code, lhsResult.returnType, rhsResult.code, lhsResult.returnType);
            return new VisitResult(preparationCode, code, "");
        }
        final String preparationCode = rhsResult.preparationCode + lhsResult.preparationCode;
        final String code;
        if (OllirUtils.isNotTerminalNode(rhs)) {
            code = "%s.%s :=.%s %s".formatted(lhsResult.code, lhsResult.returnType, assignType, rhsResult.code);
        } else {
            code = "%s.%s :=.%s %s.%s".formatted(lhsResult.code, lhsResult.returnType, assignType, rhsResult.code, lhsResult.returnType);
        }
        return new VisitResult(preparationCode, code, rhsResult.finalCode);
    }

    private VisitResult integerLiteralVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        return new VisitResult("", jmmNode.get("value"), "", "i32");
    }

    private VisitResult booleanLiteralVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        return new VisitResult("", jmmNode.get("value"), "", "bool");
    }

    private VisitResult identifierVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        final Symbol nodeSymbol = getSymbol(jmmNode.get("id"), currentMethod, symbolTable);
        final String type = nodeSymbol != null ? OllirUtils.convertType(nodeSymbol.getType()) : "";
        final boolean isClassField = OllirUtils.isClassField(jmmNode.get("id"), currentMethod, symbolTable);
        if (isClassField && (argumentPool == null || !argumentPool.isTarget())) {
            final String annotatedId = jmmNode.get("id") + (type.isEmpty() ? "" : "." + type);
            // TODO: There is probably a better way to avoid temporary variables on assignment.
            if (argumentPool != null && !jmmNode.getJmmParent().getKind().equals("AssignExpr")) {
                argumentPool.setNotTerminal(true);
            }
            final String calculation = ("getfield(this, %s)").formatted(annotatedId, type);
            return new VisitResult("", calculation, "", type);
        }
        return new VisitResult("", jmmNode.get("id"), "", type);
    }

    private VisitResult addExprVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        return binOpVisit(jmmNode, "+", "i32");
    }

    private VisitResult subExprVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        return binOpVisit(jmmNode, "-", "i32");
    }

    private VisitResult mulExprVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        return binOpVisit(jmmNode, "*", "i32");
    }

    private VisitResult divExprVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        return binOpVisit(jmmNode, "/", "i32");
    }

    private VisitResult lessExprVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        return binOpVisit(jmmNode, "<", "bool");
    }

    private VisitResult andExprVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        return binOpVisit(jmmNode, "&&", "bool");
    }

    private VisitResult notExprVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        final JmmNode node = jmmNode.getJmmChild(0);
        final VisitResult result = visit(node, new ArgumentPool("bool", OllirUtils.isNotTerminalNode(node)));
        final String code = "!.bool %s.%s".formatted(result.code, result.returnType);
        return new VisitResult(result.preparationCode, code, result.finalCode, "bool");
    }

    private VisitResult publicMethodVisit(JmmNode jmmNode, ArgumentPool o) {
        return methodDeclaration(jmmNode, jmmNode.get("name"), false);
    }

    private VisitResult mainDeclVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        return methodDeclaration(jmmNode, "main", true);
    }

    private VisitResult startVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        VisitResult result = defaultVisit(jmmNode, argumentPool);
        return new VisitResult(result.preparationCode, result.code, "");
    }

    private VisitResult thisVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        if (jmmNode.getNumChildren() > 0) {
            throw new RuntimeException("Illegal number of children in node " + jmmNode.getKind() + ".");
        }

        return new VisitResult("", "this", "", symbolTable.getClassName());
    }

    private VisitResult defaultVisit(JmmNode node, ArgumentPool argumentPool) {
        if (node.getNumChildren() < 0) {
            throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
        }

        final StringBuilder codeBuilder = new StringBuilder();
        for (JmmNode childNode : node.getChildren()) {
            if (!OllirUtils.isNotTerminalNode(childNode)) continue;
            final VisitResult result = visit(childNode, new ArgumentPool());
            codeBuilder.append(result.preparationCode);
            codeBuilder.append(result.code);
            if (node.getKind().equals("ClosedStatement")) codeBuilder.append(";\n");
            codeBuilder.append(result.finalCode);
        }

        return new VisitResult("", codeBuilder.toString(), "");
    }

    private VisitResult methodDeclaration(JmmNode jmmNode, String methodName, Boolean isStatic) {
        final StringBuilder codeBuilder = new StringBuilder();
        currentMethod = methodName;
        codeBuilder.append(OllirConstants.TAB);
        codeBuilder.append(".method public ");
        if (isStatic) codeBuilder.append("static ");
        codeBuilder.append(methodName).append("(");

        final List<Symbol> parameters = symbolTable.getParameters(methodName);
        for (Symbol param : parameters) {
            codeBuilder.append(param.getName());
            final String type = OllirUtils.convertType(param.getType());
            codeBuilder.append(".").append(type);
            if (param != parameters.get(parameters.size() - 1)) codeBuilder.append(", ");
        }
        codeBuilder.append(")");

        final Type returnType = symbolTable.getReturnType(methodName);
        codeBuilder.append(".").append(OllirUtils.convertType(returnType)).append(" {\n");

        final VisitResult result = defaultVisit(jmmNode, null);
        codeBuilder.append(result.preparationCode);
        codeBuilder.append(result.code);

        codeBuilder.append("\n");
        if (returnType.getName().equals("void")) codeBuilder.append("ret.V;\n");
        codeBuilder.append(OllirConstants.TAB).append("}").append("\n");
        return new VisitResult("", codeBuilder.toString(), "");
    }

    private VisitResult binOpVisit(JmmNode jmmNode, String operation, String returnType) {
        final JmmNode lhsNode = jmmNode.getJmmChild(0);
        final JmmNode rhsNode = jmmNode.getJmmChild(1);
        final VisitResult lhsResult = visit(lhsNode, new ArgumentPool(returnType, OllirUtils.isNotTerminalNode(lhsNode)));
        final VisitResult rhsResult = visit(rhsNode, new ArgumentPool(returnType, OllirUtils.isNotTerminalNode(rhsNode)));
        final String preparationCode = rhsResult.preparationCode + lhsResult.preparationCode;
        final String code = "%s.%s %s.%s %s.%s".formatted(lhsResult.code, lhsResult.returnType, operation, returnType, rhsResult.code, rhsResult.returnType);
        String finalCode = rhsResult.finalCode + lhsResult.finalCode;
        return new VisitResult(preparationCode, code, finalCode, returnType);
    }

    private VisitResult ignore(JmmNode jmmNode, ArgumentPool argumentPool) {
        return new VisitResult("", "", "");
    }

    @Override
    public VisitResult visit(JmmNode jmmNode) {
        return super.visit(jmmNode, new ArgumentPool());
    }


    private VisitResult tempVisit(JmmNode jmmNode, ArgumentPool argumentPool) {
        final String tempVariableName = "temp%d".formatted(tempCounter++);
        argumentPool.setId(tempVariableName);
        final VisitResult visitResult = super.visit(jmmNode, argumentPool);
        final String suffix;
        if (!OllirUtils.isNotTerminalNode(jmmNode) || jmmNode.getKind().equals("NewObjExpr") || jmmNode.getKind().equals("NewArrayExpr")
            || jmmNode.getKind().equals("DotExpression")) {
            // TODO Repeated types sometimes (.FindMaximum.FindMaximum)
            suffix = ".%s;".formatted(visitResult.returnType);
        } else {
            suffix = ";";
        }
        final String preparationCode = "%s.%s :=.%s %s%s\n".formatted(tempVariableName, visitResult.returnType, visitResult.returnType, visitResult.code, suffix);
        final String code = "%s".formatted(tempVariableName);
        return new VisitResult(visitResult.preparationCode + preparationCode, code, visitResult.finalCode, visitResult.returnType);
    }

    @Override
    public VisitResult visit(JmmNode jmmNode, ArgumentPool argumentPool) {
        VisitResult visit = super.visit(jmmNode, argumentPool);
        if (argumentPool.getIsNotTerminal()) return tempVisit(jmmNode, argumentPool);
        return visit;
    }
}
