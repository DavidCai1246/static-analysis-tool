package resources;

import org.apache.commons.compress.compressors.zstandard.ZstdCompressorOutputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Documented class
 */
public class InputJavaFile extends SuperClass {

    public int testField;
    public String testField2 = "abc";
    public Boolean testField3 = null;
    public Boolean testField4 = testField3 || true;
    public Integer testField5 = new Integer(5);
    public int[][] testField6 = new int[4][4];
    public int testField7 = testField6[1][1];
    public int[] testField8 = {1, 2, 3};

    public InputJavaFile() {
        super();
        testField4 = super.superField1;
        SuperClass superClass = new SuperClass();
        superClass.superField1 = null;
        boolean j = superClass.superField1 || true;
    }

    public InputJavaFile(Integer i) {
        int j = i + 1;
        j = i + 1;
    }

    public int testReturn() {
        Integer i = null;
        return i;
    }

    public void testMemberAssignments() {
        SuperClass superClass = new SuperClass();
        superClass.superField1 = false;
    }

    public int testReturnError() {
        Integer i = null;
        return i + 5;
    }

    public void testThrow() {
        throw new RuntimeException("");
    }

    public void testParam(Integer i) {
        int j = i + 5;
    }

    public void testNewArray() {
        Integer[] array = null;
        int i = array.length;
        array = new Integer[4];
        i = array.length;
        i = array[0] + 5;
    }

    public void testMultipleBlameLines() {
        Integer i = 0;
        if (true)
            i = null;
        else
            i = null;
        int j = i + 1;
    }

    public void testUnaryOperator() {
        int i = 0;
        i++;
        Integer j = Integer.parseInt("5");
        Integer x = Integer.parseInt("5");
        j = x++;
        j++;
        x++;
        j = null;
        j++;
    }

    public void testInvocation() {
        System.out.println("a");
        String str = null;
        str.concat("a");
        "a".concat("b");
        str = "a".concat("b");
        str.concat("c");
        "a".concat("b").concat("c");
    }

    public void testLoopMethod() {
        Integer x = 0;
        for(int i = 0; i < 1; i = i + 1) {
            x = null;
            System.out.println(x + 5);
        }
    }

    public void testLoopMethod2() {
        Integer x = 0;
        int i = 0;
        while(i < 3) {
            x = null;
            System.out.println(x + 5);
            i++;
        }
    }

    public void testLoopMethod3() {
        Integer x = 0;
        int i = 0;
        do {
            x = null;
            System.out.println(x + 5);
            i++;
        } while (i < 3);
    }

    public void testLoopMethod4() {
        Integer x = 0;
        for(int i = 0; i < 3; i = i + 1) {
            for(int j = 0; j < 4; j++) {
                x = null;
                System.out.println(x + 5);
            }
        }
    }

    public void testLoopMethod5() {
        Integer x = null;
        for(int i = 0; i < 1; i++) {
            if (x == null)
                x = 5;
            else
                x = null;
        }
        System.out.println(x+5);
    }

    public void method() {
        Integer test = null;
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
        Integer test = new Integer(5);
        if (true) {
            test = null;
        } else {

        }
        test.intValue();
    }

    public void testExceptionMethod2() {
        Integer test = new Integer(5);
        if (true) {
            test = null;
        } else {

        }
        Integer test2 = test + 3;
    }


    public void testIfMethod() {
        int a;
        Object neverNull = new Object();
        Object mayBeNull = new Object();
        Object mayBeNull2 = new Object();
        Object shouldntBeNullAtEnd = null;
        if(0 == 0) {
        } else if (1 == 1) {
        } else if (2 == 2) {
        } else if (3 == 3) {
        } else if (4 == 4) {
            neverNull = new Object();

            if (true) {
                mayBeNull = null;
            } else {
                neverNull = new Object();
            }
            mayBeNull2 = mayBeNull;
        } else {
            neverNull = new Object();
        }
        shouldntBeNullAtEnd = new Object();
    }

    public void testMethod(){
        testField = 1;
        int x;
        System.out.println(testField2);
    }

    public void testMethod2(Boolean param1) {
        Boolean localVar1 = null;
        Boolean localVar2 = param1;
    }

    /**
     * Document method
     */
    public void toto() {}

    public void totoUndocumented() {}

    /**
     * Documented method
     */
    protected void totoProtected() {}

    protected void totoProtectedUndocumented() {}

    /**
     * Documented field
     */
    protected boolean field;

    public String fieldNotDocumented;
}

class PrivateClass extends SuperClass{
    String privateClassVar = "private hello world";
}



