import sysy.exception.LexerException;
import sysy.lexer.Lexer;

import java.io.*;

public class Compiler {
    public static void main(String[] args) throws IOException, LexerException {
        try (var in = new FileInputStream("testfile.txt");
             var out = new FileOutputStream("output.txt")) {
            var lexer = new Lexer(new InputStreamReader(in));
            var output = new PrintStream(out);
            while (lexer.next()) {
                var token = lexer.getToken();
                output.printf("%s %s\n", token.getType().name(), token.getValue());
            }
        }
    }
}
