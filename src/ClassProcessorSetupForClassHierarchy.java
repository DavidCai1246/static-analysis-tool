import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtParameter;
import spoon.support.reflect.declaration.CtClassImpl;
import spoon.support.reflect.declaration.CtFieldImpl;
import spoon.support.reflect.declaration.CtMethodImpl;

import java.util.*;


/** this class is charge of just filling classVar for each class. Then they can be inherited by their children later. No printing error/warnings*/

public class ClassProcessorSetupForClassHierarchy extends AbstractProcessor<CtClass> {
    public CtClassImpl ctClass;
    public HashMap<String, VarStatus> classVarMap;
    public List<CtExpression> errorList;
    public List<CtExpression> warningList;
    public HashMap<String, HashMap<String, VarStatus>> classVarMaps;

    public ClassProcessorSetupForClassHierarchy(HashMap<String, HashMap<String, VarStatus>> classVarMapsParam) {
        classVarMap = new HashMap<>();
        errorList = new ArrayList<>();
        warningList = new ArrayList<>();
        classVarMaps = classVarMapsParam;
    }

    public void process(CtClass ctClassParam) {
        classVarMap = new HashMap<>();
        errorList = new ArrayList<>();
        warningList = new ArrayList<>();

        this.ctClass = (CtClassImpl) ctClassParam;
        classVarMaps.put(ctClass.getSimpleName(), classVarMap);

        visitFields();

        //traverse through each method and update our localVarHashMap
        for (CtMethodImpl method : (Set<CtMethodImpl>) ctClass.getMethods()) {
            HashMap<String, VarStatus> localMethodVarMap = cloneVarMap(classVarMap);
            for (CtParameter param: (List<CtParameter>) method.getParameters())
                localMethodVarMap.put(param.getSimpleName(), new VarStatus(param.getSimpleName(), null));
            List<CtStatement> statements = method.getBody().getStatements();
            processScope(statements, localMethodVarMap);
        }
    }

    /**
     * Processes the statements in a scope and updates the given variable map
     * @param statements The list of statements in the scope
     * @param scopeVarMap The variable map for the given scope
     */
    private void processScope(List<CtStatement> statements, HashMap<String, VarStatus> scopeVarMap) {
        for (CtStatement statement : statements) {
            if (statement instanceof CtAssignment<?, ?>) {
                processAssignment((CtAssignment) statement, scopeVarMap);
            } else if (statement instanceof CtLocalVariable<?>) {
                processLocalVar((CtLocalVariable) statement, scopeVarMap);
            } else if (statement instanceof CtIf) {
                processIf((CtIf) statement, scopeVarMap);
            }
            else if (statement instanceof CtLoop) {
                HashMap<String, VarStatus> loopVarMap = cloneVarMap(scopeVarMap);
                if(statement instanceof CtFor) {
                    List<CtStatement> init_statements = ((CtFor) statement).getForInit();
                    for(CtStatement init_statement : init_statements) {
                        if (init_statement instanceof CtAssignment) {
                            processAssignment((CtAssignment) init_statement, loopVarMap);
                        } else if (init_statement instanceof CtLocalVariable) {
                            processLocalVar((CtLocalVariable) init_statement, loopVarMap);
                        }
                    }
                    List<CtStatement> update_statements = ((CtFor) statement).getForUpdate();
                    for(CtStatement update_statement : update_statements) {
                        if (update_statement instanceof CtUnaryOperator<?>) {
                            visitUnaryOperator((CtUnaryOperator) update_statement, loopVarMap);
                        } else if (update_statement instanceof CtAssignment<?,?>) {
                            processAssignment((CtAssignment) update_statement, loopVarMap);
                        }
                    }
                    CtExpression expression = ((CtFor) statement).getExpression();
                    Boolean isExpressionNull = expression != null ? visitExpression(expression, loopVarMap) : Boolean.TRUE;
                }
                else if(statement instanceof CtWhile) {
                    CtExpression expression = ((CtWhile) statement).getLoopingExpression();
                    Boolean isExpressionNull = expression != null ? visitExpression(expression, loopVarMap) : Boolean.TRUE;
                }
                else if(statement instanceof CtDo) {
                    CtExpression expression = ((CtDo) statement).getLoopingExpression();
                    Boolean isExpressionNull = expression != null ? visitExpression(expression, loopVarMap) : Boolean.TRUE;
                }
                else if(statement instanceof CtForEach) {

                }
                CtBlock body = (CtBlock) ((CtLoop) statement).getBody();
                processScope(body.getStatements(), loopVarMap);
                scopeVarMap = mergeHashMaps(scopeVarMap, loopVarMap);
            } else if (statement instanceof CtUnaryOperator<?>) {
                visitUnaryOperator((CtUnaryOperator) statement, scopeVarMap);
            } else if (statement instanceof CtInvocation) {
                visitInvocation((CtInvocation) statement, scopeVarMap);
            }
        }
    }

