package nl.han.ica.icss.transforms;

import nl.han.ica.icss.ast.AST;
import nl.han.ica.icss.ast.ASTNode;
import nl.han.ica.icss.ast.Declaration;
import nl.han.ica.icss.ast.ElseClause;
import nl.han.ica.icss.ast.Expression;
import nl.han.ica.icss.ast.IfClause;
import nl.han.ica.icss.ast.Literal;
import nl.han.ica.icss.ast.Operation;
import nl.han.ica.icss.ast.Stylesheet;
import nl.han.ica.icss.ast.Stylerule;
import nl.han.ica.icss.ast.VariableAssignment;
import nl.han.ica.icss.ast.VariableReference;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Evaluator implements Transform {

    @Override
    public void apply(AST ast) {
        if (ast == null || ast.root == null) {
            return;
        }
        applyStylesheet(ast.root);
    }

    private void applyStylesheet(Stylesheet node) {
        applyNodes(node.body, new HashMap<>());
    }

    private void applyNodes(ArrayList<ASTNode> nodes, Map<String, Literal> variables) {
        int index = 0;
        while (index < nodes.size()) {
            ASTNode statement = nodes.get(index);

            if (statement instanceof VariableAssignment) {
                VariableAssignment assignment = (VariableAssignment) statement;
                Literal value = evaluate(assignment.expression, variables);
                assignment.expression = value;
                variables.put(assignment.name.name, value);
                index++;
            } else if (statement instanceof Declaration) {
                Declaration declaration = (Declaration) statement;
                declaration.expression = evaluate(declaration.expression, variables);
                index++;
            } else if (statement instanceof Stylerule) {
                Stylerule rule = (Stylerule) statement;
                applyNodes(rule.body, new HashMap<>(variables));
                index++;
            } else if (statement instanceof IfClause) {
                IfClause ifClause = (IfClause) statement;
                Literal condition = evaluate(ifClause.conditionalExpression, variables);

                ArrayList<ASTNode> selectedNodes = ((BoolLiteral) condition).value
                        ? ifClause.body
                        : getElseBody(ifClause.elseClause);
                Map<String, Literal> branchVariables = new HashMap<>(variables);
                applyNodes(selectedNodes, branchVariables);

                for (String variableName : variables.keySet()) {
                    variables.put(variableName, branchVariables.get(variableName));
                }

                nodes.remove(index);
                nodes.addAll(index, selectedNodes);
                index += selectedNodes.size();
            } else {
                index++;
            }
        }
    }

    private ArrayList<ASTNode> getElseBody(ElseClause elseClause) {
        return elseClause == null ? new ArrayList<>() : elseClause.body;
    }

    private Literal evaluate(Expression expression, Map<String, Literal> variables) {

        if (expression instanceof Literal) {
            return (Literal) expression;
        }
        if (expression instanceof VariableReference) {
            VariableReference reference = (VariableReference) expression;
            return variables.get(reference.name);
        }
        if (expression instanceof Operation) {
            return evaluateOperation((Operation) expression, variables);
        }

        return null;
    }

    private Literal evaluateOperation(Operation operation, Map<String, Literal> variables) {
        Literal left = evaluate(operation.lhs, variables);
        Literal right = evaluate(operation.rhs, variables);

        if (operation instanceof AddOperation) {
            return calculate(left, right, '+');
        }
        if (operation instanceof SubtractOperation) {
            return calculate(left, right, '-');
        }
        if (operation instanceof MultiplyOperation) {
            return calculate(left, right, '*');
        }

        return left;
    }

    private Literal calculate(Literal left, Literal right, char operator) {
        if (left instanceof PixelLiteral && right instanceof PixelLiteral) {
            int value = calculate(((PixelLiteral) left).value, ((PixelLiteral) right).value, operator);
            return new PixelLiteral(value);
        }
        if (left instanceof PercentageLiteral && right instanceof PercentageLiteral) {
            int value = calculate(((PercentageLiteral) left).value, ((PercentageLiteral) right).value, operator);
            return new PercentageLiteral(value);
        }
        if (left instanceof ScalarLiteral && right instanceof ScalarLiteral) {
            int value = calculate(((ScalarLiteral) left).value, ((ScalarLiteral) right).value, operator);
            return new ScalarLiteral(value);
        }
        if (operator == '*' && left instanceof ScalarLiteral) {
            return multiply(right, ((ScalarLiteral) left).value);
        }
        if (operator == '*' && right instanceof ScalarLiteral) {
            return multiply(left, ((ScalarLiteral) right).value);
        }

        return left;
    }

    private int calculate(int left, int right, char operator) {
        switch (operator) {
            case '+':
                return left + right;
            case '-':
                return left - right;
            case '*':
                return left * right;
            default:
        }
        return left;
    }

    private Literal multiply(Literal value, int scalar) {
        if (value instanceof PixelLiteral) {
            return new PixelLiteral(((PixelLiteral) value).value * scalar);
        }
        if (value instanceof PercentageLiteral) {
            return new PercentageLiteral(((PercentageLiteral) value).value * scalar);
        }
        if (value instanceof ScalarLiteral) {
            return new ScalarLiteral(((ScalarLiteral) value).value * scalar);
        }
        return value;
    }
}
