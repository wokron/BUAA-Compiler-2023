package sysy.parser;

import sysy.exception.LexerException;
import sysy.exception.ParserException;
import sysy.lexer.LexType;
import sysy.lexer.Lexer;
import sysy.lexer.Token;
import sysy.parser.ast.SyntaxNode;

public class Parser {
    private final Lexer lexer;
    private final int tokenBufLen = 3;
    private final Token[] tokenBuf = new Token[tokenBufLen];
    private int currTokenPos = 0;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    private boolean match(Token token, LexType type) {
        return token.getType() == type;
    }

    private void matchOrThrow(Token token, LexType type, ParserException e) throws ParserException {
        if (!match(token, type)) {
            throw e;
        }
    }

    private Token readNextToken() throws LexerException {
        if (lexer.next()) {
            tokenBuf[currTokenPos] = lexer.getToken();
        } else {
            tokenBuf[currTokenPos] = null;
        }
        currTokenPos = (currTokenPos + 1) % tokenBufLen;
        return tokenBuf[currTokenPos];
    }

    private Token readTokenByOffset(int offset) {
        assert offset < tokenBufLen;
        return tokenBuf[(currTokenPos + offset) % tokenBufLen];
    }

    public SyntaxNode parse() throws LexerException, ParserException {
        for (int i = 0; i < tokenBufLen && lexer.next(); i++) {
            if (lexer.next()) {
                tokenBuf[i] = lexer.getToken();
            } else {
                tokenBuf[i] = null;
            }
        }
        Token token = readNextToken();
        var result = parseCompUnit(token);
        token = result.getNextToken();
        if (token != null) {
            throw new ParserException();
        }
        return result.getSubtree();
    }

    private ParseResult parseCompUnit(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        Token preRead = readTokenByOffset(1);
        Token prePreRead = readTokenByOffset(2);
        while (currToken.getType() == LexType.CONSTTK
                || (currToken.getType() == LexType.INTTK
                && preRead.getType() == LexType.IDENFR
                && prePreRead.getType() == LexType.LPARENT)
        ) {
            result = parseDecl(currToken);
            currToken = result.getNextToken();
            preRead = readTokenByOffset(1);
            prePreRead = readTokenByOffset(2);
        }

        while (currToken.getType() == LexType.VOIDTK
                || (currToken.getType() == LexType.INTTK
                && preRead.getType() == LexType.IDENFR)
        ) {
            result = parseFuncDef(currToken);
            currToken = result.getNextToken();
            preRead = readTokenByOffset(1);
        }

        result = parseMainFuncDef(currToken);
        currToken = result.getNextToken();

        return new ParseResult(currToken, null);
    }

    private ParseResult parseFuncDef(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseFuncType(currToken);
        currToken = result.getNextToken();
        result = parseIdent(currToken);
        currToken = result.getNextToken();
        matchOrThrow(currToken, LexType.LPARENT, new ParserException());
        currToken = readNextToken();
        if (currToken.getType() == LexType.INTTK) {
            result = parseFuncFParams(currToken);
            currToken = result.getNextToken();
        }
        matchOrThrow(currToken, LexType.RPARENT, new ParserException());
        result = parseBlock(currToken);
        currToken = result.getNextToken();

        return new ParseResult(currToken, null);
    }

