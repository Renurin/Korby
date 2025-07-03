package korby;

public class ASTprinter implements Expr.visitor<String> {
    String print(Expr expr){
        return expr.accept(this);
    }
    @Override
    public String visitBinaryExpr (Expr.Binary expr){
        return parenthesize(expr.operator.lexemme, expr.left, expr.right);
    }
}
