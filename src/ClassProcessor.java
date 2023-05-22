import org.eclipse.jdt.core.dom.Assignment;
import spoon.MavenLauncher;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.declaration.CtClassImpl;
import spoon.support.reflect.declaration.CtFieldImpl;
import spoon.support.reflect.declaration.CtMethodImpl;
import spoon.support.reflect.reference.CtTypeReferenceImpl;

import java.util.*;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ClassProcessor extends AbstractProcessor<CtClass> {

    public CtClassImpl ctClass;
    public HashMap<String, VarStatus> classVarMap;
    public HashMap<String, VarStatus> staticFieldMap;
    public HashMap<String, VarStatus> nonStaticFieldMap;
    public HashMap<String, VarStatus> superFieldMap;

    //    public List<CtExpression> errorList;
//    public List<CtExpression> warningList;
    public List<ErrorOrWarning> errorList;
    public List<ErrorOrWarning> warningList;
    private boolean printedError;
    public HashMap<String, HashMap<String, VarStatus>> classVarMaps;


    public ClassProcessor(HashMap<String, HashMap<String, VarStatus>> classVarMapsParam) {
        classVarMap = new HashMap<>();
        errorList = new ArrayList<>();
        warningList = new ArrayList<>();
        classVarMaps = classVarMapsParam;
    }

    public void process(CtClass ctClassParam) {
        classVarMap = new HashMap<>();
        errorList = new ArrayList<>();
        warningList = new ArrayList<>();
        printedError = false;

        this.ctClass = (CtClassImpl) ctClassParam;

        System.out.print("\n\nAnalyzing class: " + ctClassParam.getSimpleName() + " from file: " + ctClass.getPosition().getFile().getName() + "\n" );
        //inherit fields from super class if applicable
//        CtTypeReferenceImpl parent = (CtTypeReferenceImpl) ctClass.getSuperclass();
//        while(parent != null){
//            HashMap<String, VarStatus> superClassClassVarMap = classVarMaps.get(parent.getSimpleName());
//            classVarMap.putAll(cloneVarMap(superClassClassVarMap));
//            parent = (CtTypeReferenceImpl) parent.getSuperclass();
//        }

        visitFields();
        printErrorWarningList("Fields");

        for (CtConstructor method : (Set<CtConstructor>) ctClass.getConstructors()) {
            HashMap<String, VarStatus> localMethodVarMap = cloneVarMap(classVarMap);
            for (CtParameter param: (List<CtParameter>) method.getParameters()) {
                HashSet<Integer> blameLines = new HashSet<>();
                blameLines.add(method.getPosition().getLine());
                VarStatus status = new VarStatus(param.getSimpleName(), param.getType().isPrimitive() ? false : null, blameLines);
                localMethodVarMap.put(param.getSimpleName(), status);
            }
            List<CtStatement> statements = method.getBody().getStatements();
            processScope(statements, localMethodVarMap);
            printErrorWarningList(method.getSignature());
            //System.out.println();
        }

        //traverse through each method and update our localVarHashMap
        for (CtMethodImpl method : (Set<CtMethodImpl>) ctClass.getMethods()) {
            HashMap<String, VarStatus> localMethodVarMap = cloneVarMap(classVarMap);
            for (CtParameter param: (List<CtParameter>) method.getParameters()) {
                HashSet<Integer> blameLines = new HashSet<>();
                blameLines.add(method.getPosition().getLine());
                VarStatus status = new VarStatus(param.getSimpleName(), param.getType().isPrimitive() ? false : null, blameLines);
                localMethodVarMap.put(param.getSimpleName(), status);
            }
            List<CtStatement> statements = method.getBody().getStatements();
            processScope(statements, localMethodVarMap);
            printErrorWarningList(method.getSignature());
            //System.out.println();
        }

        if (!printedError)
            System.out.println("No potential null-pointer exceptions were detected.");
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
                while (true) {
                    HashMap<String, VarStatus> loopVarMap = cloneVarMap(scopeVarMap);
                    processLoop((CtLoop) statement, scopeVarMap, loopVarMap);
                    loopVarMap = mergeHashMaps(scopeVarMap, loopVarMap);
                    if (scopeVarMap.equals(loopVarMap))
                        break;
                    scopeVarMap.putAll(loopVarMap);
                }
            } else if (statement instanceof CtUnaryOperator<?>) {
                visitUnaryOperator((CtUnaryOperator) statement, scopeVarMap);
            } else if (statement instanceof CtInvocation) {
                visitInvocation((CtInvocation) statement, scopeVarMap);
            } else if (statement instanceof CtCFlowBreak) {
                if (statement instanceof CtReturn) {
                    visitExpression(((CtReturn<?>) statement).getReturnedExpression(), scopeVarMap);
                } else if (statement instanceof CtThrow) {
                    visitExpression(((CtThrow) statement).getThrownExpression(), scopeVarMap);
                }
                return;
            }
        }
    }

    private void processLoop(CtLoop loop, HashMap<String, VarStatus> scopeVarMap, HashMap<String, VarStatus> loopVarMap) {
        if(loop instanceof CtFor) {
            List<CtStatement> init_statements = ((CtFor) loop).getForInit();
            for(CtStatement init_statement : init_statements) {
                if (init_statement instanceof CtAssignment) {
                    processAssignment((CtAssignment) init_statement, loopVarMap);
                } else if (init_statement instanceof CtLocalVariable) {
                    processLocalVar((CtLocalVariable) init_statement, loopVarMap);
                }
            }
            List<CtStatement> update_statements = ((CtFor) loop).getForUpdate();
            for(CtStatement update_statement : update_statements) {
                if (update_statement instanceof CtUnaryOperator<?>) {
                    visitUnaryOperator((CtUnaryOperator) update_statement, loopVarMap);
                } else if (update_statement instanceof CtAssignment<?,?>) {
                    processAssignment((CtAssignment) update_statement, loopVarMap);
                }
            }
            CtExpression expression = ((CtFor) loop).getExpression();
            Boolean isExpressionNull = expression != null ? visitExpression(expression, loopVarMap) : Boolean.TRUE;
        }
        else if(loop instanceof CtWhile) {
            CtExpression expression = ((CtWhile) loop).getLoopingExpression();
            Boolean isExpressionNull = expression != null ? visitExpression(expression, loopVarMap) : Boolean.TRUE;
        }
        else if(loop instanceof CtDo) {
            CtExpression expression = ((CtDo) loop).getLoopingExpression();
            Boolean isExpressionNull = expression != null ? visitExpression(expression, loopVarMap) : Boolean.TRUE;
        }
        else if(loop instanceof CtForEach) {
            CtExpression expression = ((CtForEach) loop).getExpression();
            Boolean isExpressionNull = expression != null ? visitExpression(expression, loopVarMap) : Boolean.TRUE;
        }
        CtBlock body = (CtBlock) (loop.getBody());
        processScope(body.getStatements(), loopVarMap);
    }

    private void processAssignment(CtAssignment statement, HashMap<String, VarStatus> scopeVarMap) {
            CtExpression<?> expression;
            VarStatus varStatus;

            CtAssignment<?, ?> assignmentStatement = (CtAssignment<?, ?>) statement;

            String varName = assignmentStatement.getAssigned().toString();
            expression = assignmentStatement.getAssignment();
            varStatus = scopeVarMap.get(varName);

            if (varStatus == null) {
                varStatus = new VarStatus(varName);
                scopeVarMap.put(varName, varStatus);
            }

            Boolean isExpressionNull = expression != null ? visitExpression(expression, scopeVarMap) : Boolean.TRUE;
            if (statement.getAssigned().getType().isPrimitive())
                isExpressionNull = false;

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
        if (statement.getType().isPrimitive())
                isExpressionNull = false;

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
            if (field.getType().isPrimitive())
                varStatus.setCanBeNull(false);
            if (varStatus.getCanBeNull() == null || varStatus.getCanBeNull()) {
                Set<Integer> blameLines = new HashSet<>();
                blameLines.add(field.getPosition().getLine());
                varStatus.setBlameCodeLines(blameLines);
            }
            classVarMap.put(fieldName, varStatus);
        }

