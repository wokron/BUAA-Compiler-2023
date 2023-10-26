import sysy.error.ErrorRecorder;
import sysy.exception.LexerException;
import sysy.exception.ParserException;
import sysy.frontend.lexer.Lexer;
import sysy.frontend.parser.Parser;
import sysy.frontend.parser.syntaxtree.CompUnitNode;
import sysy.frontend.visitor.Visitor;

import java.io.*;

public class Compiler {
    private static final ErrorRecorder recorder = new ErrorRecorder();

    public static void main(String[] args) throws IOException, LexerException, ParserException {
//        task1();
//        task2();
//        task3();
        task4LLVM();
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
             var errFile = new FileOutputStream("error.txt")) {
            var err = new PrintStream(errFile);
            var lexer = new Lexer(new InputStreamReader(testFile), recorder);
            var parser = new Parser(lexer, recorder);
            var result = parser.parse();

            var visitor = new Visitor(recorder);
            visitor.visitCompUnitNode((CompUnitNode) result);

            for (var error : recorder.getErrors()) {
                err.println(error);
            }
        }
    }

    private static void task4LLVM() throws IOException, LexerException, ParserException {
        try (var testFile = new FileInputStream("testfile.txt");
             var outputFile = new FileOutputStream("output.txt")) {
            var out = new PrintStream(outputFile);
            var lexer = new Lexer(new InputStreamReader(testFile), recorder);
            var parser = new Parser(lexer, recorder);
            var result = parser.parse();

            var visitor = new Visitor(recorder);
            var module = visitor.generateIR(result);

            module.dump(out);

        }
    }
}
