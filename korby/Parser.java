package korby;
import java.util.List;
import static korby.tokenType.*;


// Given a valid sequence of tokens, produce the corresponding syntax treee
// Given an invalid sequence, detect errors and tell the user about their mistakes.

public class Parser {
    private static class ParseError extends RuntimeException{}
    private final List<token> tokens;
    private int current = 0;

    Parser(List<token> tokens){
        this.tokens= tokens;
    }
    // visit later when add statements
    // for now its just a single expression
    Expr parse() {
        try{
            return expression();
        } catch(ParseError error){
            return null;
        }
    }
    private Expr expression(){
        return equality();
    }
    private Expr equality(){
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // Rule: comparison  → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;

    private Expr comparison(){
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL,LESS, LESS_EQUAL)) {
            token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // addition and substration
    private Expr term(){
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // and multiplication n division
    private Expr factor(){
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // now for the unary
    // rule: unary          → ( "!" | "-" ) unary
    //                     | primary ;

    private Expr unary(){
        if (match(BANG, MINUS)) {
            token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right)
        }
        return primary();
    }

    // primary
    private Expr primary(){
        if (match(FALSE)) {
            return new Expr.Literal(false);
        }
        if (match(TRUE)) {
            return new Expr.Literal(true);
        }
        if (match(NIL)) {
            return new Expr.Literal(null);
        }

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }
        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expected expression.");
    }
    

    // This checks to see if the current token has any of the given types.
    // If so, it consumes the token and returns true.
    // Otherwise, it returns false and leaves the current token alone. 
    private boolean match(tokenType... types){
        for (tokenType type : types){
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private token consume(tokenType type, String message){
        if (check(type)) {
            return advance();
        }
        throw error(peek(),message);
    }

    // The check() method returns true if the current token is of the given type. 
    private boolean check (tokenType type){
        if (isAtEnd()) {
            return false;
        }
        return peek().type == type;
    }

    // consumes and returns the token
    private token advance(){
        if (!isAtEnd()) {
            current ++;
        }
        return previous();
    }

    // Enter panic mode to report error from consumes()
    private ParseError error(token Token, String message){
        jKorby.error(Token, message);
        return new ParseError();
    }

    // discards tokens til its found a statement boundary
    // the main objective is to find an error and sync back 
    private void syncronize(){
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) {
                return;
            }
            switch (peek().type) {
                case CLASS:
                case FUN:    
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }
            advance();
        }
    }

    // Other primitive funcions
    private boolean isAtEnd() {
    return peek().type == EOF;
  }

  private token peek() {
    return tokens.get(current);
  }

  private token previous() {
    return tokens.get(current - 1);
  }
}
