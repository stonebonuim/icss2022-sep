package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.ArrayList;
import java.util.HashMap;



public class Checker {

    private HashMap<VariableReference, ArrayList<ASTNode>> variableTypes;

    public void check(AST ast) {
        variableTypes = new HashMap<>();
        checkNode(ast.root);
    }

    private void checkNode(Stylesheet node ) {
        for (ASTNode child : node.getChildren()) {
            if (child instanceof VariableAssignment) {
                checkVariable((VariableAssignment) child, node);
            }

            if (child instanceof Stylerule) {
                checkStyleRule((Stylerule) child);
            }
        }
    }

    private void checkStyleRule(Stylerule node){
    HashMap<VariableReference, ArrayList<ASTNode>> outervariableTypes = new HashMap<>(variableTypes);
        for (ASTNode child : node.getChildren()) {
            if (child instanceof VariableAssignment) {
                checkVariable((VariableAssignment) child, node);
            }

            if (child instanceof Declaration) {
                checkDeclaration((Declaration) child);
            }
            if (child instanceof IfClause) {
                checkIfClause((IfClause) child);
            }
        }
        variableTypes = outervariableTypes;
    }

    private void checkVariable(VariableAssignment variable, ASTNode node) {
    if (variable.name == null || variable.expression == null) {
        return;
    }

    checkExpressionReferences(variable.expression, node);
    
    if(variable.expression instanceof VariableReference){
        VariableReference variableReference = (VariableReference) variable.expression;
        if (variableReference.equals(variable.name)) {
            variable.setError("Een variable kan niet naar zichzelf reference");
            return;
        }
    }
    ArrayList<ASTNode> Values = new ArrayList<>();
    Values.add(variable.expression);
    Values.add(node);

    variableTypes.put(variable.name, Values);
    }

    private void checkExpressionReferences(Expression expression, ASTNode node) {
    if (expression == null) {
        return;
    }

    if (expression instanceof VariableReference) {
        checkVariableReference((VariableReference) expression, node);
    }

    for (ASTNode child : expression.getChildren()) {
        if (child instanceof Expression) {
            checkExpressionReferences((Expression) child, node);
        }
    }
}

    private void checkVariableReference(VariableReference reference, ASTNode node) {
        if (!variableTypes.containsKey(reference)) {
            reference.setError("Variabele '" + reference.name + "' is niet initialisert.");
            return;
        }
        if (!checkScopeVariable(reference, node))
            reference.setError(reference.name + " is niet gedefinieerd.");
    }

    private boolean checkScopeVariable(VariableReference reference, ASTNode node) {
        ASTNode scope = variableTypes.get(reference).get(1);
        return containsNode(scope, node );
    }

    private boolean containsNode (ASTNode scope, ASTNode target){
         if (scope == target) {
            return true;
        }
        for (ASTNode child : scope.getChildren()) {
            if (containsNode(child, target)) {
                return true;
            }
        }
        return false;
    }


private void checkScope(ASTNode target, ASTNode scope) {
    if (target instanceof VariableAssignment) {
        checkVariable((VariableAssignment) target, scope);
    } else if (target instanceof Declaration) {
        checkDeclaration((Declaration) target);
    } else if (target instanceof IfClause) {
        checkIfClause((IfClause) target);
    }
}
    private void checkDeclaration(Declaration declaration) {
        checkExpressionReferences(declaration.expression, declaration);

        if (declaration.property == null || declaration.expression == null) {
            return;
        }

        ExpressionType type = getType(declaration.expression);
        if (type == ExpressionType.UNDEFINED) {
            declaration.setError("Undefined variable");
            return;
        }


        String property = declaration.property.name;
        boolean valid = false;
        if ("width".equals(property) || "height".equals(property)) {
            valid = type == ExpressionType.PIXEL || type == ExpressionType.PERCENTAGE;
        } else if ("color".equals(property) || "background-color".equals(property)) {
            valid = type == ExpressionType.COLOR;
        }
        if (!valid) {
            declaration.setError("Ongeldig type voor property '" + property + "'.");
        }
    }

    private void checkIfClause(IfClause ifClause) {
    if (ifClause.conditionalExpression == null) {
            return;
        }

        checkExpressionReferences(ifClause.conditionalExpression, ifClause);

        if (getType(ifClause.conditionalExpression) != ExpressionType.BOOL) {
            ifClause.setError("De if-conditie moet een boolean zijn.");
        }

        HashMap<VariableReference, ArrayList<ASTNode>> outerVariableTypes =
                new HashMap<>(variableTypes);

        for (ASTNode statement : ifClause.body) {
            checkScope(statement, ifClause);
        }

        variableTypes = new HashMap<>(outerVariableTypes);

        if (ifClause.elseClause != null) {
            for (ASTNode statement : ifClause.elseClause.body) {
                checkScope(statement, ifClause.elseClause);
            }
        }

        variableTypes = outerVariableTypes;
    }

    private ExpressionType checkMath(Expression expression) {
        if (!(expression instanceof Operation)) {
            return ExpressionType.UNDEFINED;
        }

        Operation operation = (Operation) expression;
        ExpressionType lhsType = getType(operation.lhs);
        ExpressionType rhsType = getType(operation.rhs);

        if (!isNumericType(lhsType) || !isNumericType(rhsType)) {
            expression.setError("Rekenoperaties vereisen numerieke waarden.");
            return ExpressionType.UNDEFINED;
        }

        if (lhsType == ExpressionType.UNDEFINED ||
                rhsType == ExpressionType.UNDEFINED) {
            return ExpressionType.UNDEFINED;
        }

        if (expression instanceof MultiplyOperation) {
            if (lhsType != ExpressionType.SCALAR && rhsType != ExpressionType.SCALAR) {
                expression.setError("Bij vermenigvuldigen moet minimaal een operand een scalar zijn.");
            }
            return lhsType == ExpressionType.SCALAR ? rhsType : lhsType;
        } if (expression instanceof AddOperation || expression instanceof SubtractOperation) {
            if (lhsType != rhsType) {
                expression.setError("Bij optellen en aftrekken moeten de operand types gelijk zijn.");
            } else
                return lhsType;
        }
        return ExpressionType.UNDEFINED;
    }

    private boolean isNumericType(ExpressionType type) {
        return type == ExpressionType.PIXEL
                || type == ExpressionType.PERCENTAGE
                || type == ExpressionType.SCALAR;
    }

    private ExpressionType getType(Expression expression) {
        if (expression instanceof PixelLiteral) {
            return ExpressionType.PIXEL;
        }
        if (expression instanceof PercentageLiteral) {
            return ExpressionType.PERCENTAGE;
        }
        if (expression instanceof ScalarLiteral) {
            return ExpressionType.SCALAR;
        }
        if (expression instanceof ColorLiteral) {
            return ExpressionType.COLOR;
        }
        if (expression instanceof BoolLiteral) {
            return ExpressionType.BOOL;
        } if (expression instanceof VariableReference) {
                ArrayList<ASTNode> values =
            variableTypes.get((VariableReference) expression);

    if (values != null&& !values.isEmpty() && values.get(0) instanceof Expression) {
        return getType((Expression) values.get(0));
    }

        } if( expression instanceof AddOperation || expression instanceof MultiplyOperation || expression instanceof SubtractOperation){
           return checkMath(expression);
        }

        return ExpressionType.UNDEFINED;
    }
    
}
