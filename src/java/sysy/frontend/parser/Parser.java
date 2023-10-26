package sysy.frontend.parser;

import sysy.error.CompileErrorType;
import sysy.error.ErrorRecorder;
import sysy.exception.LexerException;
import sysy.exception.ParserException;
import sysy.frontend.parser.syntaxtree.*;
import sysy.frontend.lexer.LexType;
import sysy.frontend.lexer.Lexer;
import sysy.frontend.lexer.Token;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

public class Parser {
    private final PreReadBuffer buf;
    private final ErrorRecorder errorRecorder;

    public Parser(Lexer lexer, ErrorRecorder errorRecorder) throws LexerException {
        this.buf = new PreReadBuffer(lexer, 3);
        this.errorRecorder = errorRecorder;
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
        CompUnitNode subTree = new CompUnitNode();
        ParseResult result;
        Token preRead, prePreRead;

        preRead = buf.readTokenByOffset(1);
        prePreRead = buf.readTokenByOffset(2);
        while (isMatch(currToken, LexType.CONSTTK)
                || (isMatch(currToken, LexType.INTTK)
                && isMatch(preRead, LexType.IDENFR)
                && isNotMatch(prePreRead, LexType.LPARENT))
        ) {
            result = parseDecl(currToken);
            currToken = result.getNextToken();
            subTree.declares.add((DeclNode) result.getSubtree());

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
            subTree.funcs.add((FuncDefNode) result.getSubtree());

            preRead = buf.readTokenByOffset(1);
        }

        result = parseMainFuncDef(currToken);
        currToken = result.getNextToken();
        subTree.mainFunc = (MainFuncDefNode) result.getSubtree();

        return new ParseResult(currToken, subTree);
    }

    private ParseResult parseFuncDef(Token currToken) throws LexerException, ParserException {
        FuncDefNode subTree = new FuncDefNode();
        ParseResult result;

        result = parseFuncType(currToken);
        currToken = result.getNextToken();
        subTree.funcType = (FuncTypeNode) result.getSubtree();

        subTree.ident = currToken.getValue();
        subTree.identLineNum = currToken.getLineNum();
        currToken = parseToken(currToken, LexType.IDENFR, new ParserException());
        currToken = parseToken(currToken, LexType.LPARENT, new ParserException());
        if (isMatch(currToken, LexType.INTTK)) {
            result = parseFuncFParams(currToken);
            currToken = result.getNextToken();
            subTree.params = (FuncFParamsNode) result.getSubtree();
        }

        if (isMatch(currToken, LexType.RPARENT)) {
            currToken = buf.readNextToken();
        } else {
            errorRecorder.addError(CompileErrorType.RPARENT_IS_MISSING, buf.readPreToken().getLineNum());
        }

        result = parseBlock(currToken);
        subTree.block = (BlockNode) result.getSubtree();

        currToken = result.getNextToken();

        return new ParseResult(currToken, subTree);
    }

    private ParseResult parseFuncFParams(Token currToken) throws LexerException, ParserException {
        ParseResult result;
        FuncFParamsNode subTree = new FuncFParamsNode();

        result = parseFuncFParam(currToken);
        currToken = result.getNextToken();
        subTree.params.add((FuncFParamNode) result.getSubtree());

        while (isMatch(currToken, LexType.COMMA)) {
            currToken = buf.readNextToken();
            result = parseFuncFParam(currToken);
            subTree.params.add((FuncFParamNode) result.getSubtree());

            currToken = result.getNextToken();
        }

        return new ParseResult(currToken, subTree);
    }

    private ParseResult parseFuncFParam(Token currToken) throws LexerException, ParserException {
        FuncFParamNode subTree = new FuncFParamNode();
        ParseResult result;

        result = parseBType(currToken);
        currToken = result.getNextToken();
        subTree.type = (BTypeNode) result.getSubtree();

        subTree.ident = currToken.getValue();
        subTree.identLineNum = currToken.getLineNum();
        currToken = parseToken(currToken, LexType.IDENFR, new ParserException());
        if (isMatch(currToken, LexType.LBRACK)) {
            subTree.dimensions = new ArrayList<>();
            currToken = buf.readNextToken();

            if (isMatch(currToken, LexType.RBRACK)) {
                currToken = buf.readNextToken();
            } else {
                errorRecorder.addError(CompileErrorType.RBRACK_IS_MISSING, buf.readPreToken().getLineNum());
            }

            while (isMatch(currToken, LexType.LBRACK)) {
                currToken = buf.readNextToken();
                result = parseConstExp(currToken);
                subTree.dimensions.add((ConstExpNode) result.getSubtree());

                currToken = result.getNextToken();
                if (isMatch(currToken, LexType.RBRACK)) {
                    currToken = buf.readNextToken();
                } else {
                    errorRecorder.addError(CompileErrorType.RBRACK_IS_MISSING, buf.readPreToken().getLineNum());
                }
            }
        }

        return new ParseResult(currToken, subTree);
    }

