import sysy.exception.LexerException;
import sysy.exception.ParserException;
import sysy.lexer.Lexer;
import sysy.parser.Parser;

import java.io.*;

public class Compiler {
    public static void main(String[] args) throws IOException, LexerException, ParserException {
        try (var in = new FileInputStream("testfile.txt");
             var out = new FileOutputStream("output.txt")) {
            var output = new PrintStream(out);
            var lexer = new Lexer(new InputStreamReader(in));
            var parser = new Parser(lexer);
            var result = parser.parse();
            System.out.println(result);
            result.walk(output::println, output::println);
//            while (lexer.next()) {
//                var token = lexer.getToken();
//                output.printf("%s %s %d\n", token.getType().name(), token.getValue(), token.getLineNum());
//            }
        }
    }
}
