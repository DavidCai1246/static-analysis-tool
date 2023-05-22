package resources;
/**
 * Documented class
 */
public class InputJavaFile extends resources.SuperClass {
    public int testField;

    public java.lang.String testField2 = "abc";

    public java.lang.Boolean testField3 = null;

    public java.lang.Boolean testField4 = testField3 || true;

    public java.lang.Integer testField5 = new java.lang.Integer(5);

    public int[][] testField6 = new int[4][4];

    public int testField7 = testField6[1][1];

    public int[] testField8 = new int[]{ 1, 2, 3 };

    public InputJavaFile() {
        super();
        testField4 = super.superField1;
        resources.SuperClass superClass = new resources.SuperClass();
        superClass.superField1 = null;
        boolean j = superClass.superField1 || true;
    }

    public InputJavaFile(java.lang.Integer i) {
        int j = i + 1;
        j = i + 1;
    }

    public int testReturn() {
        java.lang.Integer i = null;
        return i;
    }

    public void testMemberAssignments() {
        resources.SuperClass superClass = new resources.SuperClass();
        superClass.superField1 = false;
    }

    public int testReturnError() {
        java.lang.Integer i = null;
        return i + 5;
    }

    public void testThrow() {
        throw new java.lang.RuntimeException("");
    }

    public void testParam(java.lang.Integer i) {
        int j = i + 5;
    }

    public void testNewArray() {
        java.lang.Integer[] array = null;
        int i = array.length;
        array = new java.lang.Integer[4];
        i = array.length;
        i = array[0] + 5;
    }

    public void testMultipleBlameLines() {
        java.lang.Integer i = 0;
        if (true)
            i = null;
        else
            i = null;

        int j = i + 1;
    }

    public void testUnaryOperator() {
        int i = 0;
        i++;
        java.lang.Integer j = java.lang.Integer.parseInt("5");
        java.lang.Integer x = java.lang.Integer.parseInt("5");
        j = x++;
        j++;
        x++;
        j = null;
        j++;
    }

    public void testInvocation() {
        java.lang.System.out.println("a");
        java.lang.String str = null;
        str.concat("a");
        "a".concat("b");
        str = "a".concat("b");
        str.concat("c");
        "a".concat("b").concat("c");
    }

    public void testLoopMethod() {
        java.lang.Integer x = 0;
        for (int i = 0; i < 1; i = i + 1) {
            x = null;
            java.lang.System.out.println(x + 5);
        }
    }

    public void testLoopMethod2() {
        java.lang.Integer x = 0;
        int i = 0;
        while (i < 3) {
            x = null;
            java.lang.System.out.println(x + 5);
            i++;
        } 
    }

    public void testLoopMethod3() {
        java.lang.Integer x = 0;
        int i = 0;
        do {
            x = null;
            java.lang.System.out.println(x + 5);
            i++;
        } while (i < 3 );
    }

    public void testLoopMethod4() {
        java.lang.Integer x = 0;
        for (int i = 0; i < 3; i = i + 1) {
            for (int j = 0; j < 4; j++) {
                x = null;
                java.lang.System.out.println(x + 5);
            }
        }
    }

    public void testLoopMethod5() {
        java.lang.Integer x = null;
        for (int i = 0; i < 1; i++) {
            if (x == null)
                x = 5;
            else
                x = null;

        }
        java.lang.System.out.println(x + 5);
    }

    public void method() {
        java.lang.Integer test = null;
        if (true) {
            int j = 5;
            test = 2;
        } else {
            int i = 4;
            test = 3;
        }
        int i;
    }

    public void testExceptionMethod() {
        java.lang.Integer test = new java.lang.Integer(5);
        if (true) {
            test = null;
        } else {
        }
        test.intValue();
    }

    public void testExceptionMethod2() {
        java.lang.Integer test = new java.lang.Integer(5);
        if (true) {
            test = null;
        } else {
        }
        java.lang.Integer test2 = test + 3;
    }

    public void testIfMethod() {
        int a;
        java.lang.Object neverNull = new java.lang.Object();
        java.lang.Object mayBeNull = new java.lang.Object();
        java.lang.Object mayBeNull2 = new java.lang.Object();
        java.lang.Object shouldntBeNullAtEnd = null;
        if (0 == 0) {
        } else if (1 == 1) {
        } else if (2 == 2) {
        } else if (3 == 3) {
        } else if (4 == 4) {
            neverNull = new java.lang.Object();
            if (true) {
                mayBeNull = null;
            } else {
                neverNull = new java.lang.Object();
            }
            mayBeNull2 = mayBeNull;
        } else {
            neverNull = new java.lang.Object();
        }
        shouldntBeNullAtEnd = new java.lang.Object();
    }

    public void testMethod() {
        testField = 1;
        int x;
        java.lang.System.out.println(testField2);
    }

    public void testMethod2(java.lang.Boolean param1) {
        java.lang.Boolean localVar1 = null;
        java.lang.Boolean localVar2 = param1;
    }

    /**
     * Document method
     */
    public void toto() {
    }

    public void totoUndocumented() {
    }

    /**
     * Documented method
     */
    protected void totoProtected() {
    }

    protected void totoProtectedUndocumented() {
    }

    /**
     * Documented field
     */
    protected boolean field;

    public java.lang.String fieldNotDocumented;
}