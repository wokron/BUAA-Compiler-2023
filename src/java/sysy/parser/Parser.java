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

    private boolean match(Token token, LexType type) {
        return token.getType() == type;
    }

    private void matchOrThrow(Token token, LexType type, ParserException e) throws ParserException {
        if (!match(token, type)) {
            throw e;
        }
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
        while (currToken.getType() == LexType.CONSTTK
                || (currToken.getType() == LexType.INTTK
                && preRead.getType() == LexType.IDENFR
                && prePreRead.getType() != LexType.LPARENT)
        ) {
            result = parseDecl(currToken);
            currToken = result.getNextToken();
            preRead = buf.readTokenByOffset(1);
            prePreRead = buf.readTokenByOffset(2);
        }

        preRead = buf.readTokenByOffset(1);
        while (currToken.getType() == LexType.VOIDTK
                || (currToken.getType() == LexType.INTTK
                && preRead.getType() == LexType.IDENFR)
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
        matchOrThrow(currToken, LexType.IDENFR, new ParserException());
        currToken = buf.readNextToken();
        matchOrThrow(currToken, LexType.LPARENT, new ParserException());
        currToken = buf.readNextToken();
        if (currToken.getType() == LexType.INTTK) {
            result = parseFuncFParams(currToken);
            currToken = result.getNextToken();
        }
        matchOrThrow(currToken, LexType.RPARENT, new ParserException());
        currToken = buf.readNextToken();
        result = parseBlock(currToken);
        currToken = result.getNextToken();

        return new ParseResult(currToken, null);
    }

    private ParseResult parseFuncFParams(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseFuncFParam(currToken);
        currToken = result.getNextToken();
        while (currToken.getType() == LexType.COMMA) {
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
        matchOrThrow(currToken, LexType.IDENFR, new ParserException());
        currToken = buf.readNextToken();
        if (currToken.getType() == LexType.LBRACK) {
            currToken = buf.readNextToken();
            matchOrThrow(currToken, LexType.RBRACK, new ParserException());
            currToken = buf.readNextToken();
            while (currToken.getType() == LexType.LBRACK) {
                currToken = buf.readNextToken();
                result = parseConstExp(currToken);
                currToken = result.getNextToken();
                matchOrThrow(currToken, LexType.RBRACK, new ParserException());
                currToken = buf.readNextToken();
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
        while (currToken.getType() == LexType.MULT || currToken.getType() == LexType.DIV || currToken.getType() == LexType.MOD) {
            currToken = buf.readNextToken();
            result = parseUnaryExp(currToken);
            currToken = result.getNextToken();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseUnaryExp(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        Token preRead = buf.readTokenByOffset(1);
        if (currToken.getType() == LexType.LPARENT
                || (currToken.getType() == LexType.IDENFR
                && preRead.getType() != LexType.LPARENT)
                || currToken.getType() == LexType.INTCON
        ) {
            result = parsePrimaryExp(currToken);
            currToken = result.getNextToken();
        } else if (currToken.getType() == LexType.IDENFR && preRead.getType() == LexType.LPARENT) {
            matchOrThrow(currToken, LexType.IDENFR, new ParserException());
            currToken = buf.readNextToken();
            matchOrThrow(currToken, LexType.LPARENT, new ParserException());
            currToken = buf.readNextToken();
            if (currToken.getType() != LexType.RPARENT) {
                result = parseFuncRParams(currToken);
                currToken = result.getNextToken();
            }
            matchOrThrow(currToken, LexType.RPARENT, new ParserException());
            currToken = buf.readNextToken();
        } else if (currToken.getType() == LexType.PLUS
                || currToken.getType() == LexType.MINU
                || currToken.getType() == LexType.NOT
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
        if (currToken.getType() == LexType.PLUS
                || currToken.getType() == LexType.MINU
                || currToken.getType() == LexType.NOT
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
        while (currToken.getType() == LexType.COMMA) {
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
        if (currToken.getType() == LexType.LPARENT) {
            currToken = buf.readNextToken();
            result = parseExp(currToken);
            currToken = result.getNextToken();
            matchOrThrow(currToken, LexType.RPARENT, new ParserException());
            currToken = buf.readNextToken();
        } else if (currToken.getType() == LexType.IDENFR) {
            result = parseLVal(currToken);
            currToken = result.getNextToken();
        } else if (currToken.getType() == LexType.INTCON) {
            result = parseNumber(currToken);
            currToken = result.getNextToken();
        } else {
            throw new ParserException();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseLVal(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        matchOrThrow(currToken, LexType.IDENFR, new ParserException());
        currToken = buf.readNextToken();
        while (currToken.getType() == LexType.LBRACK) {
            currToken = buf.readNextToken();
            result = parseExp(currToken);
            currToken = result.getNextToken();
            matchOrThrow(currToken, LexType.RBRACK, new ParserException());
            currToken = buf.readNextToken();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseNumber(Token currToken) throws LexerException, ParserException {
        matchOrThrow(currToken, LexType.INTCON, new ParserException());
        currToken = buf.readNextToken();

        return new ParseResult(currToken, null);
    }

    private ParseResult parseBType(Token currToken) throws LexerException, ParserException {
        matchOrThrow(currToken, LexType.INTTK, new ParserException());
        currToken = buf.readNextToken();

        return new ParseResult(currToken, null);
    }

    private ParseResult parseBlock(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        matchOrThrow(currToken, LexType.LBRACE, new ParserException());
        currToken = buf.readNextToken();
        while (currToken.getType() != LexType.RBRACE) {
            result = parseBlockItem(currToken);
            currToken = result.getNextToken();
        }
        currToken = buf.readNextToken();

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
        Token preRead = buf.readTokenByOffset(1);
        if (currToken.getType() == LexType.IDENFR
                && (preRead.getType() == LexType.LBRACK
                || preRead.getType() == LexType.ASSIGN)
        ) {
            result = parseLVal(currToken);
            currToken = result.getNextToken();
            matchOrThrow(currToken, LexType.ASSIGN, new ParserException());
            currToken = buf.readNextToken();
            if (currToken.getType() == LexType.GETINTTK) {
                currToken = buf.readNextToken();
                matchOrThrow(currToken, LexType.LPARENT, new ParserException());
                currToken = buf.readNextToken();
                matchOrThrow(currToken, LexType.RPARENT, new ParserException());
                currToken = buf.readNextToken();
                matchOrThrow(currToken, LexType.SEMICN, new ParserException());
                currToken = buf.readNextToken();
            } else if (currToken.getType() == LexType.LPARENT
                    || currToken.getType() == LexType.IDENFR
                    || currToken.getType() == LexType.INTCON
                    || currToken.getType() == LexType.PLUS
                    || currToken.getType() == LexType.MINU
            ) {
                result = parseExp(currToken);
                currToken = result.getNextToken();
                matchOrThrow(currToken, LexType.SEMICN, new ParserException());
                currToken = buf.readNextToken();
            } else {
                throw new ParserException();
            }
        } else if (currToken.getType() == LexType.LPARENT
                || currToken.getType() == LexType.IDENFR
                || currToken.getType() == LexType.INTCON
                || currToken.getType() == LexType.PLUS
                || currToken.getType() == LexType.MINU
        ) {
            result = parseExp(currToken);
            currToken = result.getNextToken();
            matchOrThrow(currToken, LexType.SEMICN, new ParserException());
            currToken = buf.readNextToken();
        } else if (currToken.getType() == LexType.SEMICN) {
            currToken = buf.readNextToken();
        } else if (currToken.getType() == LexType.LBRACE) {
            result = parseBlock(currToken);
            currToken = result.getNextToken();
        } else if (currToken.getType() == LexType.IFTK) {
            currToken = buf.readNextToken();
            matchOrThrow(currToken, LexType.LPARENT, new ParserException());
            currToken = buf.readNextToken();
            result = parseCond(currToken);
            currToken = result.getNextToken();
            matchOrThrow(currToken, LexType.RPARENT, new ParserException());
            currToken = buf.readNextToken();
            result = parseStmt(currToken);
            currToken = result.getNextToken();
            if (currToken.getType() == LexType.ELSETK) {
                currToken = buf.readNextToken();
                result = parseStmt(currToken);
                currToken = result.getNextToken();
            }
        } else if (currToken.getType() == LexType.FORTK) {
            currToken = buf.readNextToken();
            matchOrThrow(currToken, LexType.LPARENT, new ParserException());
            currToken = buf.readNextToken();
            if (currToken.getType() == LexType.IDENFR) {
                result = parseForStmt(currToken);
                currToken = result.getNextToken();
            }
            matchOrThrow(currToken, LexType.SEMICN, new ParserException());
            currToken = buf.readNextToken();
            if (currToken.getType() == LexType.LPARENT
                    || currToken.getType() == LexType.IDENFR
                    || currToken.getType() == LexType.INTCON
            ) {
                result = parseCond(currToken);
                currToken = result.getNextToken();
            }
            matchOrThrow(currToken, LexType.SEMICN, new ParserException());
            currToken = buf.readNextToken();
            if (currToken.getType() == LexType.IDENFR) {
                result = parseForStmt(currToken);
                currToken = result.getNextToken();
            }
            matchOrThrow(currToken, LexType.RPARENT, new ParserException());
            currToken = buf.readNextToken();
            result = parseStmt(currToken);
            currToken = result.getNextToken();
        } else if (currToken.getType() == LexType.BREAKTK) {
            currToken = buf.readNextToken();
            matchOrThrow(currToken, LexType.SEMICN, new ParserException());
            currToken = buf.readNextToken();
        } else if (currToken.getType() == LexType.CONTINUETK) {
            currToken = buf.readNextToken();
            matchOrThrow(currToken, LexType.SEMICN, new ParserException());
            currToken = buf.readNextToken();
        } else if (currToken.getType() == LexType.RETURNTK) {
            currToken = buf.readNextToken();
            if (currToken.getType() == LexType.LPARENT
                    || currToken.getType() == LexType.IDENFR
                    || currToken.getType() == LexType.INTCON
                    || currToken.getType() == LexType.PLUS
                    || currToken.getType() == LexType.MINU
            ) {
                result = parseExp(currToken);
                currToken = result.getNextToken();
            }
            matchOrThrow(currToken, LexType.SEMICN, new ParserException());
            currToken = buf.readNextToken();
        } else if (currToken.getType() == LexType.PRINTFTK) {
            currToken = buf.readNextToken();
            matchOrThrow(currToken, LexType.LPARENT, new ParserException());
            currToken = buf.readNextToken();
            matchOrThrow(currToken, LexType.STRCON, new ParserException());
            currToken = buf.readNextToken();
            while (currToken.getType() == LexType.COMMA) {
                currToken = buf.readNextToken();
                result = parseExp(currToken);
                currToken = result.getNextToken();
            }
            matchOrThrow(currToken, LexType.RPARENT, new ParserException());
            currToken = buf.readNextToken();
            matchOrThrow(currToken, LexType.SEMICN, new ParserException());
            currToken = buf.readNextToken();
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
        while (currToken.getType() == LexType.AND) {
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
        while (currToken.getType() == LexType.EQL || currToken.getType() == LexType.NEQ) {
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
        while (currToken.getType() == LexType.LSS
                || currToken.getType() == LexType.GRE
                || currToken.getType() == LexType.LEQ
                || currToken.getType() == LexType.GEQ
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
        matchOrThrow(currToken, LexType.ASSIGN, new ParserException());
        currToken = buf.readNextToken();
        result = parseExp(currToken);
        currToken = result.getNextToken();

        return new ParseResult(currToken, null);
    }

    private ParseResult parseFuncType(Token currToken) throws LexerException, ParserException {
        if (currToken.getType() == LexType.VOIDTK || currToken.getType() == LexType.INTTK) {
            currToken = buf.readNextToken();
        } else {
            throw new ParserException();
        }
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
        currToken = buf.readNextToken();
        result = parseBType(currToken);
        currToken = result.getNextToken();
        result = parseConstDef(currToken);
        currToken = result.getNextToken();
        while (currToken.getType() == LexType.COMMA) {
            currToken = buf.readNextToken();
            result = parseConstDef(currToken);
            currToken = result.getNextToken();
        }
        matchOrThrow(currToken, LexType.SEMICN, new ParserException());
        currToken = buf.readNextToken();

        return new ParseResult(currToken, null);
    }

    private ParseResult parseConstDef(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        matchOrThrow(currToken, LexType.IDENFR, new ParserException());
        currToken = buf.readNextToken();
        while (currToken.getType() == LexType.LBRACK) {
            currToken = buf.readNextToken();
            result = parseConstExp(currToken);
            currToken = result.getNextToken();
            matchOrThrow(currToken, LexType.RBRACK, new ParserException());
            currToken = buf.readNextToken();
        }
        matchOrThrow(currToken, LexType.ASSIGN, new ParserException());
        currToken = buf.readNextToken();
        result = parseConstInitVal(currToken);
        currToken = result.getNextToken();

        return new ParseResult(currToken, null);
    }

    private ParseResult parseConstInitVal(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        if (currToken.getType() == LexType.LPARENT
                || currToken.getType() == LexType.IDENFR
                || currToken.getType() == LexType.INTCON
                || currToken.getType() == LexType.PLUS
                || currToken.getType() == LexType.MINU
        ) {
            result = parseConstExp(currToken);
            currToken = result.getNextToken();
        } else if (currToken.getType() == LexType.LBRACE) {
            currToken = buf.readNextToken();
            if (currToken.getType() != LexType.RBRACE) {
                result = parseConstInitVal(currToken);
                currToken = result.getNextToken();
                while (currToken.getType() == LexType.COMMA) {
                    currToken = buf.readNextToken();
                    result = parseConstInitVal(currToken);
                    currToken = result.getNextToken();
                }
            }
            matchOrThrow(currToken, LexType.RBRACE, new ParserException());
            currToken = buf.readNextToken();
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
        while (currToken.getType() == LexType.COMMA) {
            currToken = buf.readNextToken();
            result = parseVarDef(currToken);
            currToken = result.getNextToken();
        }
        matchOrThrow(currToken, LexType.SEMICN, new ParserException());
        currToken = buf.readNextToken();

        return new ParseResult(currToken, null);
    }

    private ParseResult parseVarDef(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        matchOrThrow(currToken, LexType.IDENFR, new ParserException());
        currToken = buf.readNextToken();
        while (currToken.getType() == LexType.LBRACK) {
            currToken = buf.readNextToken();
            result = parseConstExp(currToken);
            currToken = result.getNextToken();
            matchOrThrow(currToken, LexType.RBRACK, new ParserException());
            currToken = buf.readNextToken();
        }
        if (currToken.getType() == LexType.ASSIGN) {
            currToken = buf.readNextToken();
            result = parseInitVal(currToken);
            currToken = result.getNextToken();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseInitVal(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        if (currToken.getType() == LexType.LPARENT
                || currToken.getType() == LexType.IDENFR
                || currToken.getType() == LexType.INTCON
                || currToken.getType() == LexType.PLUS
                || currToken.getType() == LexType.MINU
        ) {
            result = parseExp(currToken);
            currToken = result.getNextToken();
        } else if (currToken.getType() == LexType.LBRACE) {
            currToken = buf.readNextToken();
            if (currToken.getType() != LexType.RBRACK) {
                result = parseInitVal(currToken);
                currToken = result.getNextToken();
                while (currToken.getType() == LexType.COMMA) {
                    currToken = buf.readNextToken();
                    result = parseInitVal(currToken);
                    currToken = result.getNextToken();
                }
            }
            matchOrThrow(currToken, LexType.RBRACE, new ParserException());
            currToken = buf.readNextToken();
        } else {
            throw new ParserException();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseMainFuncDef(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        matchOrThrow(currToken, LexType.INTTK, new ParserException());
        currToken = buf.readNextToken();
        matchOrThrow(currToken, LexType.MAINTK, new ParserException());
        currToken = buf.readNextToken();
        matchOrThrow(currToken, LexType.LPARENT, new ParserException());
        currToken = buf.readNextToken();
        matchOrThrow(currToken, LexType.RPARENT, new ParserException());
        currToken = buf.readNextToken();
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
                this.tokenBuf[i] = new Token("EOF", null);  // token not null
            }
        }
    }

    public Token readNextToken() throws LexerException {
        if (lexer.next()) {
            tokenBuf[currTokenPos] = lexer.getToken();
        } else {
            tokenBuf[currTokenPos] = new Token("EOF", null);  // token not null
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
