package pt.up.fe.comp.optimizations;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BooleanRulesMap {
    public static final Map<List<String>, String> normalAndReduce = Map.of(
            List.of("LessExpr", "LessEqualExpr"), "LessExpr",
            List.of("LessExpr", "NotEqualExpr"), "LessExpr",
            List.of("GreaterExpr", "GreaterEqualExpr"), "GreaterExpr",
            List.of("GreaterExpr", "NotEqualExpr"), "GreaterExpr",
            List.of("EqualExpr", "GreaterEqualExpr"), "EqualExpr",
            List.of("EqualExpr", "LessEqualExpr"), "EqualExpr",
            List.of("LessEqualExpr", "GreaterEqualExpr"), "EqualExpr",
            List.of("GreaterEqualExpr", "NotEqualExpr"), "GreaterExpr",
            List.of("LessEqualExpr", "NotEqualExpr"), "LessExpr"
    );
    public static final Map<List<String>, String> normalOrReduce = Map.of(
            List.of("GreaterEqualExpr", "GreaterExpr"), "GreaterEqualExpr",
            List.of("GreaterEqualExpr", "EqualExpr"), "GreaterEqualExpr",
            List.of("LessEqualExpr", "LessExpr"), "LessEqualExpr",
            List.of("LessEqualExpr", "EqualExpr"), "LessEqualExpr",
            List.of("NotEqualExpr", "LessExpr"), "NotEqualExpr",
            List.of("NotEqualExpr", "GreaterExpr"), "NotEqualExpr",
            List.of("GreaterExpr", "LessExpr"), "NotEqualExpr",
            List.of("LessExpr", "EqualExpr"), "LessEqualExpr",
            List.of("GreaterExpr", "EqualExpr"), "GreaterEqualExpr",
            List.of("LessExpr ", "EqualExpr"), "LessEqualExpr");
    public static final Set<List<String>> normalOrTrue = Set.of(
            List.of("GreaterExpr", "LessEqualExpr"),
            List.of("LessExpr", "GreaterEqualExpr"),
            List.of("LessEqualExpr", "GreaterEqualExpr"),
            List.of("GreaterEqualExpr", "NotEqualExpr"),
            List.of("LessEqualExpr", "NotEqualExpr"),
            List.of("EqualExpr", "NotEqualExpr")
    );
    public static final Set<List<String>> normalAndFalse = Set.of(
            List.of("LessExpr", "GreaterExpr"),
            List.of("LessExpr", "GreaterEqualExpr"),
            List.of("LessExpr", "EqualExpr"),
            List.of("GreaterExpr", "LessExpr"),
            List.of("GreaterExpr", "LessEqualExpr"),
            List.of("GreaterExpr", "EqualExpr"),
            List.of("EqualExpr", "NotEqualExpr")
    );
    public static final Map<List<String>, String> switchedAndReduce = Map.of(
            List.of("LessExpr", "GreaterExpr"), "LessExpr",
            List.of("LessExpr", "GreaterEqualExpr"), "LessExpr",
            List.of("LessExpr", "NotEqualExpr"), "LessExpr",
            List.of("GreaterExpr", "LessEqualExpr"), "GreaterExpr",
            List.of("GreaterExpr", "NotEqualExpr"), "GreaterExpr",
            List.of("GreaterEqualExpr", "LessEqualExpr"), "GreaterEqualExpr",
            List.of("EqualExpr", "GreaterEqualExpr"), "EqualExpr",
            List.of("EqualExpr", "LessEqualExpr"), "EqualExpr",
            List.of("GreaterEqualExpr", "NotEqualExpr"), "GreaterExpr",
            List.of("LessEqualExpr", "NotEqualExpr"), "LessExpr"
    );
    public static final Map<List<String>, String> switchedOrReduce = Map.of(
            List.of("LessExpr", "GreaterExpr"), "LessExpr",
            List.of("GreaterEqualExpr", "LessExpr"), "GreaterEqualExpr",
            List.of("GreaterEqualExpr", "LessEqualExpr"), "GreaterEqualExpr",
            List.of("GreaterEqualExpr", "EqualExpr"), "GreaterEqualExpr",
            List.of("LessEqualExpr", "GreaterExpr"), "LessEqualExpr",
            List.of("LessEqualExpr", "EqualExpr"), "LessEqualExpr",
            List.of("NotEqualExpr", "LessExpr"), "NotEqualExpr",
            List.of("NotEqualExpr", "GreaterExpr"), "NotEqualExpr",
            List.of("LessExpr", "EqualExpr"), "LessEqualExpr",
            List.of("GreaterExpr", "EqualExpr"), "GreaterEqualExpr"
    );
    public static final Set<List<String>> switchedAndFalse = Set.of(
            List.of("LessExpr", "LessEqualExpr"),
            List.of("LessExpr", "EqualExpr"),
            List.of("GreaterExpr", "GreaterEqualExpr"),
            List.of("GreaterExpr", "EqualExpr"),
            List.of("EqualExpr", "NotEqualExpr")
    );
    public static final Set<List<String>> switchedOrTrue = Set.of(
            List.of("LessExpr", "LessEqualExpr"),
            List.of("GreaterExpr", "GreaterEqualExpr"),
            List.of("GreaterEqualExpr", "GreaterExpr"),
            List.of("GreaterEqualExpr", "NotEqualExpr"),
            List.of("LessEqualExpr", "LessExpr"),
            List.of("LessEqualExpr", "NotEqualExpr"),
            List.of("EqualExpr", "NotEqualExpr"),
            List.of("NotEqualExpr", "GreaterEqualExpr"),
            List.of("NotEqualExpr", "LessEqualExpr"),
            List.of("NotEqualExpr", "EqualExpr")
    );
    public static final Map<String, String> notReduce = Map.of(
            "LessExpr", "GreaterEqualExpr",
            "GreaterExpr", "LessEqualExpr",
            "EqualExpr", "NotEqualExpr",
            "GreaterEqualExpr", "LessExpr",
            "LessEqualExpr", "GreaterExpr",
            "NotEqualExpr", "EqualExpr"
    );
    public static final Map<String, String> notDeMorgan = Map.of(
            "AndExpr", "OrExpr",
            "OrExpr", "AndExpr"
    );
}