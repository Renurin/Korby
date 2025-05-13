

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Scanner { // SCAN!!!!
    private final String source;
    private final List<token> tokens= new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    // Keyword Map or smth  
    private static final Map<String, tokenType> keywords;
    static{
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }
    Scanner(String source){
        this.source = source;
    }

    List<token> scanTokens(){
        while (!isAtEnd()) {
            // We are  at the begging of the next lexemme !
            start = current;
            scanToken();
        }
        tokens.add(new token(EOF,"", null, line));
        return tokens;
    }

    // If consumed all characters
    private boolean isAtEnd(){
        return current >= source.length();
    }

    // Recognize Lexemes
    private void scanToken() {
        char c = advance();
        switch (c) {
            // Single character
        case '(': addToken(LEFT_PAREN); break;
        case ')': addToken(RIGHT_PAREN); break;
        case '{': addToken(LEFT_BRACE); break;
        case '}': addToken(RIGHT_BRACE); break;
        case ',': addToken(COMMA); break;
        case '.': addToken(DOT); break;
        case '-': addToken(MINUS); break;
        case '+': addToken(PLUS); break;
        case ';': addToken(SEMICOLON); break;
        case '*': addToken(STAR); break; 
            
            // Operators
        case '!':
            addToken(match('=') ? BANG_EQUAL : BANG);
            break;
        case '=':
            addToken(match('=') ? EQUAL_EQUAL : EQUAL);
            break;
        case '<':
            addToken(match('=') ? LESS_EQUAL : LESS);
            break;
        case '>':
            addToken(match('=') ? GREATER_EQUAL : GREATER);
            break;
        case '/':
            if (match('/')){
                // if it IS a comment than read til the end of line
                while (peek()!= '\n' && !isAtEnd()) {
                    advance();
                }
            } else {
                addToken(SLASH);
            }
            break;

            // Ignoring...
        case ' ':
        case '\r':
        case '\t':
            break;
        case '\n':
            line++;
            break;
        case '"':
            string();
            break;
        default:
            if (isDigit(c)) {
                number();
            } 
            else if (isAlpha(c)) {
                identifier();
            }
            else jKorby.error(line,"Unexpected character");
        }
      }

      // Check if alpha numeric
      private void identifier(){
        while (isAlphaNumeric(peek())) {
            advance();
        }
      }
    
      // Check if next char is =
      private boolean match(char expected){
        if(isAtEnd()) return false;
        if (source.charAt(current) != expected) {
            return false;            
        }
        current ++;
        return true;
      }

      // Get comments -> Lookahead
      private char peek(){
        if (isAtEnd()) {
            return '\0';
        }
        return source.charAt(current);
      }
      // Find if its a number
      private boolean isDigit(char c){
        return c >= '0' && c<=9;
      }

      // If we indeed are in a number, find if has floating point
      private void number(){
        if (peek() == '.' && isDigit(peekNext())) {
            // consume the .
            advance();

            while (isDigit(peek())) {
                advance();
            }
        }
        addToken(NUMBER, Double.parseDouble(source.substring(start,current)));
      }

      // Check to see if there is a number after the .
      private char peekNext(){
        if (current+1 >= source.length()) {
            return '\0';
        }
        return source.charAt(current +1 );
      }

      private boolean isAlpha(char check){
        return( check >= 'a' && check <= 'z' || check >= 'A' && check <= 'Z' || check == '_' );
      }

      private boolean isAlphaNumeric(char check){
        return isAlpha(check) || isDigit(check);
      }

      // Increment char to be read
      private char advance(){
        return source.charAt(current++); 
      }

      // Read String like in comment
      private void string(){
        while (peek() != '"' && !isAtEnd()) {
            if (peek() =='\n') {
                line++;
            }
            advance();
        }
        // if it doesnt close
        if (isAtEnd()) {
            jKorby.error(line, "Unterminated String");
        }
        // if close
        advance();

        // Trim the surrounding quotes " "
        String value = source.substring(start+1, current -1);
        addToken(STRING, value);
      }

      // Output, grabs the lexemme and creates the token
      private void addToken(tokenType type){
        addToken(type,null);
      }
      private void addToken (tokenType type, Object literal){ // String n Comments
            String text = source.substring(start, current);
            tokens.add(new token(type, text, literal, line));
      }
}