    private ParseResult parseConstExp(Token currToken) throws LexerException, ParserException {
        ConstExpNode subTree = new ConstExpNode();
        ParseResult result;

        result = parseAddExp(currToken);
        currToken = result.getNextToken();
        subTree.addExp = (AddExpNode) result.getSubtree();

        return new ParseResult(currToken, subTree);
    }

    private ParseResult parseAddExp(Token currToken) throws LexerException, ParserException {
        AddExpNode subTree = new AddExpNodeForSingle();
        ParseResult result;

        result = parseMulExp(currToken);
        currToken = result.getNextToken();
        ((AddExpNodeForSingle) subTree).mulExp = (MulExpNode) result.getSubtree();

        while (isMatch(currToken, LexType.PLUS) || isMatch(currToken, LexType.MINU)) {
            LexType op = currToken.getType();

            currToken = buf.readNextToken();
            result = parseMulExp(currToken);
            AddExpNodeForDouble newNode = new AddExpNodeForDouble();
            newNode.mulExp = (MulExpNode) result.getSubtree();
            newNode.addExp = subTree;
            newNode.op = op;
            subTree = newNode;

            currToken = result.getNextToken();
        }

        return new ParseResult(currToken, subTree);
    }

    private ParseResult parseMulExp(Token currToken) throws LexerException, ParserException {
        MulExpNode subTree = new MulExpNodeForSingle();
        ParseResult result;

        result = parseUnaryExp(currToken);
        currToken = result.getNextToken();
        ((MulExpNodeForSingle) subTree).unaryExp = (UnaryExpNode) result.getSubtree();

        while (isMatch(currToken, LexType.MULT) || isMatch(currToken, LexType.DIV) || isMatch(currToken, LexType.MOD)) {
            LexType op = currToken.getType();

            currToken = buf.readNextToken();
            result = parseUnaryExp(currToken);
            MulExpNodeForDouble newNode = new MulExpNodeForDouble();
            newNode.unaryExp = (UnaryExpNode) result.getSubtree();
            newNode.mulExp = subTree;
            newNode.op = op;
            subTree = newNode;

            currToken = result.getNextToken();
        }

        return new ParseResult(currToken, subTree);
    }

    private ParseResult parseUnaryExp(Token currToken) throws LexerException, ParserException {
        UnaryExpNode subTree;
        ParseResult result;
        Token preRead = buf.readTokenByOffset(1);
        if (isMatch(currToken, LexType.LPARENT)
                || (isMatch(currToken, LexType.IDENFR)
                && isNotMatch(preRead, LexType.LPARENT))
                || isMatch(currToken, LexType.INTCON)
        ) {
            var newNode = new UnaryExpNodeForPrimaryExp();

            result = parsePrimaryExp(currToken);
            currToken = result.getNextToken();
            newNode.primaryExp = (PrimaryExpNode) result.getSubtree();

            subTree = newNode;
        } else if (isMatch(currToken, LexType.IDENFR) && isMatch(preRead, LexType.LPARENT)) {
            var newNode = new UnaryExpNodeForFuncCall();
            newNode.ident = currToken.getValue();
            newNode.identLineNum = currToken.getLineNum();

            currToken = parseToken(currToken, LexType.IDENFR, new ParserException());
            currToken = parseToken(currToken, LexType.LPARENT, new ParserException());
            if (isMatch(currToken, LexType.LPARENT)
                    || isMatch(currToken, LexType.IDENFR)
                    || isMatch(currToken, LexType.INTCON)
                    || isMatch(currToken, LexType.PLUS)
                    || isMatch(currToken, LexType.MINU)
            ) {
                result = parseFuncRParams(currToken);
                currToken = result.getNextToken();
                newNode.params = (FuncRParamsNode) result.getSubtree();
            }

            if (isMatch(currToken, LexType.RPARENT)) {
                currToken = buf.readNextToken();
            } else {
                errorRecorder.addError(CompileErrorType.RPARENT_IS_MISSING, buf.readPreToken().getLineNum());
            }

            subTree = newNode;
        } else if (isMatch(currToken, LexType.PLUS)
                || isMatch(currToken, LexType.MINU)
                || isMatch(currToken, LexType.NOT)
        ) {
            var newNode = new UnaryExpNodeForUnaryOp();

            result = parseUnaryOp(currToken);
            currToken = result.getNextToken();
            newNode.op = (UnaryOpNode) result.getSubtree();

            result = parseUnaryExp(currToken);
            currToken = result.getNextToken();
            newNode.exp = (UnaryExpNode) result.getSubtree();

            subTree = newNode;
        } else {
            throw new ParserException();
        }

        return new ParseResult(currToken, subTree);
    }

    private ParseResult parseUnaryOp(Token currToken) throws LexerException, ParserException {
        UnaryOpNode subTree = new UnaryOpNode();

        if (isMatch(currToken, LexType.PLUS)
                || isMatch(currToken, LexType.MINU)
                || isMatch(currToken, LexType.NOT)
        ) {
            subTree.opType = currToken.getType();
            currToken = buf.readNextToken();
        } else {
            throw new ParserException();
        }

        return new ParseResult(currToken, subTree);
    }

