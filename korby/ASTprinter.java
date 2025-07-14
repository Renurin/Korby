package korby;

public class ASTprinter implements Expr.visitor<String> {
    String print(Expr expr){
        return expr.accept(this);
    }
    @Override
    public String visitBinaryExpr (Expr.Binary expr){
        return parenthesize(expr.operator.lexemme, expr.left, expr.right);
    }
    @Override
    public String visitGroupingExpr (Expr.Grouping group){
        return parenthesize("group",group.expression);
    }
    @Override
    public String visitLiteralExpr (Expr.Literal literal){
        if (literal.value == null) {
            return "nil";
        }
        return literal.value.toString();
    }
    @Override
    public String visitUnaryExpr (Expr.Unary unary){
        return parenthesize(unary.operator.lexemme, unary.right);
    }

    // little help to convert the value to String in subexpressions
    // it gets a name and list os subexpressions and puts em between parenthesis (- 2 3)

    private String parenthesize(String name, Expr ... exprs){
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for( Expr expr : exprs){
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }
    // buit a main to parse but now goin to the real thing
}
