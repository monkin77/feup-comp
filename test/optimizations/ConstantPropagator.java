class ConstantPropagator {
//    public int g() {
//        return 1;
//    }
//
//    public int integerAbsorventSimplificationFunction() {
//        int i1; int i2;
//
//        i1 = this.g() * 0; // i = this.g() * 0
//        i2 = 0 * this.g(); // i = 0 * this.g()
//
//        return 0;
//    }
//
//    public int integerAbsorventSimplificationVariable() {
//        int i; int i1; int i2;
//
//        i1 = i * 0; // i = 0
//        i2 = 0 * i; // i = 0
//
//        return 0;
//    }
//
//    public int integerAbsorventSimplificationLiteral() {
//        int i1; int i2;
//
//        i1 = 10 * 0; // i = 0
//        i2 = 0 * 10; // i = 0
//
//        return 0;
//    }
//
//    public int integerNeutralSimplificationFunction() {
//        int i1; int i2; int i3; int i4; int i5; int i6;
//
//        i1 = this.g() + 0; // i = this.g()
//        i2 = 0 + this.g(); // i = this.g()
//        i3 = this.g() - 0; // i = this.g()
//        i4 = this.g() * 1; // i = this.g()
//        i5 = 1 * this.g(); // i = this.g()
//        i6 = this.g() / 1; // i = this.g()
//
//        return 0;
//    }
//
//    public int integerNeutralSimplificationLiteral() {
//        int i1; int i2; int i3; int i4; int i5; int i6;
//
//        i1 = 10 + 0; // i = 10
//        i2 = 0 + 10; // i = 10
//        i3 = 10 - 0; // i = 10
//        i4 = 10 * 1; // i = 10
//        i5 = 1 * 10; // i = 10
//        i6 = 10 / 1; // i = 10
//
//        return 0;
//    }
//
//    public int integerNeutralSimplificationVariable() {
//        int i; int i1; int i2; int i3; int i4; int i5; int i6;
//
//        i1 = i + 0; // i = i
//        i2 = 0 + i; // i = i
//        i3 = i - 0; // i = i
//        i4 = i * 1; // i = i
//        i5 = 1 * i; // i = i
//        i6 = i / 1; // i = i
//
//        return 0;
//    }
//
//    public int booleanSimplificationVariable() {
//        boolean b; boolean b1; boolean b2; boolean b3; boolean b4;
//
//        b1 = false && b; // b = false
//        b2 = b && false; // b = false
//        b3 = true && b; // b = b
//        b4 = b && true; // b = b
//
//        return 0;
//    }
//
//    public int booleanSimplificationLiteral() {
//        boolean b; boolean b1; boolean b2; boolean b3; boolean b4;
//
//        b1 = true && false; // b = false
//        b2 = false && true;  // b = false
//        b3 = true && true; // b = true
//        b4 = false && false; // b = false
//
//        return 0;
//    }

    // TODO: Wrong parse
    public int conditionalSimplificationLiteral() {
        boolean b;
        int i1; int i2;

        b = false;
        if (b) {
            i1 = 0;
        } else {
            i1 = 1;
        }

        // i1 = 0

        b = true;
        if (b) {
            i2 = 0;
        } else {
            i2 = 1;
        }

        // i2 = 1

        return 0;
    }

    public static void main(String[] args) {
    }
}
