import sysy.exception.LexerException;
import sysy.exception.ParserException;
import sysy.lexer.Lexer;
import sysy.parser.Parser;

import java.io.*;

public class Compiler {
    public static void main(String[] args) throws IOException, LexerException, ParserException {
//        task1();
        task2();
    }

    private static void task1() throws IOException, LexerException {
        try (var testFile = new FileInputStream("testfile.txt");
             var outputFile = new FileOutputStream("output.txt")) {
            var out = new PrintStream(outputFile);
            var lexer = new Lexer(new InputStreamReader(testFile));
            while (lexer.next()) {
                var token = lexer.getToken();
                out.printf("%s %s\n", token.getType().name(), token.getValue());
            }
        }
    }

    private static void task2() throws IOException, LexerException, ParserException {
        try (var testFile = new FileInputStream("testfile.txt");
             var outputFile = new FileOutputStream("output.txt")) {
            var out = new PrintStream(outputFile);
            var lexer = new Lexer(new InputStreamReader(testFile));
            var parser = new Parser(lexer);
            var result = parser.parse();
            result.walk(
                    out::println,
                    nonTerminalSymbol -> {
                        String type = nonTerminalSymbol.getType();
                        if (!type.equals("BlockItem")
                                && !type.equals("Decl")
                                && !type.equals("BType")
                        ) {
                            out.println(nonTerminalSymbol);
                        }
                    }
            );
        }
    }
}
