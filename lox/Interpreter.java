package lox;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import javax.management.RuntimeErrorException;

// to clarify, the interpreter is doing a post-order traversal
// each node evaluetes its children before doing its own work
class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void>{
    private final Map<Expr, Integer> locals = new HashMap<>();
    final Environment globals = new Environment();
    private Environment environment = globals;
    // private Environment environment = new Environment();

    Interpreter(){
        // Native funcions that returns the number of seconds that passed since some point in time
        globals.define("clock", new LoxCallable() {
            @Override
            public int arity() {return 0;}

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments){
                return (double)System.currentTimeMillis()/1000.0;
            }
            @Override
            public String toString(){return "<native fn>";}
        });
    }

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

    // Here, evaluate the right operand first and if short circuit, if not, we evaluate the right operand.  
    @Override
    public Object visitLogicalExpr(Expr.Logical expr){
        Object left = evaluate(expr);

        if (expr.operator.type == tokenType.OR) {
            if (isTruthy(left)) {
                return left;
            } else{
                if (!isTruthy(left)) {
                    return left;
                }
            }
        }
        return evaluate(expr.right);
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

    void resolve(Expr expr, int depth){
        locals.put(expr,depth);
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
    public Void visitClassStmt(Stmt.Class stmt){
        environment.define(stmt.name.lexemme, null);
        LoxClass klass = new LoxClass (stmt.name.lexemme);
        environment.assign(stmt.name, klass);
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt){
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt){
        LoxFunction function = new LoxFunction(stmt, environment);
        environment.define(stmt.name.lexemme, function);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt){
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
     
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt){
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt){
        Object value = null;
        if (stmt.value != null) {
            value= evaluate(stmt.value);
        }
        throw new Return(value);
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt){
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexemme, value);
        return null;
    }

    // Interpret while loop
    @Override
    public Void visitWhileStmt(Stmt.While stmt){
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr){
        Object value = evaluate(expr.value);
        Integer distance = locals.get(expr);
        if (distance != null) {
            environment.assignAt(distance,expr.name, value);
        } else{
            globals.assign(expr.name, value);
        }
        
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
        return lookupVariable(expr.name, expr);
    }

    private Object lookupVariable(token name, Expr expr){
        Integer distance = locals.get(expr);
        if (distance != null) {
            return environment.getAt(distance, name.lexemme);
        } else{
            return globals.get(name);
        }
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

    @Override
    public Object visitCallExpr(Expr.Call expr){
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }
        // Check if trying to call using strings
        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(expr.paren, "Can only call funcions and classes.");
        }

        LoxCallable function = (LoxCallable)callee;

        // need to check if the number of parameters is equal to the nuber of arguments declared
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected "+function.arity()+" arguments but got "+arguments.size()+".");
        }

        return function.call(this, arguments);
    }
    
}
