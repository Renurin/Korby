package lox;

import java.util.List;

import javax.management.RuntimeErrorException;

// to clarify, the interpreter is doing a post-order traversal
// each node evaluetes its children before doing its own work
class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void>{

    void interpret(List <Stmt> stmts){
        try {
            for ( Stmt stmt : stmts) {
                execute(stmt);
            }
        } catch (RuntimeError error) {
            // TODO: handle exception
            jLox.runtimeError(error);
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

    private void execute(Stmt stmt){
        stmt.accept(this);
    }

    void executeBlock(List<Stmt> statements, Environment environment){
        Environment previous = this.environment;
        try {
            this.environment = environment;
            for (Stmt stmt : statements) {
                execute(stmt);
            }
            
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt){
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt){
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt){
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        }
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt){
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt){
        Object value = null;
        if (stmt.initialiaze != null) {
            value = evaluate(stmt.initialiaze);
        }

        Environment.define(stmt.name.lexemme, value);
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr){
        Object value = evaluate(expr.value);
        Environment.assign(expr.name, value);
        return value;
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

    @Override
    public Object visitVariableExpr(Expr.Variable expr){
        return Environment.get(expr.name);
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
