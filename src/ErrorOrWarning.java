import spoon.reflect.code.CtExpression;

import java.util.List;
import java.util.Set;

public class ErrorOrWarning {
    private CtExpression expression;
    private Set<Integer> blameLines;
    public ErrorOrWarning(CtExpression expression, Set<Integer> blameLines) {
        this.expression = expression;
        this.blameLines = blameLines;
    }

    public CtExpression getExpression() {
        return expression;
    }

    public Set<Integer> getBlameLines() {
        return blameLines;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (o == null)
            return false;
        if (!(o instanceof ErrorOrWarning))
            return false;
        if (!((ErrorOrWarning) o).getExpression().equals(this.expression))
            return false;
        if (!((ErrorOrWarning) o).getBlameLines().equals(this.blameLines))
            return false;
        if (((ErrorOrWarning) o).getExpression().getPosition().getLine() != this.getExpression().getPosition().getLine())
            return false;
        return true;
    }
}