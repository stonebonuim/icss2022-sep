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

import java.util.HashMap;



public class Checker {

    private HashMap<VariableReference, Expression> variableTypes;

    public void check(AST ast) {
        variableTypes = new HashMap<>();
        checkNode(ast.root);
    }

    private void checkNode(ASTNode node) {
        if (node instanceof VariableAssignment){
            checkVariable((VariableAssignment) node);
        }
        if (node instanceof Declaration) {
            checkDeclaration((Declaration) node);
        }
        if (node instanceof IfClause) {
            checkIfClause((IfClause) node);
        }
        for (ASTNode child : node.getChildren()) {
            checkNode(child);
        }
    }

    private void checkVariable(VariableAssignment variable) {
    if (variable.name == null || variable.expression == null) {
        return;
    }

    variableTypes.put(variable.name, variable.expression);
    }

    private void checkDeclaration(Declaration declaration) {
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
        if ( ifClause.conditionalExpression == null ) {
            return;
        }

        if (ifClause.conditionalExpression instanceof VariableReference) {

            if (getType(variableTypes.get(ifClause.conditionalExpression)) != ExpressionType.BOOL) {
                ifClause.setError("De if-conditie moet een boolean zijn.");
            }
        }
    }

    private ExpressionType checkMath(Expression expression) {
        if (expression instanceof BoolLiteral) {
            expression.setError("De math conditie mag geen boolean zijn");
        } if (expression instanceof ColorLiteral) {
            expression.setError("De math conditie mag geen color");
        } if (expression instanceof MultiplyOperation) {
            if(((Operation) expression).lhs != ScalarLiteral && ((Operation) expression).rhs != ScalarLiteral) {
                expression.setError("De math conditie moet een scalar hebben");
            }
        } if (expression instanceof AddOperation || expression instanceof SubtractOperation) {
            if(getType(expression.lhs) != getType( expression.rhs)) {
                expression.setError("De math conditie moet dezelfde operator hebben");

            }
        }


        return null;
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
            return getType(variableTypes.get(expression));
        } if( expression instanceof AddOperation || expression instanceof MultiplyOperation || expression instanceof SubtractOperation){
           checkMath(expression);
        }

        return ExpressionType.UNDEFINED;
    }
    
}