    private void processAssignment(CtAssignment statement, HashMap<String, VarStatus> scopeVarMap) {
        CtExpression<?> expression;
        VarStatus varStatus;

        CtAssignment<?, ?> assignmentStatement = (CtAssignment<?, ?>) statement;

        String varName = assignmentStatement.getAssigned().toString();
        expression = assignmentStatement.getAssignment();
        varStatus = scopeVarMap.get(varName);

        if (varStatus == null) {
            // TODO: Either the local variable does not exist (compile error) or we need to implement field accessing here
            // I think we can just directly check field var map if it doesn't exist in local map
            varStatus = classVarMap.get(varName);
        }

        Boolean isExpressionNull = expression != null ? visitExpression(expression, scopeVarMap) : Boolean.TRUE;
        varStatus.canBeNull = isExpressionNull;
        if (isExpressionNull == null || isExpressionNull) {
            Integer sourceLine = statement.getPosition().getLine();
            Set<Integer> blameLines = new HashSet<>();
            blameLines.add(sourceLine);
            varStatus.setBlameCodeLines(blameLines);
        }
    }

    private void processLocalVar(CtLocalVariable statement, HashMap<String, VarStatus> scopeVarMap) {
        CtExpression<?> expression;
        VarStatus varStatus;

        CtLocalVariable<?> localVariableStatement = (CtLocalVariable<?>) statement;

        String varName = localVariableStatement.getSimpleName();
        expression = localVariableStatement.getAssignment();
        varStatus = new VarStatus(varName);

        scopeVarMap.put(varName, varStatus);

        assert varStatus != null; // Weak warning suppression; TODO: remove once above `varStatus == null` todo is fixed.

        Boolean isExpressionNull = expression != null ? visitExpression(expression, scopeVarMap) : Boolean.TRUE;
        varStatus.canBeNull = isExpressionNull;
        if (isExpressionNull == null || isExpressionNull) {
            Integer sourceLine = statement.getPosition().getLine();
            Set<Integer> blameLines = new HashSet<>();
            blameLines.add(sourceLine);
            varStatus.setBlameCodeLines(blameLines);
        }
    }



    /**
     * Processes an if statement, updating the given variable map by unioning the map
     * @param ifStatement The if statement to process
     * @param scopeVarMap The scope's var map's state at the given if statement
     */
    private void processIf(CtIf ifStatement, HashMap<String, VarStatus> scopeVarMap) {
        HashMap<String, VarStatus> thenLocalVarMap = cloneVarMap(scopeVarMap);
        HashMap<String, VarStatus> elseLocalVarMap = cloneVarMap(scopeVarMap);

        CtBlock<?> thenStatement = ifStatement.getThenStatement();
        processScope(thenStatement.getStatements(), thenLocalVarMap);

        CtBlock<?> elseStatement = ifStatement.getElseStatement();
        if (elseStatement != null)
            processScope(elseStatement.getStatements(), elseLocalVarMap);

        HashMap<String, VarStatus> unionedLocalVarMap = mergeHashMaps(thenLocalVarMap, elseLocalVarMap);
        scopeVarMap.putAll(unionedLocalVarMap);
    }

    /**
     * Deep clones the variable map
     * @param varMap the input variable map
     * @return a deep cloned variable map
     */
    private HashMap<String, VarStatus> cloneVarMap(HashMap<String, VarStatus> varMap) {
        HashMap<String, VarStatus> output = (HashMap<String, VarStatus>) varMap.clone();

        output.replaceAll((k, v) -> output.get(k).clone());

        return output;
    }

    // traverse the fields and update our classVarMap:
    private void visitFields() {
        List<CtFieldImpl> fields = ctClass.getFields();
        for (CtFieldImpl field : fields) {
            String fieldName = field.getSimpleName();
            VarStatus varStatus = new VarStatus(fieldName);
            if (field.getAssignment() == null)
                varStatus.setCanBeNull(Boolean.TRUE);
            else
                varStatus.setCanBeNull(visitExpression(field.getAssignment(), classVarMap));
            if (varStatus.getCanBeNull() == null || varStatus.getCanBeNull()) {
                Set<Integer> blameLines = new HashSet<>();
                blameLines.add(field.getPosition().getLine());
                varStatus.setBlameCodeLines(blameLines);
            }
            classVarMap.put(fieldName, varStatus);
        }
    }

