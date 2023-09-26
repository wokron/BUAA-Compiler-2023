package sysy.parser;

import sysy.exception.LexerException;
import sysy.exception.ParserException;
import sysy.lexer.LexType;
import sysy.lexer.Lexer;
import sysy.lexer.Token;
import sysy.parser.ast.SyntaxNode;

public class Parser {
    private final PreReadBuffer buf;

    public Parser(Lexer lexer) throws LexerException {
        this.buf = new PreReadBuffer(lexer, 3);
    }

    private static boolean isMatch(Token token, LexType type) {
        return token.getType() == type;
    }
    
    private static boolean isNotMatch(Token token, LexType type) {
        return token.getType() != type;
    }

    private static void matchOrThrow(Token token, LexType type, ParserException e) throws ParserException {
        if (isNotMatch(token, type)) {
            throw e;
        }
    }
    
    private Token parseToken(Token token, LexType type, ParserException onFail) throws LexerException, ParserException {
        matchOrThrow(token, type, onFail);
        return buf.readNextToken();
    }

    public SyntaxNode parse() throws LexerException, ParserException {
        Token currToken = buf.readNextToken();
        ParseResult result = parseCompUnit(currToken);
        currToken = result.getNextToken();
        matchOrThrow(currToken, null, new ParserException());  // if not reach end

        return result.getSubtree();
    }

    private ParseResult parseCompUnit(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        Token preRead = buf.readTokenByOffset(1);
        Token prePreRead = buf.readTokenByOffset(2);
        while (isMatch(currToken, LexType.CONSTTK)
                || (isMatch(currToken, LexType.INTTK)
                && isMatch(preRead, LexType.IDENFR)
                && isNotMatch(prePreRead, LexType.LPARENT))
        ) {
            result = parseDecl(currToken);
            currToken = result.getNextToken();
            preRead = buf.readTokenByOffset(1);
            prePreRead = buf.readTokenByOffset(2);
        }

        preRead = buf.readTokenByOffset(1);
        while (isMatch(currToken, LexType.VOIDTK)
                || (isMatch(currToken, LexType.INTTK)
                && isMatch(preRead, LexType.IDENFR))
        ) {
            result = parseFuncDef(currToken);
            currToken = result.getNextToken();
            preRead = buf.readTokenByOffset(1);
        }

        result = parseMainFuncDef(currToken);
        currToken = result.getNextToken();

        return new ParseResult(currToken, null);
    }

    private ParseResult parseFuncDef(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseFuncType(currToken);
        currToken = result.getNextToken();
        currToken = parseToken(currToken, LexType.IDENFR, new ParserException());
        currToken = parseToken(currToken, LexType.LPARENT, new ParserException());
        if (isMatch(currToken, LexType.INTTK)) {
            result = parseFuncFParams(currToken);
            currToken = result.getNextToken();
        }
        currToken = parseToken(currToken, LexType.RPARENT, new ParserException());
        result = parseBlock(currToken);
        currToken = result.getNextToken();

        return new ParseResult(currToken, null);
    }

