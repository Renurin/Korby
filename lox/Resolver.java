package lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Function;

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void>{
    private FunctionType currentFunction = FunctionType.NONE;
    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();

    Resolver(Interpreter interpreter){
        this.interpreter = interpreter;
    }

    private enum FunctionType{
        NONE,
        FUNCTION
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt){
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt){
        declare(stmt.name);
        define(stmt.name);

        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt){
        declare(stmt.name);
        define(stmt.name);

        resolveFunction(stmt, FunctionType.FUNCTION);

        return null;
    }
    @Override
    public Void visitPrintStmt(Stmt.Print stmt){
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt){
        if (currentFunction == FunctionType.NONE) {
            jLox.error(stmt.keyword, "Cant return from top level code.");
        }
        if (stmt.value != null) {
            resolve(stmt.value);
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt){
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }



    @Override
    public Void visitVarStmt(Stmt.Var stmt){
        declare(stmt.name);
        if (stmt.initializer != null) {
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr){
        resolve(expr.value);
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr){
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr){
        resolve(expr.callee);
        for (Expr arg : expr.arguments) {
            resolve(arg);
        }
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr){
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr){
        return null;
        // Does not mention any variables
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr){
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr){
        resolve(expr.right);
        return null;
    }

    

    @Override
    public Void visitVariableExpr(Expr.Variable expr){
        if (!scopes.isEmpty() && scopes.peek().get(expr.name.lexemme) == Boolean.FALSE) {
            jLox.error(expr.name, "Cant read local variable in its own initializer.");
        }
        resolveLocal(expr, expr.name);
        return null;
    }

    void resolve(List<Stmt> statements){
        for (Stmt statement : statements) {
            resolve(statement);
        }
    }

    private void resolve(Stmt stmt){
        stmt.accept(this);
    }

    private void resolveFunction(Stmt.Function function, FunctionType type){
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;
        beginScope();
        for (token param : function.params) {
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();
        currentFunction = enclosingFunction;

    }

    private void resolve(Expr expr){
        expr.accept(this);
    }

    private void beginScope(){
        scopes.push(new HashMap<String, Boolean>());
    }

    private void endScope(){
        scopes.pop();
    }

    private void declare(token name){
        if (scopes.isEmpty()) {
            return;
        }
        Map<String, Boolean> scope = scopes.peek();
        if (scope.containsKey(name.lexemme)) {
            jLox.error(name, "Already a variable named in this scope.");
        }

        scope.put(name.lexemme, false);
    }

    private void define(token name){
        if (scopes.isEmpty()) {
            return;
        }
        scopes.peek().put(name.lexemme, true);
    }

    private void resolveLocal(Expr expr, token name){
        for (int i = scopes.size() - 1  ; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexemme)) {
                interpreter.resolve(expr, scopes.size() - 1 - i);
            }   
        }
    }
    
}