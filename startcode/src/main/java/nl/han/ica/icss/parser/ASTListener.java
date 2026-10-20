package nl.han.ica.icss.parser;

import java.util.Stack;

import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {

    // Accumulator attributes:
    private AST ast;

    // Use this to keep track of the parent nodes when recursively traversing the
    // ast
    private HANStack<ASTNode> currentContainer;

    public ASTListener() {
        ast = new AST();
        currentContainer = new HANStack<>();
    }

    public AST getAST() {
        return ast;
    }

    @Override
    public void enterStylesheet(ICSSParser.StylesheetContext ctx) {
        ASTNode stylesheet = new Stylesheet();
        currentContainer.push(stylesheet);
    }

    @Override
    public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
        ast.root = (Stylesheet) currentContainer.pop();
    }

    public void enterStylerule(ICSSParser.StyleruleContext ctx) {
        ASTNode stylerule = new Stylerule();
        currentContainer.push(stylerule);
    }

    @Override
    public void exitStylerule(ICSSParser.StyleruleContext ctx) {
        Stylerule stylerule = (Stylerule) currentContainer.pop();
        currentContainer.peek().addChild(stylerule);

    }


    @Override
    public void enterDeclaration(ICSSParser.DeclarationContext ctx) {
        Declaration declaration = new Declaration();
        declaration.addChild(getAST())
        currentContainer.push(declaration);
    }

    @Override
    public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
        Declaration declaration = (Declaration) currentContainer.pop();
        declaration.getChildren();
        currentContainer.peek().addChild(declaration);
    }

    private Expression buildExpression(ICSSParser.ExpressionContext ctx) {
        if (ctx.MUL() != null) {
            MultiplyOperation operation = new MultiplyOperation();
            operation.addChild(buildExpression(ctx.expression(0)));
            operation.addChild(buildExpression(ctx.expression(1)));
            return operation;
        }

        if (ctx.mathvariable() != null) {
            Operation operation = ctx.mathvariable().getText().equals("+")
                    ? new AddOperation()
                    : new SubtractOperation();
            operation.addChild(buildExpression(ctx.expression(0)));
            operation.addChild(buildExpression(ctx.expression(1)));
            return operation;
        }

        if (ctx.CAPITAL_IDENT() != null) {
            return new VariableReference(ctx.CAPITAL_IDENT().getText());
        }

        ICSSParser.LiteralContext literal = ctx.literal();
        if (literal instanceof ICSSParser.PixelLiteralContext) {
            return new PixelLiteral(((ICSSParser.PixelLiteralContext) literal).PIXELSIZE().getText());
        }
        if (literal instanceof ICSSParser.ColorLiteralContext) {
            return new ColorLiteral(((ICSSParser.ColorLiteralContext) literal).COLOR().getText());
        }
        if (literal instanceof ICSSParser.ScalarLiteralContext) {
            return new ScalarLiteral(((ICSSParser.ScalarLiteralContext) literal).SCALAR().getText());
        }
        return new BoolLiteral(literal.getText());
    }

}