    private ParseResult parseFuncRParams(Token currToken) throws LexerException, ParserException {
        FuncRParamsNode subTree = new FuncRParamsNode();
        ParseResult result;

        result = parseExp(currToken);
        currToken = result.getNextToken();
        subTree.exps.add((ExpNode) result.getSubtree());

        while (isMatch(currToken, LexType.COMMA)) {
            currToken = buf.readNextToken();

            result = parseExp(currToken);
            currToken = result.getNextToken();
            subTree.exps.add((ExpNode) result.getSubtree());
        }

        return new ParseResult(currToken, subTree);
    }

    private ParseResult parseExp(Token currToken) throws LexerException, ParserException {
        ExpNode subTree = new ExpNode();
        ParseResult result;

        result = parseAddExp(currToken);
        currToken = result.getNextToken();
        subTree.addExp = (AddExpNode) result.getSubtree();

        return new ParseResult(currToken, subTree);
    }

    private ParseResult parsePrimaryExp(Token currToken) throws LexerException, ParserException {
        PrimaryExpNode subTree;
        ParseResult result;

        if (isMatch(currToken, LexType.LPARENT)) {
            currToken = buf.readNextToken();

            var newNode = new PrimaryExpNodeForExp();
            result = parseExp(currToken);
            currToken = result.getNextToken();
            newNode.exp = (ExpNode) result.getSubtree();
            subTree = newNode;

            if (isMatch(currToken, LexType.RPARENT)) {
                currToken = buf.readNextToken();
            } else {
                errorRecorder.addError(CompileErrorType.RPARENT_IS_MISSING, buf.readPreToken().getLineNum());
            }

        } else if (isMatch(currToken, LexType.IDENFR)) {
            var newNode = new PrimaryExpNodeForLVal();
            result = parseLVal(currToken);
            currToken = result.getNextToken();
            newNode.lVal = (LValNode) result.getSubtree();
            subTree = newNode;
        } else if (isMatch(currToken, LexType.INTCON)) {
            var newNode = new PrimaryExpNodeForNumber();
            result = parseNumber(currToken);
            currToken = result.getNextToken();
            newNode.number = (NumberNode) result.getSubtree();
            subTree = newNode;
        } else {
            throw new ParserException();
        }

        return new ParseResult(currToken, subTree);
    }

    private ParseResult parseLVal(Token currToken) throws LexerException, ParserException {
        LValNode subTree = new LValNode();
        ParseResult result;

        subTree.ident = currToken.getValue();
        subTree.identLineNum = currToken.getLineNum();
        currToken = parseToken(currToken, LexType.IDENFR, new ParserException());
        while (isMatch(currToken, LexType.LBRACK)) {
            currToken = buf.readNextToken();

            result = parseExp(currToken);
            currToken = result.getNextToken();
            subTree.dimensions.add((ExpNode) result.getSubtree());

            if (isMatch(currToken, LexType.RBRACK)) {
                currToken = buf.readNextToken();
            } else {
                errorRecorder.addError(CompileErrorType.RBRACK_IS_MISSING, buf.readPreToken().getLineNum());
            }
        }

        return new ParseResult(currToken, subTree);
    }

    private ParseResult parseNumber(Token currToken) throws LexerException, ParserException {
        NumberNode subTree = new NumberNode();
        subTree.intConst = currToken.getValue();
        currToken = parseToken(currToken, LexType.INTCON, new ParserException());

        return new ParseResult(currToken, subTree);
    }

    private ParseResult parseBType(Token currToken) throws LexerException, ParserException {
        BTypeNode subTree = new BTypeNode();

        subTree.type = currToken.getType();
        currToken = parseToken(currToken, LexType.INTTK, new ParserException());

        return new ParseResult(currToken, subTree);
    }

    private ParseResult parseBlock(Token currToken) throws LexerException, ParserException {
        BlockNode subTree = new BlockNode();
        ParseResult result;

        currToken = parseToken(currToken, LexType.LBRACE, new ParserException());
        while (isNotMatch(currToken, LexType.RBRACE)) {
            result = parseBlockItem(currToken);
            currToken = result.getNextToken();
            subTree.blockItems.add((BlockItemNode) result.getSubtree());
        }
        subTree.blockRLineNum = currToken.getLineNum();
        currToken = buf.readNextToken();

        return new ParseResult(currToken, subTree);
    }

    private ParseResult parseBlockItem(Token currToken) throws LexerException, ParserException {
        BlockItemNode subTree;
        ParseResult result;

        if (isMatch(currToken, LexType.INTTK) || isMatch(currToken, LexType.CONSTTK)) {
            var newNode = new BlockItemNodeForDecl();

            result = parseDecl(currToken);
            currToken = result.getNextToken();
            newNode.decl = (DeclNode) result.getSubtree();

            subTree = newNode;
        } else {
            var newNode = new BlockItemNodeForStmt();

            result = parseStmt(currToken);
            currToken = result.getNextToken();
            newNode.stmt = (StmtNode) result.getSubtree();

            subTree = newNode;
        }

        return new ParseResult(currToken, subTree);
    }

