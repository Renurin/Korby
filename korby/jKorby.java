package korby;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.IOError;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


public class jKorby {
    static boolean hadError = false; // Restart  had error
    public static void main(String[] args) throws IOException {
        if (args.length >1) {
            System.out.println("Usage: Korby [script]");
            System.out.println(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        }else {
            runPrompt();
        }
    }

    // Run jKorby giving a path to file so that it reads and executes it
    private static void runFile(String path) throws IOException{
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes,Charset.defaultCharset()));
        if (hadError) {
            System.exit(65); // Indicate and error in the exit code
        }
    }

    // Run jKorby without any arguments to open a prompt to run code 
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        for (;;){
            System.out.print("> ");
            String line = reader.readLine(); // Reads an input normally typin ctrl+D return EOF
            if (line == null) { // Checking to stop loop 
                break;
            }
            run(line);
            hadError = false; // Restart without killing the session
        }
    }

    private static void run(String source){
        Scanner scanner = new Scanner(source);
        List<token> tokens = scanner.scanTokens(); // Will create a scanner class later on

        Parser parser = new Parser(tokens);
        Expr expr = parser.parse();

        // Stop if there was a syntax error.
        // done with parse =3
        if (hadError) {
            return;
        }
        System.out.println(new ASTprinter().print(expr));
    }

    // Error Handling
    static void error (int line, String message){
        report(line, "", message);
    }

    private static void report (int line, String where, String message){
        System.err.println("line["+line+"] Error"+where+": "+ message);
        hadError = true;
    }
    static void error (token Token, String message){
        if (Token.type == tokenType.EOF) {
            report(Token.line, " at end", message);
        } else {
            report(Token.line, " at '"+ Token.lexemme+ "'", message);
        }
    }

}