    public HashMap<String, VarStatus> mergeHashMaps(HashMap<String, VarStatus> map1, HashMap<String, VarStatus> map2) {
        HashMap<String, VarStatus> mergedMap = new HashMap<>();
        // if we need to handle a listOfMap instead
//        if(maps.size() == 0){
//            return mergedMap;
//        }
//
//        //getting the set that contains common varName across all maps
//        Set<String> setOfCommonVarNames = new HashSet<>(maps.get(0).keySet());
//        for(HashMap<String, VarStatus> map: maps){
//            setOfCommonVarNames.retainAll(map.keySet());
//        }
//
//        List<String> listOfCommonVarNames = new ArrayList<>(setOfCommonVarNames);
//        for(String commonVarName: listOfCommonVarNames){
//            mergedMap.put(commonVarName, new VarStatus(commonVarName, false));
//        }
//
//        maps.stream().reduce(mergedMap, (mergingMap, map) ->{
//            for(Map.Entry<String, VarStatus> entry: mergingMap.entrySet()){
//                String mergingMapKey = entry.getKey();
//                Boolean mergingMapCanBeNull = entry.getValue().getCanBeNull();
//                VarStatus mapVal = map.get(mergingMapKey);
//                if(map.get(mergingMapKey) != null){
//                    if
//                }
//            }

        for (Map.Entry<String, VarStatus> map1Entry : map1.entrySet()) {
            String map1Key = map1Entry.getKey();
            //evaluate common item across 2 maps
            if(map2.get(map1Key) != null){
                VarStatus varStatus1 = map1Entry.getValue();
                VarStatus varStatus2 = map2.get(map1Key);
                if(varStatus1.getCanBeNull() == null){
                    if(varStatus2.getCanBeNull() == null){
                        mergedMap.put(map1Key, new VarStatus(map1Key, null, mergeBlameCodeLines(varStatus1, varStatus2)));
                    }
                    else if(varStatus2.getCanBeNull()){
                        mergedMap.put(map1Key, new VarStatus(map1Key, true, new HashSet<>(varStatus2.getBlameCodeLines())));
                    }
                    else{
                        mergedMap.put(map1Key, new VarStatus(map1Key, null, new HashSet<>(varStatus1.getBlameCodeLines())));
                    }
                }
                else if(varStatus1.getCanBeNull()){
                    if(varStatus2.getCanBeNull() == null || !varStatus2.getCanBeNull()){
                        mergedMap.put(map1Key, new VarStatus(map1Key, true, new HashSet<>(varStatus1.getBlameCodeLines())));
                    }
                    else if(varStatus2.getCanBeNull()){
                        mergedMap.put(map1Key, new VarStatus(map1Key, true, mergeBlameCodeLines(varStatus1, varStatus2)));
                    }
                }else{
                    if(varStatus2.getCanBeNull() == null){
                        mergedMap.put(map1Key, new VarStatus(map1Key, null, new HashSet<>(varStatus2.getBlameCodeLines())));
                    }
                    else if(varStatus2.getCanBeNull()){
                        mergedMap.put(map1Key, new VarStatus(map1Key, true, new HashSet<>(varStatus2.getBlameCodeLines())));
                    }
                    else{
                        mergedMap.put(map1Key, new VarStatus(map1Key, false));
                    }
                }
            }
        }

        return mergedMap;
    }

    private Set<Integer> mergeBlameCodeLines(VarStatus varStatus1, VarStatus varStatus2) {
        Set<Integer> mergedBlameCodeLines =  new HashSet<>(varStatus1.getBlameCodeLines());
        mergedBlameCodeLines.addAll(varStatus2.getBlameCodeLines());
        return mergedBlameCodeLines;
    }

    private Boolean visitExpression(CtExpression expression, HashMap<String, VarStatus> scopeVarMap) {
        if (expression instanceof CtBinaryOperator)
            return visitBinaryOperator((CtBinaryOperator) expression, scopeVarMap);
        else if (expression instanceof CtUnaryOperator)
            return visitUnaryOperator((CtUnaryOperator) expression, scopeVarMap);
        else if (expression instanceof CtConditional)
            return visitConditional((CtConditional) expression, scopeVarMap);
        else if (expression instanceof CtLiteral)
            return visitLiteral((CtLiteral) expression);
        else if (expression instanceof CtInvocation)
            return visitInvocation((CtInvocation) expression, scopeVarMap);
        else if (expression instanceof CtNewArray)
            return visitNewArray((CtNewArray) expression, scopeVarMap);
        else if (expression instanceof CtConstructorCall)
            return visitConstructorCall((CtConstructorCall) expression, scopeVarMap);
        else if (expression instanceof CtVariableAccess)
            return visitVariableAccess((CtVariableAccess) expression, scopeVarMap);
        else if (expression instanceof CtExecutableReferenceExpression)
            return false;
        else if (expression instanceof CtTypeAccess)
            return false;
        else if (expression instanceof CtThisAccess)
            return false;
        return false;
    }