    private ParseResult parseStmt(Token currToken) throws LexerException, ParserException {
        StmtNode subTree;
        ParseResult result;

        Token preRead = buf.readTokenByOffset(1);
        if (isMatch(currToken, LexType.IDENFR)
                && (buf.findUntil(LexType.ASSIGN, LexType.SEMICN))
        ) {
            LValNode tmpLVal;
            result = parseLVal(currToken);
            currToken = result.getNextToken();
            tmpLVal = (LValNode) result.getSubtree();

            currToken = parseToken(currToken, LexType.ASSIGN, new ParserException());
            if (isMatch(currToken, LexType.GETINTTK)) {
                var newNode = new StmtNodeForGetInt();
                newNode.lVal = tmpLVal;

                currToken = buf.readNextToken();
                currToken = parseToken(currToken, LexType.LPARENT, new ParserException());

                if (isMatch(currToken, LexType.RPARENT)) {
                    currToken = buf.readNextToken();
                } else {
                    errorRecorder.addError(CompileErrorType.RPARENT_IS_MISSING, buf.readPreToken().getLineNum());
                }

                if (isMatch(currToken, LexType.SEMICN)) {
                    currToken = buf.readNextToken();
                } else {
                    errorRecorder.addError(CompileErrorType.SEMICN_IS_MISSING, buf.readPreToken().getLineNum());
                }

                subTree = newNode;
            } else if (isMatch(currToken, LexType.LPARENT)
                    || isMatch(currToken, LexType.IDENFR)
                    || isMatch(currToken, LexType.INTCON)
                    || isMatch(currToken, LexType.PLUS)
                    || isMatch(currToken, LexType.MINU)
            ) {
                var newNode = new StmtNodeForAssign();
                result = parseExp(currToken);
                currToken = result.getNextToken();
                newNode.exp = (ExpNode) result.getSubtree();
                newNode.lVal = tmpLVal;

                if (isMatch(currToken, LexType.SEMICN)) {
                    currToken = buf.readNextToken();
                } else {
                    errorRecorder.addError(CompileErrorType.SEMICN_IS_MISSING, buf.readPreToken().getLineNum());
                }

                subTree = newNode;
            } else {
                throw new ParserException();
            }
        } else if (isMatch(currToken, LexType.LPARENT)
                || isMatch(currToken, LexType.IDENFR)
                || isMatch(currToken, LexType.INTCON)
                || isMatch(currToken, LexType.PLUS)
                || isMatch(currToken, LexType.MINU)
        ) {
            var newNode = new StmtNodeForExp();
            result = parseExp(currToken);
            currToken = result.getNextToken();
            newNode.exp = (ExpNode) result.getSubtree();

            if (isMatch(currToken, LexType.SEMICN)) {
                currToken = buf.readNextToken();
            } else {
                errorRecorder.addError(CompileErrorType.SEMICN_IS_MISSING, buf.readPreToken().getLineNum());
            }

            subTree = newNode;
        } else if (isMatch(currToken, LexType.SEMICN)) {
            currToken = buf.readNextToken();
            subTree = new StmtNodeForExp();
        } else if (isMatch(currToken, LexType.LBRACE)) {
            var newNode = new StmtNodeForBlock();

            result = parseBlock(currToken);
            currToken = result.getNextToken();
            newNode.block = (BlockNode) result.getSubtree();

            subTree = newNode;
        } else if (isMatch(currToken, LexType.IFTK)) {
            var newNode = new StmtNodeForIfElse();

            currToken = buf.readNextToken();
            currToken = parseToken(currToken, LexType.LPARENT, new ParserException());

            result = parseCond(currToken);
            currToken = result.getNextToken();
            newNode.cond = (CondNode) result.getSubtree();

            if (isMatch(currToken, LexType.RPARENT)) {
                currToken = buf.readNextToken();
            } else {
                errorRecorder.addError(CompileErrorType.RPARENT_IS_MISSING, buf.readPreToken().getLineNum());
            }

            result = parseStmt(currToken);
            currToken = result.getNextToken();
            newNode.ifStmt = (StmtNode) result.getSubtree();

            if (isMatch(currToken, LexType.ELSETK)) {
                currToken = buf.readNextToken();

                result = parseStmt(currToken);
                currToken = result.getNextToken();
                newNode.elseStmt = (StmtNode) result.getSubtree();
            }

            subTree = newNode;
        } else if (isMatch(currToken, LexType.FORTK)) {
            var newNode = new StmtNodeForLoop();

            currToken = buf.readNextToken();
            currToken = parseToken(currToken, LexType.LPARENT, new ParserException());
            if (isMatch(currToken, LexType.IDENFR)) {
                result = parseForStmt(currToken);
                currToken = result.getNextToken();
                newNode.forStmt1 = (ForStmtNode) result.getSubtree();
            }

            if (isMatch(currToken, LexType.SEMICN)) {
                currToken = buf.readNextToken();
            } else {
                errorRecorder.addError(CompileErrorType.SEMICN_IS_MISSING, buf.readPreToken().getLineNum());
            }

            if (isMatch(currToken, LexType.LPARENT)
                    || isMatch(currToken, LexType.IDENFR)
                    || isMatch(currToken, LexType.INTCON)
                    || isMatch(currToken, LexType.PLUS)
                    || isMatch(currToken, LexType.MINU)
                    || isMatch(currToken, LexType.NOT)
            ) {
                result = parseCond(currToken);
                currToken = result.getNextToken();
                newNode.cond = (CondNode) result.getSubtree();
            }

            if (isMatch(currToken, LexType.SEMICN)) {
                currToken = buf.readNextToken();
            } else {
                errorRecorder.addError(CompileErrorType.SEMICN_IS_MISSING, buf.readPreToken().getLineNum());
            }

            if (isMatch(currToken, LexType.IDENFR)) {
                result = parseForStmt(currToken);
                currToken = result.getNextToken();
                newNode.forStmt2 = (ForStmtNode) result.getSubtree();
            }

            if (isMatch(currToken, LexType.RPARENT)) {
                currToken = buf.readNextToken();
            } else {
                errorRecorder.addError(CompileErrorType.RPARENT_IS_MISSING, buf.readPreToken().getLineNum());
            }

            result = parseStmt(currToken);
            currToken = result.getNextToken();
            newNode.stmt = (StmtNode) result.getSubtree();

            subTree = newNode;
        } else if (isMatch(currToken, LexType.BREAKTK)) {
            var newNode = new StmtNodeForContinueBreak();
            newNode.tkLineNum = currToken.getLineNum();
            newNode.type = currToken.getType();

            currToken = buf.readNextToken();

            if (isMatch(currToken, LexType.SEMICN)) {
                currToken = buf.readNextToken();
            } else {
                errorRecorder.addError(CompileErrorType.SEMICN_IS_MISSING, buf.readPreToken().getLineNum());
            }

            subTree = newNode;
        } else if (isMatch(currToken, LexType.CONTINUETK)) {
            var newNode = new StmtNodeForContinueBreak();
            newNode.tkLineNum = currToken.getLineNum();
            newNode.type = currToken.getType();

            currToken = buf.readNextToken();

            if (isMatch(currToken, LexType.SEMICN)) {
                currToken = buf.readNextToken();
            } else {
                errorRecorder.addError(CompileErrorType.SEMICN_IS_MISSING, buf.readPreToken().getLineNum());
            }

            subTree = newNode;
        } else if (isMatch(currToken, LexType.RETURNTK)) {
            var newNode = new StmtNodeForReturn();
            newNode.returnLineNum = currToken.getLineNum();

            currToken = buf.readNextToken();
            if (isMatch(currToken, LexType.LPARENT)
                    || isMatch(currToken, LexType.IDENFR)
                    || isMatch(currToken, LexType.INTCON)
                    || isMatch(currToken, LexType.PLUS)
                    || isMatch(currToken, LexType.MINU)
            ) {
                result = parseExp(currToken);
                currToken = result.getNextToken();
                newNode.exp = (ExpNode) result.getSubtree();
            }

            if (isMatch(currToken, LexType.SEMICN)) {
                currToken = buf.readNextToken();
            } else {
                errorRecorder.addError(CompileErrorType.SEMICN_IS_MISSING, buf.readPreToken().getLineNum());
            }

            subTree = newNode;
        } else if (isMatch(currToken, LexType.PRINTFTK)) {
            var newNode = new StmtNodeForPrintf();
            newNode.printfLineNum = currToken.getLineNum();

            currToken = buf.readNextToken();
            currToken = parseToken(currToken, LexType.LPARENT, new ParserException());

            newNode.formatString = currToken.getValue();
            currToken = parseToken(currToken, LexType.STRCON, new ParserException());
            while (isMatch(currToken, LexType.COMMA)) {
                currToken = buf.readNextToken();

                result = parseExp(currToken);
                currToken = result.getNextToken();
                newNode.exps.add((ExpNode) result.getSubtree());
            }

            if (isMatch(currToken, LexType.RPARENT)) {
                currToken = buf.readNextToken();
            } else {
                errorRecorder.addError(CompileErrorType.RPARENT_IS_MISSING, buf.readPreToken().getLineNum());
            }

            if (isMatch(currToken, LexType.SEMICN)) {
                currToken = buf.readNextToken();
            } else {
                errorRecorder.addError(CompileErrorType.SEMICN_IS_MISSING, buf.readPreToken().getLineNum());
            }

            subTree = newNode;
        } else {
            throw new ParserException();
        }

        return new ParseResult(currToken, subTree);
    }

