package korby;

// to clarify, the interpreter is doing a post-order traversal
// each node evaluetes its children before doing its own work
class Interpreter implements Expr.Visitor<Object>{

    @Override
    public Object visitLiteralExpr(Expr.Literal expr){
        return expr.value;
    }

    // Evaluating parenthesis
    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    // Evaluating unary
    @Override
    public Object visitUnaryExpr(Expr.Unary expr){
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                return -(double)right;
        }
        // UNreachable
        return null;
        
    }
    // True of false?
    
}
