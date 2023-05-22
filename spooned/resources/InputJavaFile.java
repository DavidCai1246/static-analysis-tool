package resources;
/**
 * Documented class
 */
public class InputJavaFile {
    public int testField;

    public java.lang.String testField2 = "abc";

    public void testMethod() {
        testField = 1;
        java.lang.System.out.println(testField2);
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