//        while(true) {
//            HashMap<String, VarStatus> pastVarMap = cloneVarMap(classVarMap);
//
//            for (CtMethod method: (Set<CtMethod>) ctClass.getMethods()) {
//                if (method.isStatic()) {
//                    HashMap<String, VarStatus> localMethodVarMap = cloneVarMap(classVarMap);
//                    for (CtParameter param: (List<CtParameter>) method.getParameters())
//                        localMethodVarMap.put(param.getSimpleName(), new VarStatus(param.getSimpleName(), param.getType().isPrimitive() ? false : null));
//                    List<CtStatement> statements = method.getBody().getStatements();
//                    processScope(statements, localMethodVarMap);
//                    classVarMap.putAll(mergeHashMaps(localMethodVarMap, classVarMap));
//                }
//            }
//
//            for (CtConstructor constructor: (Set<CtConstructor>) ctClass.getConstructors()) {
//                HashMap<String, VarStatus> constructorVarMap = cloneVarMap(classVarMap);
//                for (CtParameter param: (List<CtParameter>) constructor.getParameters())
//                    constructorVarMap.put(param.getSimpleName(), new VarStatus(param.getSimpleName(), param.getType().isPrimitive() ? false : null));
//                List<CtStatement> statements = constructor.getBody().getStatements();
//                processScope(statements, constructorVarMap);
//                classVarMap.putAll(mergeHashMaps(constructorVarMap, classVarMap));
//            }
//
//
//            loopVarMap = mergeHashMaps(scopeVarMap, loopVarMap);
//            if (scopeVarMap.equals(loopVarMap))
//                break;
//            scopeVarMap.putAll(loopVarMap);
//        }
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
                mergedMap.put(map1Key, getMergedVarStatus(map1Key, map1Entry.getValue(), map2.get(map1Key)));
            }
        }

        return mergedMap;
    }

    private VarStatus getMergedVarStatus(String varName, VarStatus varStatus1, VarStatus varStatus2) {
        if(varStatus1.getCanBeNull() == null){
            if(varStatus2.getCanBeNull() == null){
                return new VarStatus(varName, null, mergeBlameCodeLines(varStatus1, varStatus2));
            }
            else if(varStatus2.getCanBeNull()){
                return new VarStatus(varName, true, new HashSet<>(varStatus2.getBlameCodeLines()));
            }
            else{
                return new VarStatus(varName, null, new HashSet<>(varStatus1.getBlameCodeLines()));
            }
        }
        else if(varStatus1.getCanBeNull()){
            if(varStatus2.getCanBeNull() == null || !varStatus2.getCanBeNull()){
                return new VarStatus(varName, true, new HashSet<>(varStatus1.getBlameCodeLines()));
            }
            else {
                return new VarStatus(varName, true, mergeBlameCodeLines(varStatus1, varStatus2));
            }
        }else{
            if(varStatus2.getCanBeNull() == null){
                return new VarStatus(varName, null, new HashSet<>(varStatus2.getBlameCodeLines()));
            }
            else if(varStatus2.getCanBeNull()){
                return new VarStatus(varName, true, new HashSet<>(varStatus2.getBlameCodeLines()));
            }
            else{
                return new VarStatus(varName, false);
            }
        }
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
        else if (expression instanceof CtArrayRead)
            return visitArrayRead((CtArrayRead) expression, scopeVarMap);
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
        return null;
    }
    
