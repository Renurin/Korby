package tool;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

// The main reason of this file is to generate the ast
// I really did not wanna write all 21 clases by myself =3

public class generateAST {
    public static void main(String[] args) throws IOException{
        if (args.length!=1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];

        defineAST(outputDir,"Expr", Arrays.asList(
        "Binary   : Expr left, token operator, Expr right",
        "Grouping : Expr expression",
        "Literal  : Object value",
        "Unary    : token operator, Expr right"
        ));
    }
    // This needs to output the base Expr.java
    private static void defineAST(String outputDir, String basename, List<String> types) throws IOException{
        String path = outputDir + "/" + basename + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");
        writer.println("package korby;");
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class "+basename+"{");

        // define the AST classes
        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, basename, className, fields);
        }
        writer.println("}");
        writer.close();
    }
    private static void defineType( PrintWriter writer, String baseName, String className, String fieldList){
        // Creating class for Expr:
        writer.println(" static class "+ className + " extends " + baseName +"{");

        // Constructor
        writer.println("    " + className + "(" + fieldList + ") {");

        // Store parameters in fields
        String[] fields = fieldList.split(",");
        for (String field : fields) {
                String name = field.split(" ")[1];
                writer.println("    this."+ name +"= " + name+ ";");
        }
        writer.println("    }"); // close constructor

        // Fields
        writer.println();
        for (String field : fields) {
            writer.println("    final "+ field + ";");
        }
        writer.println("}"); // closing class
    }
}
