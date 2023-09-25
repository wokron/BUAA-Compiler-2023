package sysy.parser;

import sysy.exception.LexerException;
import sysy.exception.ParserException;
import sysy.lexer.Lexer;
import sysy.lexer.Token;
import sysy.parser.ast.SyntaxNode;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;

public class Parser {
    private final Lexer lexer;
    private final Queue<Token> preReadQueue = new ArrayDeque<>();

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    private Token readNext() throws IOException, LexerException {
        if (!preReadQueue.isEmpty()) {
            return preReadQueue.poll();
        } else {
            if (lexer.next()) {
                return lexer.getToken();
            } else {
                return null;
            }
        }
    }

    private Token preReadNext() throws IOException, LexerException {
        if (lexer.next()) {
            Token token = lexer.getToken();
            preReadQueue.add(token);
            return token;
        } else {
            return null;
        }
    }

    public SyntaxNode parse() throws IOException, LexerException, ParserException {
        Token token = readNext();
        var result = parseCompUnit(token);
        token = result.getNextToken();
        if (token != null) {
            throw new ParserException();
        }
        return result.getSubtree();
    }

    private ParseResult parseCompUnit(Token currToken) throws IOException {
//        Token preRead1 = preReadNext();
//        Token preRead2 = preReadNext();
        throw new RuntimeException();  // need to implement
    }
}

class ParseResult {
    private final Token nextToken;
    private final SyntaxNode subtree;

    public ParseResult(Token nextToken, SyntaxNode subtree) {
        this.nextToken = nextToken;
        this.subtree = subtree;
    }

    public Token getNextToken() {
        return nextToken;
    }

    public SyntaxNode getSubtree() {
        return subtree;
    }
}