    private ParseResult parseFuncFParams(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseFuncFParam(currToken);
        currToken = result.getNextToken();
        while (isMatch(currToken, LexType.COMMA)) {
            currToken = buf.readNextToken();
            result = parseFuncFParam(currToken);
            currToken = result.getNextToken();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseFuncFParam(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseBType(currToken);
        currToken = result.getNextToken();
        currToken = parseToken(currToken, LexType.IDENFR, new ParserException());
        if (isMatch(currToken, LexType.LBRACK)) {
            currToken = buf.readNextToken();
            currToken = parseToken(currToken, LexType.RBRACK, new ParserException());
            while (isMatch(currToken, LexType.LBRACK)) {
                currToken = buf.readNextToken();
                result = parseConstExp(currToken);
                currToken = result.getNextToken();
                currToken = parseToken(currToken, LexType.RBRACK, new ParserException());
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
        while (isMatch(currToken, LexType.PLUS) || isMatch(currToken, LexType.MINU)) {
            currToken = buf.readNextToken();
            result = parseMulExp(currToken);
            currToken = result.getNextToken();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseMulExp(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseUnaryExp(currToken);
        currToken = result.getNextToken();
        while (isMatch(currToken, LexType.MULT) || isMatch(currToken, LexType.DIV) || isMatch(currToken, LexType.MOD)) {
            currToken = buf.readNextToken();
            result = parseUnaryExp(currToken);
            currToken = result.getNextToken();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseUnaryExp(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        Token preRead = buf.readTokenByOffset(1);
        if (isMatch(currToken, LexType.LPARENT)
                || (isMatch(currToken, LexType.IDENFR)
                && isNotMatch(preRead, LexType.LPARENT))
                || isMatch(currToken, LexType.INTCON)
        ) {
            result = parsePrimaryExp(currToken);
            currToken = result.getNextToken();
        } else if (isMatch(currToken, LexType.IDENFR) && isMatch(preRead, LexType.LPARENT)) {
            currToken = parseToken(currToken, LexType.IDENFR, new ParserException());
            currToken = parseToken(currToken, LexType.LPARENT, new ParserException());
            if (isNotMatch(currToken, LexType.RPARENT)) {
                result = parseFuncRParams(currToken);
                currToken = result.getNextToken();
            }
            currToken = parseToken(currToken, LexType.RPARENT, new ParserException());
        } else if (isMatch(currToken, LexType.PLUS)
                || isMatch(currToken, LexType.MINU)
                || isMatch(currToken, LexType.NOT)
        ) {
            result = parseUnaryOp(currToken);
            currToken = result.getNextToken();
            result = parseUnaryExp(currToken);
            currToken = result.getNextToken();
        } else {
            throw new ParserException();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseUnaryOp(Token currToken) throws LexerException, ParserException {
        if (isMatch(currToken, LexType.PLUS)
                || isMatch(currToken, LexType.MINU)
                || isMatch(currToken, LexType.NOT)
        ) {
            currToken = buf.readNextToken();
        } else {
            throw new ParserException();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseFuncRParams(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseExp(currToken);
        currToken = result.getNextToken();
        while (isMatch(currToken, LexType.COMMA)) {
            currToken = buf.readNextToken();
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
        if (isMatch(currToken, LexType.LPARENT)) {
            currToken = buf.readNextToken();
            result = parseExp(currToken);
            currToken = result.getNextToken();
            currToken = parseToken(currToken, LexType.RPARENT, new ParserException());
        } else if (isMatch(currToken, LexType.IDENFR)) {
            result = parseLVal(currToken);
            currToken = result.getNextToken();
        } else if (isMatch(currToken, LexType.INTCON)) {
            result = parseNumber(currToken);
            currToken = result.getNextToken();
        } else {
            throw new ParserException();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseLVal(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        currToken = parseToken(currToken, LexType.IDENFR, new ParserException());
        while (isMatch(currToken, LexType.LBRACK)) {
            currToken = buf.readNextToken();
            result = parseExp(currToken);
            currToken = result.getNextToken();
            currToken = parseToken(currToken, LexType.RBRACK, new ParserException());
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseNumber(Token currToken) throws LexerException, ParserException {
        currToken = parseToken(currToken, LexType.INTCON, new ParserException());

        return new ParseResult(currToken, null);
    }

    private ParseResult parseBType(Token currToken) throws LexerException, ParserException {
        currToken = parseToken(currToken, LexType.INTTK, new ParserException());

        return new ParseResult(currToken, null);
    }

    private ParseResult parseBlock(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        currToken = parseToken(currToken, LexType.LBRACE, new ParserException());
        while (isNotMatch(currToken, LexType.RBRACE)) {
            result = parseBlockItem(currToken);
            currToken = result.getNextToken();
        }
        currToken = buf.readNextToken();

        return new ParseResult(currToken, null);
    }

    private ParseResult parseBlockItem(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        if (isMatch(currToken, LexType.INTTK) || isMatch(currToken, LexType.CONSTTK)) {
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
        Token preRead = buf.readTokenByOffset(1);
        if (isMatch(currToken, LexType.IDENFR)
                && (isMatch(preRead, LexType.LBRACK)
                || isMatch(preRead, LexType.ASSIGN))
        ) {
            result = parseLVal(currToken);
            currToken = result.getNextToken();
            currToken = parseToken(currToken, LexType.ASSIGN, new ParserException());
            if (isMatch(currToken, LexType.GETINTTK)) {
                currToken = buf.readNextToken();
                currToken = parseToken(currToken, LexType.LPARENT, new ParserException());
                currToken = parseToken(currToken, LexType.RPARENT, new ParserException());
                currToken = parseToken(currToken, LexType.SEMICN, new ParserException());
            } else if (isMatch(currToken, LexType.LPARENT)
                    || isMatch(currToken, LexType.IDENFR)
                    || isMatch(currToken, LexType.INTCON)
                    || isMatch(currToken, LexType.PLUS)
                    || isMatch(currToken, LexType.MINU)
            ) {
                result = parseExp(currToken);
                currToken = result.getNextToken();
                currToken = parseToken(currToken, LexType.SEMICN, new ParserException());
            } else {
                throw new ParserException();
            }
        } else if (isMatch(currToken, LexType.LPARENT)
                || isMatch(currToken, LexType.IDENFR)
                || isMatch(currToken, LexType.INTCON)
                || isMatch(currToken, LexType.PLUS)
                || isMatch(currToken, LexType.MINU)
        ) {
            result = parseExp(currToken);
            currToken = result.getNextToken();
            currToken = parseToken(currToken, LexType.SEMICN, new ParserException());
        } else if (isMatch(currToken, LexType.SEMICN)) {
            currToken = buf.readNextToken();
        } else if (isMatch(currToken, LexType.LBRACE)) {
            result = parseBlock(currToken);
            currToken = result.getNextToken();
        } else if (isMatch(currToken, LexType.IFTK)) {
            currToken = buf.readNextToken();
            currToken = parseToken(currToken, LexType.LPARENT, new ParserException());
            result = parseCond(currToken);
            currToken = result.getNextToken();
            currToken = parseToken(currToken, LexType.RPARENT, new ParserException());
            result = parseStmt(currToken);
            currToken = result.getNextToken();
            if (isMatch(currToken, LexType.ELSETK)) {
                currToken = buf.readNextToken();
                result = parseStmt(currToken);
                currToken = result.getNextToken();
            }
        } else if (isMatch(currToken, LexType.FORTK)) {
            currToken = buf.readNextToken();
            currToken = parseToken(currToken, LexType.LPARENT, new ParserException());
            if (isMatch(currToken, LexType.IDENFR)) {
                result = parseForStmt(currToken);
                currToken = result.getNextToken();
            }
            currToken = parseToken(currToken, LexType.SEMICN, new ParserException());
            if (isMatch(currToken, LexType.LPARENT)
                    || isMatch(currToken, LexType.IDENFR)
                    || isMatch(currToken, LexType.INTCON)
            ) {
                result = parseCond(currToken);
                currToken = result.getNextToken();
            }
            currToken = parseToken(currToken, LexType.SEMICN, new ParserException());
            if (isMatch(currToken, LexType.IDENFR)) {
                result = parseForStmt(currToken);
                currToken = result.getNextToken();
            }
            currToken = parseToken(currToken, LexType.RPARENT, new ParserException());
            result = parseStmt(currToken);
            currToken = result.getNextToken();
        } else if (isMatch(currToken, LexType.BREAKTK)) {
            currToken = buf.readNextToken();
            currToken = parseToken(currToken, LexType.SEMICN, new ParserException());
        } else if (isMatch(currToken, LexType.CONTINUETK)) {
            currToken = buf.readNextToken();
            currToken = parseToken(currToken, LexType.SEMICN, new ParserException());
        } else if (isMatch(currToken, LexType.RETURNTK)) {
            currToken = buf.readNextToken();
            if (isMatch(currToken, LexType.LPARENT)
                    || isMatch(currToken, LexType.IDENFR)
                    || isMatch(currToken, LexType.INTCON)
                    || isMatch(currToken, LexType.PLUS)
                    || isMatch(currToken, LexType.MINU)
            ) {
                result = parseExp(currToken);
                currToken = result.getNextToken();
            }
            currToken = parseToken(currToken, LexType.SEMICN, new ParserException());
        } else if (isMatch(currToken, LexType.PRINTFTK)) {
            currToken = buf.readNextToken();
            currToken = parseToken(currToken, LexType.LPARENT, new ParserException());
            currToken = parseToken(currToken, LexType.STRCON, new ParserException());
            while (isMatch(currToken, LexType.COMMA)) {
                currToken = buf.readNextToken();
                result = parseExp(currToken);
                currToken = result.getNextToken();
            }
            currToken = parseToken(currToken, LexType.RPARENT, new ParserException());
            currToken = parseToken(currToken, LexType.SEMICN, new ParserException());
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
        while (isMatch(currToken, LexType.OR)) {
            currToken = buf.readNextToken();
            result = parseLAndExp(currToken);
            currToken = result.getNextToken();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseLAndExp(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseEqExp(currToken);
        currToken = result.getNextToken();
        while (isMatch(currToken, LexType.AND)) {
            currToken = buf.readNextToken();
            result = parseEqExp(currToken);
            currToken = result.getNextToken();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseEqExp(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseRelExp(currToken);
        currToken = result.getNextToken();
        while (isMatch(currToken, LexType.EQL) || isMatch(currToken, LexType.NEQ)) {
            currToken = buf.readNextToken();
            result = parseRelExp(currToken);
            currToken = result.getNextToken();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseRelExp(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseAddExp(currToken);
        currToken = result.getNextToken();
        while (isMatch(currToken, LexType.LSS)
                || isMatch(currToken, LexType.GRE)
                || isMatch(currToken, LexType.LEQ)
                || isMatch(currToken, LexType.GEQ)
        ) {
            currToken = buf.readNextToken();
            result = parseAddExp(currToken);
            currToken = result.getNextToken();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseForStmt(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseLVal(currToken);
        currToken = result.getNextToken();
        currToken = parseToken(currToken, LexType.ASSIGN, new ParserException());
        result = parseExp(currToken);
        currToken = result.getNextToken();

        return new ParseResult(currToken, null);
    }

    private ParseResult parseFuncType(Token currToken) throws LexerException, ParserException {
        if (isMatch(currToken, LexType.VOIDTK) || isMatch(currToken, LexType.INTTK)) {
            currToken = buf.readNextToken();
        } else {
            throw new ParserException();
        }
        return new ParseResult(currToken, null);
    }

    private ParseResult parseDecl(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        if (isMatch(currToken, LexType.CONSTTK)) {
            result = parseConstDecl(currToken);
            currToken = result.getNextToken();
        } else if (isMatch(currToken, LexType.INTTK)) {
            result = parseVarDecl(currToken);
            currToken = result.getNextToken();
        } else {
            throw new ParserException();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseConstDecl(Token currToken) throws LexerException, ParserException{
        ParseResult result;
        currToken = parseToken(currToken, LexType.CONSTTK, new ParserException());
        result = parseBType(currToken);
        currToken = result.getNextToken();
        result = parseConstDef(currToken);
        currToken = result.getNextToken();
        while (isMatch(currToken, LexType.COMMA)) {
            currToken = buf.readNextToken();
            result = parseConstDef(currToken);
            currToken = result.getNextToken();
        }
        currToken = parseToken(currToken, LexType.SEMICN, new ParserException());

        return new ParseResult(currToken, null);
    }

    private ParseResult parseConstDef(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        currToken = parseToken(currToken, LexType.IDENFR, new ParserException());
        while (isMatch(currToken, LexType.LBRACK)) {
            currToken = buf.readNextToken();
            result = parseConstExp(currToken);
            currToken = result.getNextToken();
            currToken = parseToken(currToken, LexType.RBRACK, new ParserException());
        }
        currToken = parseToken(currToken, LexType.ASSIGN, new ParserException());
        result = parseConstInitVal(currToken);
        currToken = result.getNextToken();

        return new ParseResult(currToken, null);
    }

    private ParseResult parseConstInitVal(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        if (isMatch(currToken, LexType.LPARENT)
                || isMatch(currToken, LexType.IDENFR)
                || isMatch(currToken, LexType.INTCON)
                || isMatch(currToken, LexType.PLUS)
                || isMatch(currToken, LexType.MINU)
        ) {
            result = parseConstExp(currToken);
            currToken = result.getNextToken();
        } else if (isMatch(currToken, LexType.LBRACE)) {
            currToken = buf.readNextToken();
            if (isNotMatch(currToken, LexType.RBRACE)) {
                result = parseConstInitVal(currToken);
                currToken = result.getNextToken();
                while (isMatch(currToken, LexType.COMMA)) {
                    currToken = buf.readNextToken();
                    result = parseConstInitVal(currToken);
                    currToken = result.getNextToken();
                }
            }
            currToken = parseToken(currToken, LexType.RBRACE, new ParserException());
        } else {
            throw new ParserException();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseVarDecl(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseBType(currToken);
        currToken = result.getNextToken();
        result = parseVarDef(currToken);
        currToken = result.getNextToken();
        while (isMatch(currToken, LexType.COMMA)) {
            currToken = buf.readNextToken();
            result = parseVarDef(currToken);
            currToken = result.getNextToken();
        }
        currToken = parseToken(currToken, LexType.SEMICN, new ParserException());

        return new ParseResult(currToken, null);
    }

    private ParseResult parseVarDef(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        currToken = parseToken(currToken, LexType.IDENFR, new ParserException());
        while (isMatch(currToken, LexType.LBRACK)) {
            currToken = buf.readNextToken();
            result = parseConstExp(currToken);
            currToken = result.getNextToken();
            currToken = parseToken(currToken, LexType.RBRACK, new ParserException());
        }
        if (isMatch(currToken, LexType.ASSIGN)) {
            currToken = buf.readNextToken();
            result = parseInitVal(currToken);
            currToken = result.getNextToken();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseInitVal(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        if (isMatch(currToken, LexType.LPARENT)
                || isMatch(currToken, LexType.IDENFR)
                || isMatch(currToken, LexType.INTCON)
                || isMatch(currToken, LexType.PLUS)
                || isMatch(currToken, LexType.MINU)
        ) {
            result = parseExp(currToken);
            currToken = result.getNextToken();
        } else if (isMatch(currToken, LexType.LBRACE)) {
            currToken = buf.readNextToken();
            if (isNotMatch(currToken, LexType.RBRACK)) {
                result = parseInitVal(currToken);
                currToken = result.getNextToken();
                while (isMatch(currToken, LexType.COMMA)) {
                    currToken = buf.readNextToken();
                    result = parseInitVal(currToken);
                    currToken = result.getNextToken();
                }
            }
            currToken = parseToken(currToken, LexType.RBRACE, new ParserException());
        } else {
            throw new ParserException();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseMainFuncDef(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        currToken = parseToken(currToken, LexType.INTTK, new ParserException());
        currToken = parseToken(currToken, LexType.MAINTK, new ParserException());
        currToken = parseToken(currToken, LexType.LPARENT, new ParserException());
        currToken = parseToken(currToken, LexType.RPARENT, new ParserException());
        result = parseBlock(currToken);
        currToken = result.getNextToken();

        return new ParseResult(currToken, null);
    }
}


class PreReadBuffer {
    private final Lexer lexer;
    private final int tokenBufLen;
    private final Token[] tokenBuf;
    private int currTokenPos = 0;

    public PreReadBuffer(Lexer lexer, int bufLen) throws LexerException {
        assert bufLen >= 2;

        this.lexer = lexer;
        this.tokenBufLen = bufLen;
        this.tokenBuf = new Token[this.tokenBufLen];

        for (int i = 1; i < this.tokenBufLen; i++) {
            if (this.lexer.next()) {
                this.tokenBuf[i] = lexer.getToken();
            } else {
                this.tokenBuf[i] = new Token("EOF", null, 0);  // token not null
            }
        }
    }

    public Token readNextToken() throws LexerException {
        if (lexer.next()) {
            tokenBuf[currTokenPos] = lexer.getToken();
        } else {
            tokenBuf[currTokenPos] = new Token("EOF", null, 0);  // token not null
        }
        currTokenPos = (currTokenPos + 1) % tokenBufLen;
        return tokenBuf[currTokenPos];
    }

    public Token readTokenByOffset(int offset) {
        assert offset < tokenBufLen;
        return tokenBuf[(currTokenPos + offset) % tokenBufLen];
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
