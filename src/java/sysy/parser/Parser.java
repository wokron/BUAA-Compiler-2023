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

    private Token readNext() throws LexerException {
        if (lexer.next()) {
            tokenBuf[currTokenPos] = lexer.getToken();
        } else {
            tokenBuf[currTokenPos] = null;
        }
        currTokenPos = (currTokenPos + 1) % tokenBufLen;
        return tokenBuf[currTokenPos];
    }

    private Token readToken(int offset) {
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
        Token token = readNext();
        var result = parseCompUnit(token);
        token = result.getNextToken();
        if (token != null) {
            throw new ParserException();
        }
        return result.getSubtree();
    }

    private ParseResult parseCompUnit(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        Token preRead = readToken(1);
        Token prePreRead = readToken(2);
        while (currToken.getType() == LexType.CONSTTK
                || (currToken.getType() == LexType.INTTK
                && preRead.getType() == LexType.IDENFR
                && prePreRead.getType() == LexType.LPARENT)
        ) {
            result = parseDecl(currToken);
            currToken = result.getNextToken();
            preRead = readToken(1);
            prePreRead = readToken(2);
        }

        while (currToken.getType() == LexType.VOIDTK
                || (currToken.getType() == LexType.INTTK
                && preRead.getType() == LexType.IDENFR)
        ) {
            result = parseFuncDef(currToken);
            currToken = result.getNextToken();
            preRead = readToken(1);
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
        if (currToken.getType() != LexType.LPARENT) {
            throw new ParserException();
        }
        currToken = readNext();
        if (currToken.getType() == LexType.INTTK) {
            result = parseFuncFParams(currToken);
            currToken = result.getNextToken();
        }
        if (currToken.getType() != LexType.RPARENT) {
            throw new ParserException();
        }
        result = parseBlock(currToken);
        currToken = result.getNextToken();

        return new ParseResult(currToken, null);
    }

    private ParseResult parseFuncFParams(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseFuncFParam(currToken);
        currToken = result.getNextToken();
        while (currToken.getType() == LexType.COMMA) {
            currToken = readNext();
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
            currToken = readNext();
            if (currToken.getType() != LexType.RBRACK) {
                throw new ParserException();
            }
            currToken = readNext();
            while (currToken.getType() == LexType.LBRACK) {
                currToken = readNext();
                result = parseConstExp(currToken);
                currToken = result.getNextToken();
                if (currToken.getType() != LexType.RBRACK) {
                    throw new ParserException();
                }
                currToken = readNext();
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
            currToken = readNext();
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
            currToken = readNext();
            result = parseUnaryExp(currToken);
            currToken = result.getNextToken();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseUnaryExp(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        Token preRead = readToken(1);
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
            if (currToken.getType() != LexType.LPARENT) {
                throw new ParserException();
            }
            currToken = readNext();
            if (currToken.getType() != LexType.RPARENT) {
                result = parseFuncRParams(currToken);
                currToken = result.getNextToken();
            }
            if (currToken.getType() != LexType.RPARENT) {
                throw new ParserException();
            }
            currToken = readNext();
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
            currToken = readNext();
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
            currToken = readNext();
            result = parseExp(currToken);
            currToken = result.getNextToken();
            if (currToken.getType() != LexType.RPARENT) {
                throw new ParserException();
            }
            currToken = readNext();
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
            currToken = readNext();
            result = parseExp(currToken);
            currToken = result.getNextToken();
            if (currToken.getType() != LexType.RBRACK) {
                throw new ParserException();
            }
            currToken = readNext();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseNumber(Token currToken) throws LexerException, ParserException {
        if (currToken.getType() != LexType.INTCON) {
            throw new ParserException();
        }
        currToken = readNext();

        return new ParseResult(currToken, null);
    }

    private ParseResult parseBType(Token currToken) throws LexerException, ParserException {
        if (currToken.getType() != LexType.INTTK) {
            throw new ParserException();
        }
        currToken = readNext();
        return new ParseResult(currToken, null);
    }

    private ParseResult parseBlock(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        if (currToken.getType() != LexType.LBRACE) {
            throw new ParserException();
        }
        currToken = readNext();
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
        Token preRead = readToken(1);
        if (currToken.getType() == LexType.IDENFR
                && (preRead.getType() == LexType.LBRACK
                || preRead.getType() == LexType.ASSIGN)
        ) {
            result = parseLVal(currToken);
            currToken = result.getNextToken();
            if (currToken.getType() != LexType.ASSIGN) {
                throw new ParserException();
            }
            currToken = readNext();
            if (currToken.getType() == LexType.GETINTTK) {
                currToken = readNext();
                if (currToken.getType() != LexType.LPARENT) {
                    throw new ParserException();
                }
                currToken = readNext();
                if (currToken.getType() != LexType.RPARENT) {
                    throw new ParserException();
                }
                currToken = readNext();
                if (currToken.getType() != LexType.SEMICN) {
                    throw new ParserException();
                }
                currToken = readNext();
            } else if (currToken.getType() == LexType.LPARENT
                    || currToken.getType() == LexType.IDENFR
                    || currToken.getType() == LexType.INTCON
            ) {
                result = parseExp(currToken);
                currToken = result.getNextToken();
                if (currToken.getType() != LexType.SEMICN) {
                    throw new ParserException();
                }
                currToken = readNext();
            } else {
                throw new ParserException();
            }
        } else if (currToken.getType() == LexType.LPARENT
                || currToken.getType() == LexType.IDENFR
                || currToken.getType() == LexType.INTCON
        ) {
            result = parseExp(currToken);
            currToken = result.getNextToken();
            if (currToken.getType() != LexType.SEMICN) {
                throw new ParserException();
            }
            currToken = readNext();
        } else if (currToken.getType() == LexType.SEMICN) {
            currToken = readNext();
        } else if (currToken.getType() == LexType.LBRACE) {
            currToken = readNext();
            result = parseBlock(currToken);
            currToken = result.getNextToken();
        } else if (currToken.getType() == LexType.IFTK) {
            currToken = readNext();
            if (currToken.getType() != LexType.LPARENT) {
                throw new ParserException();
            }
            currToken = readNext();
            result = parseCond(currToken);
            currToken = result.getNextToken();
            if (currToken.getType() != LexType.RPARENT) {
                throw new ParserException();
            }
            currToken = readNext();
            result = parseStmt(currToken);
            currToken = result.getNextToken();
            if (currToken.getType() == LexType.ELSETK) {
                currToken = readNext();
                result = parseStmt(currToken);
                currToken = result.getNextToken();
            }
        } else if (currToken.getType() == LexType.FORTK) {
            currToken = readNext();
            if (currToken.getType() != LexType.LPARENT) {
                throw new ParserException();
            }
            currToken = readNext();
            if (currToken.getType() == LexType.IDENFR) {
                result = parseForStmt(currToken);
                currToken = result.getNextToken();
            }
            if (currToken.getType() != LexType.SEMICN) {
                throw new ParserException();
            }
            currToken = readNext();
            if (currToken.getType() == LexType.LPARENT
                    || currToken.getType() == LexType.IDENFR
                    || currToken.getType() == LexType.INTCON
            ) {
                result = parseCond(currToken);
                currToken = result.getNextToken();
            }
            if (currToken.getType() != LexType.SEMICN) {
                throw new ParserException();
            }
            currToken = readNext();
            if (currToken.getType() == LexType.IDENFR) {
                result = parseForStmt(currToken);
                currToken = result.getNextToken();
            }
            if (currToken.getType() != LexType.RPARENT) {
                throw new ParserException();
            }
            currToken = readNext();
            result = parseStmt(currToken);
            currToken = result.getNextToken();
        } else if (currToken.getType() == LexType.BREAKTK) {
            currToken = readNext();
            if (currToken.getType() != LexType.SEMICN) {
                throw new ParserException();
            }
            currToken = readNext();
        } else if (currToken.getType() == LexType.CONTINUETK) {
            currToken = readNext();
            if (currToken.getType() != LexType.SEMICN) {
                throw new ParserException();
            }
            currToken = readNext();
        } else if (currToken.getType() == LexType.RETURNTK) {
            currToken = readNext();
            if (currToken.getType() == LexType.LPARENT
                    || currToken.getType() == LexType.IDENFR
                    || currToken.getType() == LexType.INTCON
            ) {
                result = parseExp(currToken);
                currToken = result.getNextToken();
            }
            if (currToken.getType() != LexType.SEMICN) {
                throw new ParserException();
            }
            currToken = readNext();
        } else if (currToken.getType() == LexType.PRINTFTK) {
            currToken = readNext();
            if (currToken.getType() != LexType.LPARENT) {
                throw new ParserException();
            }
            currToken = readNext();
            if (currToken.getType() != LexType.STRCON) {
                throw new ParserException();
            }
            currToken = readNext();
            while (currToken.getType() == LexType.COMMA) {
                currToken = readNext();
                result = parseExp(currToken);
                currToken = result.getNextToken();
            }
            if (currToken.getType() != LexType.RPARENT) {
                throw new ParserException();
            }
            currToken = readNext();
            if (currToken.getType() != LexType.SEMICN) {
                throw new ParserException();
            }
            currToken = readNext();
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
            currToken = readNext();
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
            currToken = readNext();
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
            currToken = readNext();
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
            currToken = readNext();
            result = parseAddExp(currToken);
            currToken = result.getNextToken();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseForStmt(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseLVal(currToken);
        currToken = result.getNextToken();
        if (currToken.getType() != LexType.ASSIGN) {
            throw new ParserException();
        }
        currToken = readNext();
        result = parseExp(currToken);
        currToken = result.getNextToken();

        return new ParseResult(currToken, null);
    }

    private ParseResult parseIdent(Token currToken) throws LexerException, ParserException {
        if (currToken.getType() != LexType.IDENFR) {
            throw new ParserException();
        }
        currToken = readNext();
        return new ParseResult(currToken, null);
    }

    private ParseResult parseFuncType(Token currToken) throws LexerException, ParserException {
        if (currToken.getType() != LexType.VOIDTK || currToken.getType() != LexType.INTTK) {
            throw new ParserException();
        }
        currToken = readNext();
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
        if (currToken.getType() != LexType.CONSTTK) {
            throw new ParserException();
        }
        currToken = readNext();
        result = parseBType(currToken);
        currToken = result.getNextToken();
        result = parseConstDef(currToken);
        currToken = result.getNextToken();
        while (currToken.getType() == LexType.COMMA) {
            currToken = readNext();
            result = parseConstDef(currToken);
            currToken = result.getNextToken();
        }
        if (currToken.getType() != LexType.SEMICN) {
            throw new ParserException();
        }
        currToken = readNext();

        return new ParseResult(currToken, null);
    }

    private ParseResult parseConstDef(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseIdent(currToken);
        currToken = result.getNextToken();
        while (currToken.getType() == LexType.LBRACK) {
            currToken = readNext();
            result = parseConstExp(currToken);
            currToken = result.getNextToken();
            if (currToken.getType() != LexType.RBRACK) {
                throw new ParserException();
            }
            currToken = readNext();
        }
        if (currToken.getType() != LexType.ASSIGN) {
            throw new ParserException();
        }
        currToken = readNext();
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
            currToken = readNext();
            result = parseVarDef(currToken);
            currToken = result.getNextToken();
        }
        if (currToken.getType() != LexType.SEMICN) {
            throw new ParserException();
        }
        currToken = readNext();

        return new ParseResult(currToken, null);
    }

    private ParseResult parseVarDef(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        result = parseIdent(currToken);
        currToken = result.getNextToken();
        while (currToken.getType() == LexType.LBRACK) {
            currToken = readNext();
            result = parseConstExp(currToken);
            currToken = result.getNextToken();
            if (currToken.getType() != LexType.RBRACK) {
                throw new ParserException();
            }
            currToken = readNext();
        }

        return new ParseResult(currToken, null);
    }

    private ParseResult parseMainFuncDef(Token currToken) throws LexerException, ParserException {
        if (currToken.getType() != LexType.INTTK) {
            throw new ParserException();
        }
        currToken = readNext();
        if (currToken.getType() != LexType.MAINTK) {
            throw new ParserException();
        }
        currToken = readNext();
        if (currToken.getType() != LexType.LPARENT) {
            throw new ParserException();
        }
        currToken = readNext();
        if (currToken.getType() != LexType.RPARENT) {
            throw new ParserException();
        }
        currToken = readNext();
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