    private void logError(CtExpression expression, Boolean canBeNull) {
        if (canBeNull == Boolean.TRUE) {
            errorList.add(expression);
        } else if (canBeNull == null) {
            warningList.add(expression);
        }
    }

    private Boolean visitConditional(CtConditional conditional, HashMap<String, VarStatus> scopeVarMap) {
        Boolean thenResult = visitExpression(conditional.getThenExpression(), scopeVarMap);
        Boolean elseResult = visitExpression(conditional.getElseExpression(), scopeVarMap);
        logError(conditional.getCondition(), visitExpression(conditional.getCondition(), scopeVarMap));
        if ((thenResult != null && thenResult) || (elseResult != null && elseResult))
            return true;
        if (thenResult == null || elseResult == null)
            return null;
        return false;
    }

    private Boolean visitBinaryOperator(CtBinaryOperator operator, HashMap<String, VarStatus> scopeVarMap) {
        logError(operator.getRightHandOperand(), visitExpression(operator.getRightHandOperand(), scopeVarMap));
        logError(operator.getLeftHandOperand(), visitExpression(operator.getLeftHandOperand(), scopeVarMap));
        return false;
    }

    private Boolean visitUnaryOperator(CtUnaryOperator operator, HashMap<String, VarStatus> scopeVarMap) {
        logError(operator.getOperand(), visitExpression(operator.getOperand(), scopeVarMap));
        UnaryOperatorKind kind = operator.getKind();
        if (kind == UnaryOperatorKind.POSTDEC || kind == UnaryOperatorKind.POSTINC || kind == UnaryOperatorKind.PREDEC || kind == UnaryOperatorKind.PREINC){
            String varName = ((CtVariableAccess) operator.getOperand()).getVariable().getSimpleName();
            scopeVarMap.get(varName).setCanBeNull(false);
        }
        return false;
    }

    private Boolean visitLiteral(CtLiteral literal) {
        return literal.getType().getSimpleName().equals("<nulltype>");
    }

    private Boolean visitNewArray(CtNewArray newArray, HashMap<String, VarStatus> scopeVarMap) {
        for (CtExpression dimension : (List<CtExpression>) newArray.getDimensionExpressions()) {
            logError(dimension, visitExpression(dimension, scopeVarMap));
        }
        return false;
    }

    private Boolean visitInvocation(CtInvocation invocation, HashMap<String, VarStatus> scopeVarMap) { // TODO: Add NullPointerException checks
        logError(invocation.getTarget(), visitExpression(invocation.getTarget(), scopeVarMap));
        for (CtExpression arg: (List<CtExpression>) invocation.getArguments())
            logError(arg, visitExpression(arg, scopeVarMap));
        return null;
    }

    private Boolean visitConstructorCall(CtConstructorCall constructorCall, HashMap<String, VarStatus> scopeVarMap) {
        for (CtExpression arg : (List<CtExpression>) constructorCall.getArguments())
            visitExpression(arg, scopeVarMap);
        return false;
    }

    private Boolean visitVariableAccess(CtVariableAccess varAccess, HashMap<String, VarStatus> scopeVarMap) {
        String variableName = varAccess.getVariable().getSimpleName();
        if (scopeVarMap.get(variableName) != null)
            return scopeVarMap.get(variableName).getCanBeNull();
        return null;
    }

//    private void printErrorWarningList() {
//        if (!errorList.isEmpty()) {
//            for (CtExpression error: errorList) {
//                System.out.print("Error found in variable: " + error.toString() + " at line: " + error.getPosition().getLine()+ "\n");
//            }
//        } else {
//            System.out.print("No null-pointer errors detected!\n");
//        }
//        if (!warningList.isEmpty()) {
//            for (CtExpression warning: warningList) {
//                System.out.print("Warning in variable: " + warning.toString() + " at line: " + warning.getPosition().getLine()+ "\n");
//            }
//        } else {
//            System.out.print("No null-pointer warnings detected!\n");
//        }
//    }
}
