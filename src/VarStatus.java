import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VarStatus {
    public Boolean canBeNull;
    public String varName;
    public Set<Integer> blameCodeLines;

    public VarStatus(String varName){
        this.varName = varName;
        canBeNull = null;
        blameCodeLines = new HashSet<>();
    }

    public VarStatus(String varName, Boolean canBeNullParam){
        this.varName = varName;
        canBeNull = canBeNullParam;
        blameCodeLines = new HashSet<>();
    }

    public VarStatus(String varName, Boolean canBeNullParam, Set<Integer> blameCodeLinesParam){
        this.varName = varName;
        canBeNull = canBeNullParam;
        blameCodeLines = blameCodeLinesParam;
    }



    // getters and setters
    public Boolean getCanBeNull() {
        return canBeNull;
    }

    public void setCanBeNull(Boolean canBeNull) {
        this.canBeNull = canBeNull;
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public Set<Integer> getBlameCodeLines() {
        return blameCodeLines;
    }

    public void setBlameCodeLines(Set<Integer> blameCodeLine) {
        this.blameCodeLines = blameCodeLine;
    }

    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    @Override
    protected VarStatus clone() {
        return new VarStatus(varName, canBeNull, new HashSet<>(blameCodeLines));
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (o == null)
            return false;
        if (!(o instanceof VarStatus))
            return false;
        if (((VarStatus) o).getCanBeNull() != this.canBeNull)
            return false;
        if (!((VarStatus) o).getVarName().equals(this.varName))
            return false;
        if (!((VarStatus) o).getBlameCodeLines().equals(this.getBlameCodeLines()))
            return false;
        return true;
    }
}