    private ParseResult parseCond(Token currToken) throws LexerException, ParserException {
        CondNode subTree = new CondNode();
        ParseResult result;

        result = parseLOrExp(currToken);
        currToken = result.getNextToken();
        subTree.lOrExp = (LOrExpNode) result.getSubtree();

        return new ParseResult(currToken, subTree);
    }

    private ParseResult parseLOrExp(Token currToken) throws LexerException, ParserException {
        LOrExpNode subTree = new LOrExpNodeForSingle();
        ParseResult result;

        result = parseLAndExp(currToken);
        currToken = result.getNextToken();
        ((LOrExpNodeForSingle) subTree).lAndExp = (LAndExpNode) result.getSubtree();

        while (isMatch(currToken, LexType.OR)) {
            currToken = buf.readNextToken();

            result = parseLAndExp(currToken);
            currToken = result.getNextToken();
            LOrExpNodeForDouble newNode = new LOrExpNodeForDouble();
            newNode.lAndExp = (LAndExpNode) result.getSubtree();
            newNode.lOrExp = subTree;
            subTree = newNode;
        }

        return new ParseResult(currToken, subTree);
    }

    private ParseResult parseLAndExp(Token currToken) throws LexerException, ParserException {
        LAndExpNode subTree = new LAndExpNodeForSingle();
        ParseResult result;

        result = parseEqExp(currToken);
        currToken = result.getNextToken();
        ((LAndExpNodeForSingle) subTree).eqExp = (EqExpNode) result.getSubtree();

        while (isMatch(currToken, LexType.AND)) {
            currToken = buf.readNextToken();

            result = parseEqExp(currToken);
            currToken = result.getNextToken();
            LAndExpNodeForDouble newNode = new LAndExpNodeForDouble();
            newNode.eqExp = (EqExpNode) result.getSubtree();
            newNode.lAndExp = subTree;
            subTree = newNode;
        }

        return new ParseResult(currToken, subTree);
    }

