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
    }

    // Run jKorby without any arguments to open a prompt to run code 
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        for (;;){
            System.out.print("> ");
            String line = reader.readLine(); // Reads an input 
            if (line == null) {
                break;
            }
            run(line);
        }
    }

    private static void run(String source){

    }
}
