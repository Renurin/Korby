

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
           jKorby.error(line,"Unexpected character");
        }
      }

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