    private ParseResult parseFuncFParams(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseFuncFParam(currToken);
        currToken = result.getNextToken();
        while (currToken.getType() == LexType.COMMA) {
            currToken = readNextToken();
            result = parseFuncFParam(currToken);
            currToken = result.getNextToken();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseFuncFParam(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseBType(currToken);
        currToken = result.getNextToken();
        result = parseIdent(currToken);
        currToken = result.getNextToken();
        if (currToken.getType() == LexType.LBRACK) {
            currToken = readNextToken();
            matchOrThrow(currToken, LexType.RBRACK, new ParserException());
            currToken = readNextToken();
            while (currToken.getType() == LexType.LBRACK) {
                currToken = readNextToken();
                result = parseConstExp(currToken);
                currToken = result.getNextToken();
                matchOrThrow(currToken, LexType.RBRACK, new ParserException());
                currToken = readNextToken();
            }
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseConstExp(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseAddExp(currToken);
        currToken = result.getNextToken();

        return new ParseResult(currToken, null);
    }

    private ParseResult parseAddExp(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseMulExp(currToken);
        currToken = result.getNextToken();
        while (currToken.getType() == LexType.PLUS || currToken.getType() == LexType.MINU) {
            currToken = readNextToken();
            result = parseMulExp(currToken);
            currToken = result.getNextToken();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseMulExp(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseUnaryExp(currToken);
        currToken = result.getNextToken();
        while (currToken.getType() == LexType.MULT || currToken.getType() == LexType.DIV || currToken.getType() == LexType.MOD) {
            currToken = readNextToken();
            result = parseUnaryExp(currToken);
            currToken = result.getNextToken();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseUnaryExp(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        Token preRead = readTokenByOffset(1);
        if (currToken.getType() == LexType.LPARENT
                || (currToken.getType() == LexType.IDENFR
                && preRead.getType() != LexType.LPARENT)
                || currToken.getType() == LexType.INTCON
        ) {
            result = parsePrimaryExp(currToken);
            currToken = result.getNextToken();
        } else if (currToken.getType() == LexType.IDENFR && preRead.getType() == LexType.LPARENT) {
            result = parseIdent(currToken);
            currToken = result.getNextToken();
            matchOrThrow(currToken, LexType.LPARENT, new ParserException());
            currToken = readNextToken();
            if (currToken.getType() != LexType.RPARENT) {
                result = parseFuncRParams(currToken);
                currToken = result.getNextToken();
            }
            matchOrThrow(currToken, LexType.RPARENT, new ParserException());
            currToken = readNextToken();
        } else {
            throw new ParserException();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseFuncRParams(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseExp(currToken);
        currToken = result.getNextToken();
        while (currToken.getType() == LexType.COMMA) {
            currToken = readNextToken();
            result = parseExp(currToken);
            currToken = result.getNextToken();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseExp(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseAddExp(currToken);
        currToken = result.getNextToken();

        return new ParseResult(currToken, null);
    }

    private ParseResult parsePrimaryExp(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        if (currToken.getType() == LexType.LPARENT) {
            currToken = readNextToken();
            result = parseExp(currToken);
            currToken = result.getNextToken();
            matchOrThrow(currToken, LexType.RPARENT, new ParserException());
            currToken = readNextToken();
        } else if (currToken.getType() == LexType.IDENFR) {
            result = parseLVal(currToken);
            currToken = result.getNextToken();
        } else if (currToken.getType() == LexType.INTCON) {
            result = parseNumber(currToken);
            currToken = result.getNextToken();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseLVal(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseIdent(currToken);
        currToken = result.getNextToken();
        while (currToken.getType() == LexType.LBRACK) {
            currToken = readNextToken();
            result = parseExp(currToken);
            currToken = result.getNextToken();
            matchOrThrow(currToken, LexType.RBRACK, new ParserException());
            currToken = readNextToken();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseNumber(Token currToken) throws LexerException, ParserException {
        matchOrThrow(currToken, LexType.INTCON, new ParserException());
        currToken = readNextToken();

        return new ParseResult(currToken, null);
    }

    private ParseResult parseBType(Token currToken) throws LexerException, ParserException {
        matchOrThrow(currToken, LexType.INTTK, new ParserException());
        currToken = readNextToken();
        return new ParseResult(currToken, null);
    }

    private ParseResult parseBlock(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        matchOrThrow(currToken, LexType.LBRACE, new ParserException());
        currToken = readNextToken();
        while (currToken.getType() != LexType.RBRACE) {
            result = parseBlockItem(currToken);
            currToken = result.getNextToken();
        }
        return new ParseResult(currToken, null);
    }

    private ParseResult parseBlockItem(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        if (currToken.getType() == LexType.INTTK || currToken.getType() == LexType.CONSTTK) {
            result = parseDecl(currToken);
            currToken = result.getNextToken();
        } else {
            result = parseStmt(currToken);
            currToken = result.getNextToken();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseStmt(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        Token preRead = readTokenByOffset(1);
        if (currToken.getType() == LexType.IDENFR
                && (preRead.getType() == LexType.LBRACK
                || preRead.getType() == LexType.ASSIGN)
        ) {
            result = parseLVal(currToken);
            currToken = result.getNextToken();
            matchOrThrow(currToken, LexType.ASSIGN, new ParserException());
            currToken = readNextToken();
            if (currToken.getType() == LexType.GETINTTK) {
                currToken = readNextToken();
                matchOrThrow(currToken, LexType.LPARENT, new ParserException());
                currToken = readNextToken();
                matchOrThrow(currToken, LexType.RPARENT, new ParserException());
                currToken = readNextToken();
                matchOrThrow(currToken, LexType.SEMICN, new ParserException());
                currToken = readNextToken();
            } else if (currToken.getType() == LexType.LPARENT
                    || currToken.getType() == LexType.IDENFR
                    || currToken.getType() == LexType.INTCON
            ) {
                result = parseExp(currToken);
                currToken = result.getNextToken();
                matchOrThrow(currToken, LexType.SEMICN, new ParserException());
                currToken = readNextToken();
            } else {
                throw new ParserException();
            }
        } else if (currToken.getType() == LexType.LPARENT
                || currToken.getType() == LexType.IDENFR
                || currToken.getType() == LexType.INTCON
        ) {
            result = parseExp(currToken);
            currToken = result.getNextToken();
            matchOrThrow(currToken, LexType.SEMICN, new ParserException());
            currToken = readNextToken();
        } else if (currToken.getType() == LexType.SEMICN) {
            currToken = readNextToken();
        } else if (currToken.getType() == LexType.LBRACE) {
            currToken = readNextToken();
            result = parseBlock(currToken);
            currToken = result.getNextToken();
        } else if (currToken.getType() == LexType.IFTK) {
            currToken = readNextToken();
            matchOrThrow(currToken, LexType.LPARENT, new ParserException());
            currToken = readNextToken();
            result = parseCond(currToken);
            currToken = result.getNextToken();
            matchOrThrow(currToken, LexType.RPARENT, new ParserException());
            currToken = readNextToken();
            result = parseStmt(currToken);
            currToken = result.getNextToken();
            if (currToken.getType() == LexType.ELSETK) {
                currToken = readNextToken();
                result = parseStmt(currToken);
                currToken = result.getNextToken();
            }
        } else if (currToken.getType() == LexType.FORTK) {
            currToken = readNextToken();
            matchOrThrow(currToken, LexType.LPARENT, new ParserException());
            currToken = readNextToken();
            if (currToken.getType() == LexType.IDENFR) {
                result = parseForStmt(currToken);
                currToken = result.getNextToken();
            }
            matchOrThrow(currToken, LexType.SEMICN, new ParserException());
            currToken = readNextToken();
            if (currToken.getType() == LexType.LPARENT
                    || currToken.getType() == LexType.IDENFR
                    || currToken.getType() == LexType.INTCON
            ) {
                result = parseCond(currToken);
                currToken = result.getNextToken();
            }
            matchOrThrow(currToken, LexType.SEMICN, new ParserException());
            currToken = readNextToken();
            if (currToken.getType() == LexType.IDENFR) {
                result = parseForStmt(currToken);
                currToken = result.getNextToken();
            }
            matchOrThrow(currToken, LexType.RPARENT, new ParserException());
            currToken = readNextToken();
            result = parseStmt(currToken);
            currToken = result.getNextToken();
        } else if (currToken.getType() == LexType.BREAKTK) {
            currToken = readNextToken();
            matchOrThrow(currToken, LexType.SEMICN, new ParserException());
            currToken = readNextToken();
        } else if (currToken.getType() == LexType.CONTINUETK) {
            currToken = readNextToken();
            matchOrThrow(currToken, LexType.SEMICN, new ParserException());
            currToken = readNextToken();
        } else if (currToken.getType() == LexType.RETURNTK) {
            currToken = readNextToken();
            if (currToken.getType() == LexType.LPARENT
                    || currToken.getType() == LexType.IDENFR
                    || currToken.getType() == LexType.INTCON
            ) {
                result = parseExp(currToken);
                currToken = result.getNextToken();
            }
            matchOrThrow(currToken, LexType.SEMICN, new ParserException());
            currToken = readNextToken();
        } else if (currToken.getType() == LexType.PRINTFTK) {
            currToken = readNextToken();
            matchOrThrow(currToken, LexType.LPARENT, new ParserException());
            currToken = readNextToken();
            matchOrThrow(currToken, LexType.STRCON, new ParserException());
            currToken = readNextToken();
            while (currToken.getType() == LexType.COMMA) {
                currToken = readNextToken();
                result = parseExp(currToken);
                currToken = result.getNextToken();
            }
            matchOrThrow(currToken, LexType.RPARENT, new ParserException());
            currToken = readNextToken();
            matchOrThrow(currToken, LexType.SEMICN, new ParserException());
            currToken = readNextToken();
        } else {
            throw new ParserException();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseCond(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseLOrExp(currToken);
        currToken = result.getNextToken();

        return new ParseResult(currToken, null);
    }

    private ParseResult parseLOrExp(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseLAndExp(currToken);
        currToken = result.getNextToken();
        while (currToken.getType() == LexType.OR) {
            currToken = readNextToken();
            result = parseLAndExp(currToken);
            currToken = result.getNextToken();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseLAndExp(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseEqExp(currToken);
        currToken = result.getNextToken();
        while (currToken.getType() == LexType.AND) {
            currToken = readNextToken();
            result = parseEqExp(currToken);
            currToken = result.getNextToken();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseEqExp(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseRelExp(currToken);
        currToken = result.getNextToken();
        while (currToken.getType() == LexType.EQL || currToken.getType() == LexType.NEQ) {
            currToken = readNextToken();
            result = parseRelExp(currToken);
            currToken = result.getNextToken();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseRelExp(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseAddExp(currToken);
        currToken = result.getNextToken();
        while (currToken.getType() == LexType.LSS
                || currToken.getType() == LexType.GRE
                || currToken.getType() == LexType.LEQ
                || currToken.getType() == LexType.GEQ
        ) {
            currToken = readNextToken();
            result = parseAddExp(currToken);
            currToken = result.getNextToken();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseForStmt(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseLVal(currToken);
        currToken = result.getNextToken();
        matchOrThrow(currToken, LexType.ASSIGN, new ParserException());
        currToken = readNextToken();
        result = parseExp(currToken);
        currToken = result.getNextToken();

        return new ParseResult(currToken, null);
    }

    private ParseResult parseIdent(Token currToken) throws LexerException, ParserException {
        matchOrThrow(currToken, LexType.IDENFR, new ParserException());
        currToken = readNextToken();
        return new ParseResult(currToken, null);
    }

    private ParseResult parseFuncType(Token currToken) throws LexerException, ParserException {
        if (currToken.getType() != LexType.VOIDTK || currToken.getType() != LexType.INTTK) {
            throw new ParserException();
        }
        currToken = readNextToken();
        return new ParseResult(currToken, null);
    }

    private ParseResult parseDecl(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        if (currToken.getType() == LexType.CONSTTK) {
            result = parseConstDecl(currToken);
            currToken = result.getNextToken();
        } else if (currToken.getType() == LexType.INTTK) {
            result = parseVarDecl(currToken);
            currToken = result.getNextToken();
        } else {
            throw new ParserException();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseConstDecl(Token currToken) throws LexerException, ParserException{
        ParseResult result;
        matchOrThrow(currToken, LexType.CONSTTK, new ParserException());
        currToken = readNextToken();
        result = parseBType(currToken);
        currToken = result.getNextToken();
        result = parseConstDef(currToken);
        currToken = result.getNextToken();
        while (currToken.getType() == LexType.COMMA) {
            currToken = readNextToken();
            result = parseConstDef(currToken);
            currToken = result.getNextToken();
        }
        matchOrThrow(currToken, LexType.SEMICN, new ParserException());
        currToken = readNextToken();

        return new ParseResult(currToken, null);
    }

    private ParseResult parseConstDef(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseIdent(currToken);
        currToken = result.getNextToken();
        while (currToken.getType() == LexType.LBRACK) {
            currToken = readNextToken();
            result = parseConstExp(currToken);
            currToken = result.getNextToken();
            matchOrThrow(currToken, LexType.RBRACK, new ParserException());
            currToken = readNextToken();
        }
        matchOrThrow(currToken, LexType.ASSIGN, new ParserException());
        currToken = readNextToken();
        result = parseConstInitVal(currToken);
        currToken = result.getNextToken();

        return new ParseResult(currToken, null);
    }

    private ParseResult parseConstInitVal(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseConstExp(currToken);
        currToken = result.getNextToken();

        return new ParseResult(currToken, null);
    }

    private ParseResult parseVarDecl(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseBType(currToken);
        currToken = result.getNextToken();
        result = parseVarDef(currToken);
        currToken = result.getNextToken();
        while (currToken.getType() == LexType.COMMA) {
            currToken = readNextToken();
            result = parseVarDef(currToken);
            currToken = result.getNextToken();
        }
        matchOrThrow(currToken, LexType.SEMICN, new ParserException());
        currToken = readNextToken();

        return new ParseResult(currToken, null);
    }

    private ParseResult parseVarDef(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseIdent(currToken);
        currToken = result.getNextToken();
        while (currToken.getType() == LexType.LBRACK) {
            currToken = readNextToken();
            result = parseConstExp(currToken);
            currToken = result.getNextToken();
            matchOrThrow(currToken, LexType.RBRACK, new ParserException());
            currToken = readNextToken();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseMainFuncDef(Token currToken) throws LexerException, ParserException {
        matchOrThrow(currToken, LexType.INTTK, new ParserException());
        currToken = readNextToken();
        matchOrThrow(currToken, LexType.MAINTK, new ParserException());
        currToken = readNextToken();
        matchOrThrow(currToken, LexType.LPARENT, new ParserException());
        currToken = readNextToken();
        matchOrThrow(currToken, LexType.RPARENT, new ParserException());
        currToken = readNextToken();
        ParseResult result = parseBlock(currToken);
        currToken = result.getNextToken();

        return new ParseResult(currToken, null);
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
