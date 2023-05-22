package resources;
public class ErrorOrWarning {
    private spoon.reflect.code.CtExpression expression;

    private java.util.List<java.lang.Integer> blameLines;

    public ErrorOrWarning(spoon.reflect.code.CtExpression expression, java.util.List<java.lang.Integer> blameLines) {
        this.expression = expression;
        this.blameLines = blameLines;
    }

    public spoon.reflect.code.CtExpression getExpression() {
        return expression;
    }

    public java.util.List<java.lang.Integer> getBlameLines() {
        return blameLines;
    }
}