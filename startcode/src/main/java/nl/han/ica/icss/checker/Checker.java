package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.HashMap;



public class Checker {

    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
        variableTypes = new HANLinkedList<>();
        variableTypes.addFirst(new HashMap<>());
        checkNode(ast.root);
    }

    private void checkNode(ASTNode node) {
        if (node instanceof VariableAssignment){
            checkVariable((VariableAssignment) node);
        }
        if (node instanceof Declaration) {
            checkDeclaration((Declaration) node);
        }
//        if (node instanceof IfClause) {
//            checkIfClause((IfClause) node);
//        }
        for (ASTNode child : node.getChildren()) {
            checkNode(child);
        }
    }

    private void checkVariable(VariableAssignment node) {

    }

    private void checkDeclaration(Declaration declaration) {
        if (declaration.property == null || declaration.expression == null) {
            return;
        }

        ExpressionType type = getType(declaration.expression);
        if (type == ExpressionType.UNDEFINED) {
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

//    private void checkIfClause(IfClause ifClause) {
//        if ( ifClause.conditionalExpression == null ) {
//            return;
//        }
//        ExpressionType type = getType(ifClause.conditionalExpression);
//        if (type != ExpressionType.BOOL ) {
//            ifClause.setError("Ongeldig type voor property '" + ifClause.conditionalExpression + "'.");
//        }
//    }

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
        }

        return ExpressionType.UNDEFINED;
    }
    
}