    private ParseResult parseEqExp(Token currToken) throws LexerException, ParserException {
        EqExpNode subTree = new EqExpNodeForSingle();
        ParseResult result;

        result = parseRelExp(currToken);
        currToken = result.getNextToken();
        ((EqExpNodeForSingle) subTree).relExp = (RelExpNode) result.getSubtree();

        while (isMatch(currToken, LexType.EQL) || isMatch(currToken, LexType.NEQ)) {
            LexType op = currToken.getType();
            currToken = buf.readNextToken();

            result = parseRelExp(currToken);
            currToken = result.getNextToken();
            EqExpNodeForDouble newNode = new EqExpNodeForDouble();
            newNode.relExp = (RelExpNode) result.getSubtree();
            newNode.eqExp = subTree;
            newNode.op = op;
            subTree = newNode;
        }

        return new ParseResult(currToken, subTree);
    }

    private ParseResult parseRelExp(Token currToken) throws LexerException, ParserException {
        RelExpNode subTree = new RelExpNodeForSingle();
        ParseResult result;

        result = parseAddExp(currToken);
        currToken = result.getNextToken();
        ((RelExpNodeForSingle) subTree).addExp = (AddExpNode) result.getSubtree();

        while (isMatch(currToken, LexType.LSS)
                || isMatch(currToken, LexType.GRE)
                || isMatch(currToken, LexType.LEQ)
                || isMatch(currToken, LexType.GEQ)
        ) {
            LexType op = currToken.getType();
            currToken = buf.readNextToken();

            result = parseAddExp(currToken);
            currToken = result.getNextToken();
            RelExpNodeForDouble newNode = new RelExpNodeForDouble();
            newNode.addExp = (AddExpNode) result.getSubtree();
            newNode.relExp = subTree;
            newNode.op = op;
            subTree = newNode;
        }

        return new ParseResult(currToken, subTree);
    }

    private ParseResult parseForStmt(Token currToken) throws LexerException, ParserException {
        ForStmtNode subTree = new ForStmtNode();
        ParseResult result;

        result = parseLVal(currToken);
        currToken = result.getNextToken();
        subTree.lVal = (LValNode) result.getSubtree();

        currToken = parseToken(currToken, LexType.ASSIGN, new ParserException());

        result = parseExp(currToken);
        currToken = result.getNextToken();
        subTree.exp = (ExpNode) result.getSubtree();

        return new ParseResult(currToken, subTree);
    }

    private ParseResult parseFuncType(Token currToken) throws LexerException, ParserException {
        FuncTypeNode subTree = new FuncTypeNode();

        if (isMatch(currToken, LexType.VOIDTK) || isMatch(currToken, LexType.INTTK)) {
            subTree.type = currToken.getType();

            currToken = buf.readNextToken();
        } else {
            throw new ParserException();
        }
        return new ParseResult(currToken, subTree);
    }