//    private void logError(CtExpression expression, Boolean canBeNull) {
//        if (canBeNull == Boolean.TRUE) {
//            errorList.add(expression);
//        } else if (canBeNull == null) {
//            warningList.add(expression);
//        }
//    }
    private void logError(CtExpression expression, Boolean canBeNull, HashMap<String, VarStatus> scopeVarMap) {
        if (canBeNull == Boolean.TRUE) {
            if (scopeVarMap.containsKey(expression.toString())) {
                ErrorOrWarning error = new ErrorOrWarning(expression,scopeVarMap.get(expression.toString()).getBlameCodeLines());
                if (!errorList.contains(error))
                    errorList.add(error);
            }
        } else if (canBeNull == null) {
            if (scopeVarMap.containsKey(expression.toString())) {
                ErrorOrWarning error = new ErrorOrWarning(expression,scopeVarMap.get(expression.toString()).getBlameCodeLines());
                if (!warningList.contains(error))
                    warningList.add(error);
            }
        }
    }


    private Boolean visitConditional(CtConditional conditional, HashMap<String, VarStatus> scopeVarMap) {
        Boolean thenResult = visitExpression(conditional.getThenExpression(), scopeVarMap);
        Boolean elseResult = visitExpression(conditional.getElseExpression(), scopeVarMap);
        logError(conditional.getCondition(), visitExpression(conditional.getCondition(), scopeVarMap), scopeVarMap);
        if ((thenResult != null && thenResult) || (elseResult != null && elseResult))
            return true;
        if (thenResult == null || elseResult == null)
            return null;
        return false;
    }

    private Boolean visitBinaryOperator(CtBinaryOperator operator, HashMap<String, VarStatus> scopeVarMap) {
        logError(operator.getRightHandOperand(), visitExpression(operator.getRightHandOperand(), scopeVarMap), scopeVarMap);
        logError(operator.getLeftHandOperand(), visitExpression(operator.getLeftHandOperand(), scopeVarMap), scopeVarMap);
        return false;
    }

    private Boolean visitUnaryOperator(CtUnaryOperator operator, HashMap<String, VarStatus> scopeVarMap) {
        logError(operator.getOperand(), visitExpression(operator.getOperand(), scopeVarMap), scopeVarMap);
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
            logError(dimension, visitExpression(dimension, scopeVarMap), scopeVarMap);
        }

        List<CtExpression> elements = (List<CtExpression>) newArray.getElements();
        for (int i = 0; i < elements.size(); i++) {
            Boolean canBeNull = visitExpression(elements.get(i), scopeVarMap);
            logError(elements.get(i), canBeNull, scopeVarMap);
        }
        return false;
    }

    private Boolean visitArrayRead(CtArrayRead arrayRead, HashMap<String, VarStatus> scopeVarMap) {
        return arrayRead.getType().isPrimitive() ? false : null;
    }

    private Boolean visitInvocation(CtInvocation invocation, HashMap<String, VarStatus> scopeVarMap) { // TODO: Add NullPointerException checks
        if (invocation.getTarget() != null)
            logError(invocation.getTarget(), visitExpression(invocation.getTarget(), scopeVarMap), scopeVarMap);
        for (CtExpression arg: (List<CtExpression>) invocation.getArguments())
            visitExpression(arg, scopeVarMap);
        return null;
    }

    private Boolean visitConstructorCall(CtConstructorCall constructorCall, HashMap<String, VarStatus> scopeVarMap) {
        for (CtExpression arg : (List<CtExpression>) constructorCall.getArguments())
            visitExpression(arg, scopeVarMap);
        return false;
    }

    private Boolean visitVariableAccess(CtVariableAccess varAccess, HashMap<String, VarStatus> scopeVarMap) {
        String variableName = varAccess.toString();
        if (variableName.split(" ")[variableName.split(" ").length - 1].indexOf('.') == -1)
            variableName = varAccess.getVariable().getSimpleName();
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
    private void printErrorWarningList(String location) {
        if (!errorList.isEmpty()) {
            System.out.println("\nHere are the places in " + location + " where null-pointer exceptions occur in at least one branch or combination of method calls.");
            for (ErrorOrWarning error: errorList) {
                System.out.print("Error found in variable: " + error.getExpression().toString() + " at line: " + error.getExpression().getPosition().getLine() + ", see line(s): " + Arrays.toString(error.getBlameLines().toArray())+ " to see the cause of this error\n");
            }
            errorList.clear();
            printedError = true;
        }
        if (!warningList.isEmpty()) {
            System.out.println("\nHere are the places in " + location + " where the variables may be null and null-pointer exceptions may occur. Ensure these values are not null.");
            for (ErrorOrWarning warning: warningList) {
                System.out.print("Warning in variable: " + warning.getExpression().toString() + " at line: " + warning.getExpression().getPosition().getLine() + ", see line(s): " + Arrays.toString(warning.getBlameLines().toArray())+ " to see the cause of this warning\n");
            }
            warningList.clear();
            printedError = true;
        }
    }
}
