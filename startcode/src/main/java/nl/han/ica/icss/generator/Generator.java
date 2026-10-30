package nl.han.ica.icss.generator;

import nl.han.ica.icss.ast.AST;
import nl.han.ica.icss.ast.ASTNode;
import nl.han.ica.icss.ast.Declaration;
import nl.han.ica.icss.ast.Expression;
import nl.han.ica.icss.ast.Literal;
import nl.han.ica.icss.ast.Selector;
import nl.han.ica.icss.ast.Stylerule;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;

public class Generator {

    public String generate(AST ast) {
        StringBuilder css = new StringBuilder();
        for (ASTNode node : ast.root.body) {
            if (node instanceof Stylerule) {
                if (css.length() > 0) {
                    css.append(System.lineSeparator()).append(System.lineSeparator());
                }
                appendStyleRule(css, (Stylerule) node);
            }
        }
        return css.toString();
    }

    private void appendStyleRule(StringBuilder css, Stylerule rule) {
        for (int i = 0; i < rule.selectors.size(); i++) {
            if (i > 0) {
                css.append(", ");
            }
            css.append(selectorText(rule.selectors.get(i)));
        }

        css.append(" {").append(System.lineSeparator());
        for (ASTNode node : rule.body) {
            if (node instanceof Declaration) {
                appendDeclaration(css, (Declaration) node);
            }
        }
        css.append('}');
    }

    private String selectorText(Selector selector) {
        if (selector instanceof TagSelector) {
            return ((TagSelector) selector).tag;
        }
        if (selector instanceof IdSelector) {
            return ((IdSelector) selector).id;
        }
        if (selector instanceof ClassSelector) {
            return ((ClassSelector) selector).cls;
        }
        return "";
    }

    private void appendDeclaration(StringBuilder css, Declaration declaration) {
        css.append("  ")
                .append(declaration.property.name)
                .append(": ")
                .append(literalText(declaration.expression))
                .append(';')
                .append(System.lineSeparator());
    }

    private String literalText(Expression expression) {

        Literal literal = (Literal) expression;
        if (literal instanceof PixelLiteral) {
            return ((PixelLiteral) literal).value + "px";
        }
        if (literal instanceof PercentageLiteral) {
            return ((PercentageLiteral) literal).value + "%";
        }
        if (literal instanceof ScalarLiteral) {
            return Integer.toString(((ScalarLiteral) literal).value);
        }
        if (literal instanceof ColorLiteral) {
            return ((ColorLiteral) literal).value;
        }
        if (literal instanceof BoolLiteral) {
            return ((BoolLiteral) literal).value ? "TRUE" : "FALSE";
        }

        return "";
    }
}
