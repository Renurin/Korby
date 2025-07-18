package korby;

import javax.management.RuntimeErrorException;

// to clarify, the interpreter is doing a post-order traversal
// each node evaluetes its children before doing its own work
class Interpreter implements Expr.Visitor<Object>{

    void interpret(Expr expression){
        try {
            Object value = evaluate(expression);
            System.out.println(stringify(value));
        } catch (RuntimeError error) {
            // TODO: handle exception
            korby.runtimeError(error);
        }
    }


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
                // Trying to get the runtime error:
                checkNumberOperand(expr.operator, right);
                return -(double)right;


        }
        // UNreachable
        return null;
    }

    private void checkNumberOperand(token operator, Object operand){
        if (operand instanceof Double) {
            return;
        }
        throw new RuntimeError(operator, "Operand must be a number!");
    }

    private void checkNumberOperands(token operator, Object left, Object right){
        if (left instanceof Double && right instanceof Double) {
            return;
        }
        throw new RuntimeError(operator, "Operands must be numbers!");
    }


    // True or false?
    // Using ruby's rule where if not false or nil everything is true
    private boolean isTruthy(Object obj){
        if (obj == null) {
            return false;
        }
        if (obj instanceof Boolean) {
            return (boolean)obj;
        }
        return true;
    }

    private boolean isEqual(Object obj1, Object obj2){
        if (obj1 == null && obj2 == null) {
            return true;
        }
        if (obj1 == null) {
            return false;
        }
        return obj1.equals(obj2);
    }

    private String stringify(Object object){
        if (object == null) {
            return "nil";
        }
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    // binary
    @Override
    public Object visitBinaryExpr(Expr.Binary expr){
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER:
                // Runtime error handling
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                // Runtime error handling
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                // Runtime error handling
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                // Runtime error handling
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            case BANG_EQUAL:
                return !isEqual(left,right);
            case EQUAL_EQUAL:
                return isEqual(left,right);
            case MINUS:
                // Runtime error handling
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings!");
            case SLASH:
                // Runtime error handling
                checkNumberOperands(expr.operator, left, right);
                return (double)left / (double)right;
            case STAR:
                // Runtime error handling
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double) right;
            
        }
        // Unreachable
        return null;
    }
    
}
