import sysy.error.ErrorRecorder;
import sysy.exception.LexerException;
import sysy.exception.ParserException;
import sysy.lexer.Lexer;
import sysy.parser.Parser;

import java.io.*;

public class Compiler {
    private static final ErrorRecorder recorder = new ErrorRecorder();

    public static void main(String[] args) throws IOException, LexerException, ParserException {
//        task1();
//        task2();
        task3();
    }

    private static void task1() throws IOException, LexerException {
        try (var testFile = new FileInputStream("testfile.txt");
             var outputFile = new FileOutputStream("output.txt")) {
            var out = new PrintStream(outputFile);
            var lexer = new Lexer(new InputStreamReader(testFile), recorder);
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
            var lexer = new Lexer(new InputStreamReader(testFile), recorder);
            var parser = new Parser(lexer, recorder);
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

    private static void task3() throws IOException, LexerException, ParserException {
        try (var testFile = new FileInputStream("testfile.txt");
             var outputFile = new FileOutputStream("output.txt")) {
            var out = new PrintStream(outputFile);
            var lexer = new Lexer(new InputStreamReader(testFile), recorder);
            var parser = new Parser(lexer, recorder);
            parser.parse();

            for (var error : recorder.getErrors()) {
                out.println(error);
            }
        }
    }
}
