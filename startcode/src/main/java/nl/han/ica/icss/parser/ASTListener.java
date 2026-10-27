package nl.han.ica.icss.parser;

import java.util.Stack;

import com.google.j2objc.annotations.Property;
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

    public void enterCssname(ICSSParser.CssnameContext ctx) {
        currentContainer.push(new Stylerule());
    }

    @Override
    public void exitCssname(ICSSParser.CssnameContext ctx) {
        Stylerule stylerule = (Stylerule) currentContainer.pop();
        currentContainer.peek().addChild(stylerule);
    }

    @Override
    public void enterDeclaration(ICSSParser.DeclarationContext ctx) {
        if (ctx.property() != null) {
            Declaration declaration = new Declaration();
            currentContainer.push(declaration);
        }
    }

    @Override
    public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
        if (ctx.property() == null) {
            return;
        }

        Declaration declaration = (Declaration) currentContainer.pop();
        declaration.addChild(buildExpression(ctx.expression()));
        currentContainer.peek().addChild(declaration);
    }

    @Override
    public void enterProperty(ICSSParser.PropertyContext ctx) {
        currentContainer.push(new PropertyName(ctx.LOWER_IDENT().getText()));
    }

    @Override
    public void exitProperty(ICSSParser.PropertyContext ctx) {
        PropertyName property = (PropertyName) currentContainer.pop();
        currentContainer.peek().addChild(property);
    }

    @Override
    public void exitClassSelector(ICSSParser.ClassSelectorContext ctx) {
        currentContainer.peek().addChild(
                new ClassSelector(ctx.CLASS_IDENT().getText())
        );
    }

    @Override
    public void exitIdSelector(ICSSParser.IdSelectorContext ctx) {
        currentContainer.peek().addChild(
                new IdSelector(ctx.ID_IDENT().getText())
        );
    }

    @Override
    public void exitTagSelector(ICSSParser.TagSelectorContext ctx) {
        currentContainer.peek().addChild(
                new TagSelector(ctx.LOWER_IDENT().getText())
        );
    }

    @Override
    public void exitVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
        VariableAssignment assignment = new VariableAssignment();
        assignment.addChild(new VariableReference(ctx.CAPITAL_IDENT().getText()));
        assignment.addChild(buildExpression(ctx.expression()));
        currentContainer.peek().addChild(assignment);
    }

    @Override
    public void enterIfcommand(ICSSParser.IfcommandContext ctx){
        IfClause clause = new IfClause();
        currentContainer.push(clause);
    }

    @Override
    public void exitIfcommand(ICSSParser.IfcommandContext ctx){
        IfClause clause = (IfClause) currentContainer.pop();
        clause.addChild(buildExpression(ctx.expression()));
        currentContainer.peek().addChild(clause);
    }

    @Override
    public void enterElsecommand(ICSSParser.ElsecommandContext ctx) {
        ElseClause clause = new ElseClause();
        currentContainer.push(clause);
    }

    @Override
    public void exitElsecommand(ICSSParser.ElsecommandContext ctx) {
        ElseClause clause = (ElseClause) currentContainer.pop();
        currentContainer.peek().addChild(clause);
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
        if (literal instanceof ICSSParser.PercentageLiteralContext){
            return new PercentageLiteral(((ICSSParser.PercentageLiteralContext) literal).PERCENTAGE().getText());
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