    private ParseResult parseDecl(Token currToken) throws LexerException, ParserException {
        DeclNode subTree;
        ParseResult result;

        if (isMatch(currToken, LexType.CONSTTK)) {
            var newNode = new DeclNodeForConstDecl();

            result = parseConstDecl(currToken);
            currToken = result.getNextToken();
            newNode.constDecl = (ConstDeclNode) result.getSubtree();

            subTree = newNode;
        } else if (isMatch(currToken, LexType.INTTK)) {
            var newNode = new DeclNodeForVarDecl();

            result = parseVarDecl(currToken);
            currToken = result.getNextToken();
            newNode.varDecl = (VarDeclNode) result.getSubtree();

            subTree = newNode;
        } else {
            throw new ParserException();
        }

        return new ParseResult(currToken, subTree);
    }

    private ParseResult parseConstDecl(Token currToken) throws LexerException, ParserException {
        ConstDeclNode subTree = new ConstDeclNode();
        ParseResult result;

        currToken = parseToken(currToken, LexType.CONSTTK, new ParserException());

        result = parseBType(currToken);
        currToken = result.getNextToken();
        subTree.type = (BTypeNode) result.getSubtree();

        result = parseConstDef(currToken);
        currToken = result.getNextToken();
        subTree.constDefs.add((ConstDefNode) result.getSubtree());

        while (isMatch(currToken, LexType.COMMA)) {
            currToken = buf.readNextToken();

            result = parseConstDef(currToken);
            currToken = result.getNextToken();
            subTree.constDefs.add((ConstDefNode) result.getSubtree());
        }

        if (isMatch(currToken, LexType.SEMICN)) {
            currToken = buf.readNextToken();
        } else {
            errorRecorder.addError(CompileErrorType.SEMICN_IS_MISSING, buf.readPreToken().getLineNum());
        }

        return new ParseResult(currToken, subTree);
    }

    private ParseResult parseConstDef(Token currToken) throws LexerException, ParserException {
        ConstDefNode subTree = new ConstDefNode();
        ParseResult result;

        subTree.ident = currToken.getValue();
        subTree.identLineNum = currToken.getLineNum();
        currToken = parseToken(currToken, LexType.IDENFR, new ParserException());
        while (isMatch(currToken, LexType.LBRACK)) {
            currToken = buf.readNextToken();

            result = parseConstExp(currToken);
            currToken = result.getNextToken();
            subTree.dimensions.add((ConstExpNode) result.getSubtree());

            if (isMatch(currToken, LexType.RBRACK)) {
                currToken = buf.readNextToken();
            } else {
                errorRecorder.addError(CompileErrorType.RBRACK_IS_MISSING, buf.readPreToken().getLineNum());
            }
        }
        currToken = parseToken(currToken, LexType.ASSIGN, new ParserException());

        result = parseConstInitVal(currToken);
        currToken = result.getNextToken();
        subTree.constInitVal = (ConstInitValNode) result.getSubtree();

        return new ParseResult(currToken, subTree);
    }

    private ParseResult parseConstInitVal(Token currToken) throws LexerException, ParserException {
        ConstInitValNode subTree;
        ParseResult result;

        if (isMatch(currToken, LexType.LPARENT)
                || isMatch(currToken, LexType.IDENFR)
                || isMatch(currToken, LexType.INTCON)
                || isMatch(currToken, LexType.PLUS)
                || isMatch(currToken, LexType.MINU)
        ) {
            var newNode = new ConstInitValNodeForConstExp();

            result = parseConstExp(currToken);
            currToken = result.getNextToken();
            newNode.constExp = (ConstExpNode) result.getSubtree();

            subTree = newNode;
        } else if (isMatch(currToken, LexType.LBRACE)) {
            var newNode = new ConstInitValNodeForArrayInit();

            currToken = buf.readNextToken();
            if (isNotMatch(currToken, LexType.RBRACE)) {
                result = parseConstInitVal(currToken);
                currToken = result.getNextToken();
                newNode.initValues.add((ConstInitValNode) result.getSubtree());

                while (isMatch(currToken, LexType.COMMA)) {
                    currToken = buf.readNextToken();

                    result = parseConstInitVal(currToken);
                    currToken = result.getNextToken();
                    newNode.initValues.add((ConstInitValNode) result.getSubtree());
                }
            }
            currToken = parseToken(currToken, LexType.RBRACE, new ParserException());

            subTree = newNode;
        } else {
            throw new ParserException();
        }

        return new ParseResult(currToken, subTree);
    }

    private ParseResult parseVarDecl(Token currToken) throws LexerException, ParserException {
        VarDeclNode subTree = new VarDeclNode();
        ParseResult result;

        result = parseBType(currToken);
        currToken = result.getNextToken();
        subTree.type = (BTypeNode) result.getSubtree();

        result = parseVarDef(currToken);
        currToken = result.getNextToken();
        subTree.varDefs.add((VarDefNode) result.getSubtree());

        while (isMatch(currToken, LexType.COMMA)) {
            currToken = buf.readNextToken();

            result = parseVarDef(currToken);
            currToken = result.getNextToken();
            subTree.varDefs.add((VarDefNode) result.getSubtree());
        }

        if (isMatch(currToken, LexType.SEMICN)) {
            currToken = buf.readNextToken();
        } else {
            errorRecorder.addError(CompileErrorType.SEMICN_IS_MISSING, buf.readPreToken().getLineNum());
        }

        return new ParseResult(currToken, subTree);
    }

