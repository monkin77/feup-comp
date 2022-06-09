package pt.up.fe.comp.optimization;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BooleanSimplificationTest {
    private String compile(String expression) {
        String code = """
                import io;
                class MyClass {
                    public static void main(String[] args) {
                        int x; int y; boolean a;
                        a = %s;
                        io.println(1);
                        io.println(a);
                    }
                }
                    """.formatted(expression);
        Map<String, String> config = new HashMap<>();
        config.put("optimize", "true");
        OllirResult result = TestUtils.optimize(code, config);
        String ollirCode = result.getOllirCode();
        String assignmentLine = ollirCode
                .split("a.bool :=.bool ")[1]
                .split(";")[0]
                .replaceAll("(\\.bool|\\.i32)", "");
        System.out.println(assignmentLine);
        return assignmentLine;
    }

//    @Test
//    public void test() {
//        assertEquals(compile(""), "");
//    }

    @Test
    public void test_1() {
        String normal = "x < y";
        String switched = "y > x";
        String result = compile("(x < y) && (x < y)");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_2() {
        assertEquals(compile("(x < y) && (y < x)"), "false");
    }


    @Test
    public void test_3() {
        assertEquals(compile("(x < y) && !(x < y)"), "false");
    }


    @Test
    public void test_4() {
        String normal = "x < y";
        String switched = "y > x";
        String result = compile("(x < y) && !(y < x)");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_5() {
        assertEquals(compile("(x < y) && (!(x < y) && !(y < x))"), "false");
    }


    @Test
    public void test_6() {
        String normal = "x < y";
        String switched = "y > x";
        String result = compile("(x < y) && !(!(x < y) && !(y < x))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_7() {
        assertEquals(compile("(y < x) && (x < y)"), "false");
    }


    @Test
    public void test_8() {
        String normal = "y < x";
        String switched = "x > y";
        String result = compile("(y < x) && (y < x)");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_9() {
        String normal = "x > y";
        String switched = "y < x";
        String result = compile("(y < x) && !(x < y)");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_10() {
        assertEquals(compile("(y < x) && !(y < x)"), "false");
    }


    @Test
    public void test_11() {
        assertEquals(compile("(y < x) && (!(x < y) && !(y < x))"), "false");
    }


    @Test
    public void test_12() {
        String normal = "y < x";
        String switched = "x > y";
        String result = compile("(y < x) && !(!(x < y) && !(y < x))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_13() {
        assertEquals(compile("!(x < y) && (x < y)"), "false");
    }


    @Test
    public void test_14() {
        String normal = "x > y";
        String switched = "y < x";
        String result = compile("!(x < y) && (y < x)");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_15() {
        String normal = "x >= y";
        String switched = "y <= x";
        String result = compile("!(x < y) && !(x < y)");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_16() {
        String normal = "x == y";
        String switched = "y == x";
        String result = compile("!(x < y) && !(y < x)");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_17() {
        String normal = "x == y";
        String switched = "y == x";
        String result = compile("!(x < y) && (!(x < y) && !(y < x))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_18() {
        String normal = "x > y";
        String switched = "y < x";
        String result = compile("!(x < y) && !(!(x < y) && !(y < x))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_19() {
        String normal = "x < y";
        String switched = "y > x";
        String result = compile("!(y < x) && (x < y)");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_20() {
        assertEquals(compile("!(y < x) && (y < x)"), "false");
    }


    @Test
    public void test_21() {
        String normal = "x == y";
        String switched = "y == x";
        String result = compile("!(y < x) && !(x < y)");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_22() {
        String normal = "y >= x";
        String switched = "x <= y";
        String result = compile("!(y < x) && !(y < x)");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_23() {
        String normal = "x == y";
        String switched = "y == x";
        String result = compile("!(y < x) && (!(x < y) && !(y < x))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_24() {
        String normal = "x < y";
        String switched = "y > x";
        String result = compile("!(y < x) && !(!(x < y) && !(y < x))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_25() {
        assertEquals(compile("(!(x < y) && !(y < x)) && (x < y)"), "false");
    }


    @Test
    public void test_26() {
        assertEquals(compile("(!(x < y) && !(y < x)) && (y < x)"), "false");
    }


    @Test
    public void test_27() {
        String normal = "x == y";
        String switched = "y == x";
        String result = compile("(!(x < y) && !(y < x)) && !(x < y)");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_28() {
        String normal = "x == y";
        String switched = "y == x";
        String result = compile("(!(x < y) && !(y < x)) && !(y < x)");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_29() {
        String normal = "x == y";
        String switched = "y == x";
        String result = compile("(!(x < y) && !(y < x)) && (!(x < y) && !(y < x))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_30() {
        assertEquals(compile("(!(x < y) && !(y < x)) && !(!(x < y) && !(y < x))"), "false");
    }


    @Test
    public void test_31() {
        String normal = "x < y";
        String switched = "y > x";
        String result = compile("!(!(x < y) && !(y < x)) && (x < y)");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_32() {
        String normal = "y < x";
        String switched = "x > y";
        String result = compile("!(!(x < y) && !(y < x)) && (y < x)");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_33() {
        String normal = "x > y";
        String switched = "y < x";
        String result = compile("!(!(x < y) && !(y < x)) && !(x < y)");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_34() {
        String normal = "x < y";
        String switched = "y > x";
        String result = compile("!(!(x < y) && !(y < x)) && !(y < x)");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_35() {
        assertEquals(compile("!(!(x < y) && !(y < x)) && (!(x < y) && !(y < x))"), "false");
    }


    @Test
    public void test_36() {
        String normal = "x != y";
        String switched = "y != x";
        String result = compile("!(!(x < y) && !(y < x)) && !(!(x < y) && !(y < x))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_37() {
        String normal = "x < y";
        String switched = "y > x";
        String result = compile("!(!(x < y) && !(x < y))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_38() {
        String normal = "x != y";
        String switched = "y != x";
        String result = compile("!(!(x < y) && !(y < x))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_39() {
        assertEquals(compile("!(!(x < y) && (x < y))"), "true");
    }


    @Test
    public void test_40() {
        String normal = "x <= y";
        String switched = "y >= x";
        String result = compile("!(!(x < y) && (y < x))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_41() {
        String normal = "x <= y";
        String switched = "y >= x";
        String result = compile("!(!(x < y) && !(!(x < y) && !(y < x)))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_42() {
        String normal = "x != y";
        String switched = "y != x";
        String result = compile("!(!(x < y) && (!(x < y) && !(y < x)))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_43() {
        String normal = "x != y";
        String switched = "y != x";
        String result = compile("!(!(y < x) && !(x < y))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_44() {
        String normal = "y < x";
        String switched = "x > y";
        String result = compile("!(!(y < x) && !(y < x))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_45() {
        String normal = "x >= y";
        String switched = "y <= x";
        String result = compile("!(!(y < x) && (x < y))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_46() {
        assertEquals(compile("!(!(y < x) && (y < x))"), "true");
    }


    @Test
    public void test_47() {
        String normal = "x >= y";
        String switched = "y <= x";
        String result = compile("!(!(y < x) && !(!(x < y) && !(y < x)))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_48() {
        String normal = "x != y";
        String switched = "y != x";
        String result = compile("!(!(y < x) && (!(x < y) && !(y < x)))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_49() {
        assertEquals(compile("!((x < y) && !(x < y))"), "true");
    }


    @Test
    public void test_50() {
        String normal = "x >= y";
        String switched = "y <= x";
        String result = compile("!((x < y) && !(y < x))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_51() {
        String normal = "x >= y";
        String switched = "y <= x";
        String result = compile("!((x < y) && (x < y))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_52() {
        assertEquals(compile("!((x < y) && (y < x))"), "true");
    }


    @Test
    public void test_53() {
        String normal = "x >= y";
        String switched = "y <= x";
        String result = compile("!((x < y) && !(!(x < y) && !(y < x)))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_54() {
        assertEquals(compile("!((x < y) && (!(x < y) && !(y < x)))"), "true");
    }


    @Test
    public void test_55() {
        String normal = "x <= y";
        String switched = "y >= x";
        String result = compile("!((y < x) && !(x < y))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_56() {
        assertEquals(compile("!((y < x) && !(y < x))"), "true");
    }


    @Test
    public void test_57() {
        assertEquals(compile("!((y < x) && (x < y))"), "true");
    }


    @Test
    public void test_58() {
        String normal = "y >= x";
        String switched = "x <= y";
        String result = compile("!((y < x) && (y < x))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_59() {
        String normal = "y >= x";
        String switched = "x <= y";
        String result = compile("!((y < x) && !(!(x < y) && !(y < x)))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_60() {
        assertEquals(compile("!((y < x) && (!(x < y) && !(y < x)))"), "true");
    }


    @Test
    public void test_61() {
        String normal = "x <= y";
        String switched = "y >= x";
        String result = compile("!(!(!(x < y) && !(y < x)) && !(x < y))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_62() {
        String normal = "x >= y";
        String switched = "y <= x";
        String result = compile("!(!(!(x < y) && !(y < x)) && !(y < x))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_63() {
        String normal = "x >= y";
        String switched = "y <= x";
        String result = compile("!(!(!(x < y) && !(y < x)) && (x < y))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_64() {
        String normal = "y >= x";
        String switched = "x <= y";
        String result = compile("!(!(!(x < y) && !(y < x)) && (y < x))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_65() {
        String normal = "x == y";
        String switched = "y == x";
        String result = compile("!(!(!(x < y) && !(y < x)) && !(!(x < y) && !(y < x)))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_66() {
        assertEquals(compile("!(!(!(x < y) && !(y < x)) && (!(x < y) && !(y < x)))"), "true");
    }


    @Test
    public void test_67() {
        String normal = "x != y";
        String switched = "y != x";
        String result = compile("!((!(x < y) && !(y < x)) && !(x < y))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_68() {
        String normal = "x != y";
        String switched = "y != x";
        String result = compile("!((!(x < y) && !(y < x)) && !(y < x))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_69() {
        assertEquals(compile("!((!(x < y) && !(y < x)) && (x < y))"), "true");
    }


    @Test
    public void test_70() {
        assertEquals(compile("!((!(x < y) && !(y < x)) && (y < x))"), "true");
    }


    @Test
    public void test_71() {
        assertEquals(compile("!((!(x < y) && !(y < x)) && !(!(x < y) && !(y < x)))"), "true");
    }


    @Test
    public void test_72() {
        String normal = "x != y";
        String switched = "y != x";
        String result = compile("!((!(x < y) && !(y < x)) && (!(x < y) && !(y < x)))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_73() {
        assertEquals(compile("(x < y) && (y < x)"), "false");
    }


    @Test
    public void test_74() {
        String normal = "x < y";
        String switched = "y > x";
        String result = compile("(x < y) && (x < y)");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_75() {
        String normal = "x < y";
        String switched = "y > x";
        String result = compile("(x < y) && !(y < x)");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_76() {
        assertEquals(compile("(x < y) && !(x < y)"), "false");
    }


    @Test
    public void test_77() {
        assertEquals(compile("(x < y) && (!(y < x) && !(x < y))"), "false");
    }


    @Test
    public void test_78() {
        String normal = "x < y";
        String switched = "y > x";
        String result = compile("(x < y) && !(!(y < x) && !(x < y))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_79() {
        String normal = "y < x";
        String switched = "x > y";
        String result = compile("(y < x) && (y < x)");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_80() {
        assertEquals(compile("(y < x) && (x < y)"), "false");
    }


    @Test
    public void test_81() {
        assertEquals(compile("(y < x) && !(y < x)"), "false");
    }


    @Test
    public void test_82() {
        String normal = "x > y";
        String switched = "y < x";
        String result = compile("(y < x) && !(x < y)");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_83() {
        assertEquals(compile("(y < x) && (!(y < x) && !(x < y))"), "false");
    }


    @Test
    public void test_84() {
        String normal = "y < x";
        String switched = "x > y";
        String result = compile("(y < x) && !(!(y < x) && !(x < y))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_85() {
        String normal = "x > y";
        String switched = "y < x";
        String result = compile("!(x < y) && (y < x)");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_86() {
        assertEquals(compile("!(x < y) && (x < y)"), "false");
    }


    @Test
    public void test_87() {
        String normal = "x == y";
        String switched = "y == x";
        String result = compile("!(x < y) && !(y < x)");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_88() {
        String normal = "x >= y";
        String switched = "y <= x";
        String result = compile("!(x < y) && !(x < y)");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_89() {
        String normal = "x == y";
        String switched = "y == x";
        String result = compile("!(x < y) && (!(y < x) && !(x < y))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_90() {
        String normal = "x > y";
        String switched = "y < x";
        String result = compile("!(x < y) && !(!(y < x) && !(x < y))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_91() {
        assertEquals(compile("!(y < x) && (y < x)"), "false");
    }


    @Test
    public void test_92() {
        String normal = "x < y";
        String switched = "y > x";
        String result = compile("!(y < x) && (x < y)");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_93() {
        String normal = "y >= x";
        String switched = "x <= y";
        String result = compile("!(y < x) && !(y < x)");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_94() {
        String normal = "x == y";
        String switched = "y == x";
        String result = compile("!(y < x) && !(x < y)");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_95() {
        String normal = "x == y";
        String switched = "y == x";
        String result = compile("!(y < x) && (!(y < x) && !(x < y))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_96() {
        String normal = "x < y";
        String switched = "y > x";
        String result = compile("!(y < x) && !(!(y < x) && !(x < y))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_97() {
        assertEquals(compile("(!(x < y) && !(y < x)) && (y < x)"), "false");
    }


    @Test
    public void test_98() {
        assertEquals(compile("(!(x < y) && !(y < x)) && (x < y)"), "false");
    }


    @Test
    public void test_99() {
        String normal = "x == y";
        String switched = "y == x";
        String result = compile("(!(x < y) && !(y < x)) && !(y < x)");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_100() {
        String normal = "x == y";
        String switched = "y == x";
        String result = compile("(!(x < y) && !(y < x)) && !(x < y)");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_101() {
        String normal = "x == y";
        String switched = "y == x";
        String result = compile("(!(x < y) && !(y < x)) && (!(y < x) && !(x < y))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_102() {
        assertEquals(compile("(!(x < y) && !(y < x)) && !(!(y < x) && !(x < y))"), "false");
    }


    @Test
    public void test_103() {
        String normal = "y < x";
        String switched = "x > y";
        String result = compile("!(!(x < y) && !(y < x)) && (y < x)");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_104() {
        String normal = "x < y";
        String switched = "y > x";
        String result = compile("!(!(x < y) && !(y < x)) && (x < y)");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_105() {
        String normal = "x < y";
        String switched = "y > x";
        String result = compile("!(!(x < y) && !(y < x)) && !(y < x)");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_106() {
        String normal = "x > y";
        String switched = "y < x";
        String result = compile("!(!(x < y) && !(y < x)) && !(x < y)");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_107() {
        assertEquals(compile("!(!(x < y) && !(y < x)) && (!(y < x) && !(x < y))"), "false");
    }


    @Test
    public void test_108() {
        String normal = "x != y";
        String switched = "y != x";
        String result = compile("!(!(x < y) && !(y < x)) && !(!(y < x) && !(x < y))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_109() {
        String normal = "x != y";
        String switched = "y != x";
        String result = compile("!(!(x < y) && !(y < x))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_110() {
        String normal = "x < y";
        String switched = "y > x";
        String result = compile("!(!(x < y) && !(x < y))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_111() {
        String normal = "x <= y";
        String switched = "y >= x";
        String result = compile("!(!(x < y) && (y < x))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_112() {
        assertEquals(compile("!(!(x < y) && (x < y))"), "true");
    }


    @Test
    public void test_113() {
        String normal = "x <= y";
        String switched = "y >= x";
        String result = compile("!(!(x < y) && !(!(y < x) && !(x < y)))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_114() {
        String normal = "x != y";
        String switched = "y != x";
        String result = compile("!(!(x < y) && (!(y < x) && !(x < y)))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_115() {
        String normal = "y < x";
        String switched = "x > y";
        String result = compile("!(!(y < x) && !(y < x))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_116() {
        String normal = "x != y";
        String switched = "y != x";
        String result = compile("!(!(y < x) && !(x < y))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_117() {
        assertEquals(compile("!(!(y < x) && (y < x))"), "true");
    }


    @Test
    public void test_118() {
        String normal = "x >= y";
        String switched = "y <= x";
        String result = compile("!(!(y < x) && (x < y))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_119() {
        String normal = "x >= y";
        String switched = "y <= x";
        String result = compile("!(!(y < x) && !(!(y < x) && !(x < y)))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_120() {
        String normal = "x != y";
        String switched = "y != x";
        String result = compile("!(!(y < x) && (!(y < x) && !(x < y)))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_121() {
        String normal = "x >= y";
        String switched = "y <= x";
        String result = compile("!((x < y) && !(y < x))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_122() {
        assertEquals(compile("!((x < y) && !(x < y))"), "true");
    }


    @Test
    public void test_123() {
        assertEquals(compile("!((x < y) && (y < x))"), "true");
    }


    @Test
    public void test_124() {
        String normal = "x >= y";
        String switched = "y <= x";
        String result = compile("!((x < y) && (x < y))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_125() {
        String normal = "x >= y";
        String switched = "y <= x";
        String result = compile("!((x < y) && !(!(y < x) && !(x < y)))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_126() {
        assertEquals(compile("!((x < y) && (!(y < x) && !(x < y)))"), "true");
    }


    @Test
    public void test_127() {
        assertEquals(compile("!((y < x) && !(y < x))"), "true");
    }


    @Test
    public void test_128() {
        String normal = "x <= y";
        String switched = "y >= x";
        String result = compile("!((y < x) && !(x < y))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_129() {
        String normal = "y >= x";
        String switched = "x <= y";
        String result = compile("!((y < x) && (y < x))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_130() {
        assertEquals(compile("!((y < x) && (x < y))"), "true");
    }


    @Test
    public void test_131() {
        String normal = "y >= x";
        String switched = "x <= y";
        String result = compile("!((y < x) && !(!(y < x) && !(x < y)))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_132() {
        assertEquals(compile("!((y < x) && (!(y < x) && !(x < y)))"), "true");
    }


    @Test
    public void test_133() {
        String normal = "x >= y";
        String switched = "y <= x";
        String result = compile("!(!(!(x < y) && !(y < x)) && !(y < x))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_134() {
        String normal = "x <= y";
        String switched = "y >= x";
        String result = compile("!(!(!(x < y) && !(y < x)) && !(x < y))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_135() {
        String normal = "y >= x";
        String switched = "x <= y";
        String result = compile("!(!(!(x < y) && !(y < x)) && (y < x))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_136() {
        String normal = "x >= y";
        String switched = "y <= x";
        String result = compile("!(!(!(x < y) && !(y < x)) && (x < y))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_137() {
        String normal = "x == y";
        String switched = "y == x";
        String result = compile("!(!(!(x < y) && !(y < x)) && !(!(y < x) && !(x < y)))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_138() {
        assertEquals(compile("!(!(!(x < y) && !(y < x)) && (!(y < x) && !(x < y)))"), "true");
    }


    @Test
    public void test_139() {
        String normal = "x != y";
        String switched = "y != x";
        String result = compile("!((!(x < y) && !(y < x)) && !(y < x))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_140() {
        String normal = "x != y";
        String switched = "y != x";
        String result = compile("!((!(x < y) && !(y < x)) && !(x < y))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }


    @Test
    public void test_141() {
        assertEquals(compile("!((!(x < y) && !(y < x)) && (y < x))"), "true");
    }


    @Test
    public void test_142() {
        assertEquals(compile("!((!(x < y) && !(y < x)) && (x < y))"), "true");
    }


    @Test
    public void test_143() {
        assertEquals(compile("!((!(x < y) && !(y < x)) && !(!(y < x) && !(x < y)))"), "true");
    }


    @Test
    public void test_144() {
        String normal = "x != y";
        String switched = "y != x";
        String result = compile("!((!(x < y) && !(y < x)) && (!(y < x) && !(x < y)))");
        assertTrue("%s should be one of %s, %s".formatted(result, normal, switched), result.equals(normal) || result.equals(switched));
    }
}