    private ParseResult parseVarDef(Token currToken) throws LexerException, ParserException {
        VarDefNode subTree = new VarDefNode();
        ParseResult result;

        subTree.ident = currToken.getValue();
        subTree.identLineNum = currToken.getLineNum();
        currToken = parseToken(currToken, LexType.IDENFR, new ParserException());
        while (isMatch(currToken, LexType.LBRACK)) {
            currToken = buf.readNextToken();

            result = parseConstExp(currToken);
            currToken = result.getNextToken();
            subTree.dimensions.add((ConstExpNode) result.getSubtree());

            if (isMatch(currToken, LexType.RBRACK)) {
                currToken = buf.readNextToken();
            } else {
                errorRecorder.addError(CompileErrorType.RBRACK_IS_MISSING, buf.readPreToken().getLineNum());
            }
        }
        if (isMatch(currToken, LexType.ASSIGN)) {
            currToken = buf.readNextToken();

            result = parseInitVal(currToken);
            currToken = result.getNextToken();
            subTree.initVal = (InitValNode) result.getSubtree();
        }

        return new ParseResult(currToken, subTree);
    }

    private ParseResult parseInitVal(Token currToken) throws LexerException, ParserException {
        InitValNode subTree;
        ParseResult result;

        if (isMatch(currToken, LexType.LPARENT)
                || isMatch(currToken, LexType.IDENFR)
                || isMatch(currToken, LexType.INTCON)
                || isMatch(currToken, LexType.PLUS)
                || isMatch(currToken, LexType.MINU)
        ) {
            var newNode = new InitValNodeForExp();

            result = parseExp(currToken);
            currToken = result.getNextToken();
            newNode.exp = (ExpNode) result.getSubtree();

            subTree = newNode;
        } else if (isMatch(currToken, LexType.LBRACE)) {
            var newNode = new InitValNodeForArray();

            currToken = buf.readNextToken();
            if (isNotMatch(currToken, LexType.RBRACK)) {
                result = parseInitVal(currToken);
                currToken = result.getNextToken();
                newNode.initVals.add((InitValNode) result.getSubtree());

                while (isMatch(currToken, LexType.COMMA)) {
                    currToken = buf.readNextToken();

                    result = parseInitVal(currToken);
                    currToken = result.getNextToken();
                    newNode.initVals.add((InitValNode) result.getSubtree());
                }
            }
            currToken = parseToken(currToken, LexType.RBRACE, new ParserException());

            subTree = newNode;
        } else {
            throw new ParserException();
        }

        return new ParseResult(currToken, subTree);
    }

    private ParseResult parseMainFuncDef(Token currToken) throws LexerException, ParserException {
        MainFuncDefNode subTree = new MainFuncDefNode();
        ParseResult result;

        currToken = parseToken(currToken, LexType.INTTK, new ParserException());
        currToken = parseToken(currToken, LexType.MAINTK, new ParserException());
        currToken = parseToken(currToken, LexType.LPARENT, new ParserException());

        if (isMatch(currToken, LexType.RPARENT)) {
            currToken = buf.readNextToken();
        } else {
            errorRecorder.addError(CompileErrorType.RPARENT_IS_MISSING, buf.readPreToken().getLineNum());
        }

        result = parseBlock(currToken);
        currToken = result.getNextToken();
        subTree.mainBlock = (BlockNode) result.getSubtree();

        return new ParseResult(currToken, subTree);
    }
}


class PreReadBuffer {
    private final Lexer lexer;
    private final int tokenBufLen;
    private final Token[] tokenBuf;
    private int currTokenPos = 0;
    private final Queue<Token> findBuffer = new ArrayDeque<>();
    private Token preToken = null;

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
        preToken = tokenBuf[currTokenPos];
        if (!findBuffer.isEmpty()) {
            tokenBuf[currTokenPos] = findBuffer.poll();
        } else if (lexer.next()) {
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

    public boolean findUntil(LexType find, LexType until) throws LexerException {
        for (int i = 0, j = currTokenPos; i < tokenBufLen; i++, j = (currTokenPos + 1) % tokenBufLen) {
            if (tokenBuf[j].getType() == find) {
                return true;
            } else if (tokenBuf[j].getType() == until) {
                return false;
            }
        }

        while (lexer.next()) {
            var token = lexer.getToken();
            findBuffer.add(lexer.getToken());
            if (token.getType() == find) {
                return true;
            } else if (token.getType() == until) {
                return false;
            }
        }
        return false;
    }

    public Token readPreToken() {
        return preToken;
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
