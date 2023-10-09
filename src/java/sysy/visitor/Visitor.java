package sysy.visitor;

import sysy.lexer.LexType;
import sysy.parser.syntaxtree.*;
import sysy.parser.syntaxtree.symbol.TerminalSymbol;
import sysy.symtable.SymbolTable;

public class Visitor {
    private final SymbolTable table = new SymbolTable();
    private SymbolTable currTable = table;
    public void visitAddExpNodeForDouble(AddExpNodeForDouble elm) {
        visitAddExpNode(elm.addExp);
        visitMulExpNode(elm.mulExp);
    }

    public void visitAddExpNodeForSingle(AddExpNodeForSingle elm) {
        visitMulExpNode(elm.mulExp);
    }

    public void visitAddExpNode(AddExpNode elm) {
        if (elm instanceof AddExpNodeForSingle) {
            visitAddExpNodeForSingle((AddExpNodeForSingle) elm);
        } else {
            visitAddExpNodeForDouble((AddExpNodeForDouble) elm);
        }
    }

    public void visitBlockItemNodeForDecl(BlockItemNodeForDecl elm) {
        visitDeclNode(elm.decl);
    }

    public void visitBlockItemNodeForStmt(BlockItemNodeForStmt elm) {
        visitStmtNode(elm.stmt);
    }

    public void visitBlockItemNode(BlockItemNode elm) {
        if (elm instanceof BlockItemNodeForDecl) {
            visitBlockItemNodeForDecl((BlockItemNodeForDecl) elm);
        } else {
            visitBlockItemNodeForStmt((BlockItemNodeForStmt) elm);
        }
    }

    public void visitBlockNode(BlockNode elm) {
        for (var blockItem : elm.blockItems) {
            visitBlockItemNode(blockItem);
        }
    }

    public void visitBTypeNode(BTypeNode elm) {

    }

    public void visitCompUnitNode(CompUnitNode elm) {
        for (var declare : elm.declares) {
            visitDeclNode(declare);
        }

        for (var func : elm.funcs) {
            visitFuncDefNode(func);
        }

        visitMainFuncDefNode(elm.mainFunc);
    }

    public void visitCondNode(CondNode elm) {
        visitLOrExpNode(elm.lOrExp);
    }

    public void visitConstDeclNode(ConstDeclNode elm) {
        for (var constDef : elm.constDefs) {
            visitConstDefNode(constDef);
        }
    }

    public void visitConstDefNode(ConstDefNode elm) {
        for (var dimension : elm.dimensions) {
            visitConstExpNode(dimension);
        }

        visitConstInitValNode(elm.constInitVal);
    }

    public void visitConstExpNode(ConstExpNode elm) {
        visitAddExpNode(elm.addExp);
    }

    public void visitConstInitValNodeForArrayInit(ConstInitValNodeForArrayInit elm) {
        for (var init : elm.initValues) {
            visitConstInitValNode(init);
        }
    }

    public void visitConstInitValNodeForConstExp(ConstInitValNodeForConstExp elm) {
        visitConstExpNode(elm.constExp);
    }

    public void visitConstInitValNode(ConstInitValNode elm) {
        if (elm instanceof ConstInitValNodeForArrayInit) {
            visitConstInitValNodeForArrayInit((ConstInitValNodeForArrayInit) elm);
        } else {
            visitConstInitValNodeForConstExp((ConstInitValNodeForConstExp) elm);
        }
    }

    public void visitDeclNodeForConstDecl(DeclNodeForConstDecl elm) {
        visitConstDeclNode(elm.constDecl);
    }

    public void visitDeclNodeForVarDecl(DeclNodeForVarDecl elm) {
        visitVarDeclNode(elm.varDecl);
    }

    public void visitDeclNode(DeclNode elm) {
        if (elm instanceof DeclNodeForVarDecl) {
            visitDeclNodeForVarDecl((DeclNodeForVarDecl) elm);
        } else {
            visitDeclNodeForConstDecl((DeclNodeForConstDecl) elm);
        }
    }

    public void visitEqExpNodeForDouble(EqExpNodeForDouble elm) {
        visitEqExpNode(elm.eqExp);
        visitRelExpNode(elm.relExp);
    }

    public void visitEqExpNodeForSingle(EqExpNodeForSingle elm) {
        visitRelExpNode(elm.relExp);
    }

    public void visitEqExpNode(EqExpNode elm) {
        if (elm instanceof EqExpNodeForDouble) {
            visitEqExpNodeForDouble((EqExpNodeForDouble) elm);
        } else {
            visitEqExpNodeForSingle((EqExpNodeForSingle) elm);
        }
    }

    public void visitExpNode(ExpNode elm) {
        visitAddExpNode(elm.addExp);
    }

    public void visitForStmtNode(ForStmtNode elm) {
        visitLValNode(elm.lVal);
        visitExpNode(elm.exp);
    }

    public void visitFuncDefNode(FuncDefNode elm) {
        visitFuncTypeNode(elm.funcType);
        visitFuncFParamsNode(elm.params);
        visitBlockNode(elm.block);
    }

    public void visitFuncFParamNode(FuncFParamNode elm) {
        visitBTypeNode(elm.type);
        if (elm.dimensions != null) {
            for (var dim : elm.dimensions) {
                visitConstExpNode(dim);
            }
        }
    }

    public void visitFuncFParamsNode(FuncFParamsNode elm) {
        for (var param : elm.params) {
            visitFuncFParamNode(param);
        }
    }

    public void visitFuncRParamsNode(FuncRParamsNode elm) {
        for (var exp : elm.exps) {
            visitExpNode(exp);
        }
    }

    public void visitFuncTypeNode(FuncTypeNode elm) {

    }

    public void visitInitValNodeForArray(InitValNodeForArray elm) {
        for (var init : elm.initVals) {
            visitInitValNode(init);
        }
    }

    public void visitInitValNodeForExp(InitValNodeForExp elm) {
        visitExpNode(elm.exp);
    }

    public void visitInitValNode(InitValNode elm) {
        if (elm instanceof InitValNodeForArray) {
            visitInitValNodeForArray((InitValNodeForArray) elm);
        } else {
            visitInitValNodeForExp((InitValNodeForExp) elm);
        }
    }

    public void visitLAndExpNodeForDouble(LAndExpNodeForDouble elm) {
        visitLAndExpNode(elm.lAndExp);
        visitEqExpNode(elm.eqExp);
    }

    public void visitLAndExpNodeForSingle(LAndExpNodeForSingle elm) {
        visitEqExpNode(elm.eqExp);
    }

    public void visitLAndExpNode(LAndExpNode elm) {
        if (elm instanceof  LAndExpNodeForDouble) {
            visitLAndExpNodeForDouble((LAndExpNodeForDouble) elm);
        } else {
            visitLAndExpNodeForSingle((LAndExpNodeForSingle) elm);
        }
    }

    public void visitLOrExpNodeForDouble(LOrExpNodeForDouble elm) {
        visitLOrExpNode(elm.lOrExp);
        visitLAndExpNode(elm.lAndExp);
    }

    public void visitLOrExpNodeForSingle(LOrExpNodeForSingle elm) {
        visitLAndExpNode(elm.lAndExp);
    }

    public void visitLOrExpNode(LOrExpNode elm) {
        if (elm instanceof LOrExpNodeForDouble) {
            visitLOrExpNodeForDouble((LOrExpNodeForDouble) elm);
        } else {
            visitLOrExpNodeForSingle((LOrExpNodeForSingle) elm);
        }
    }

    public void visitLValNode(LValNode elm) {
        for (var dim : elm.dimensions) {
            visitExpNode(dim);
        }
    }

    public void visitMainFuncDefNode(MainFuncDefNode elm) {
        visitBlockNode(elm.mainBlock);
    }

    public void visitMulExpNodeForDouble(MulExpNodeForDouble elm) {
        visitMulExpNode(elm.mulExp);
        visitUnaryExpNode(elm.unaryExp);
    }

    public void visitMulExpNodeForSingle(MulExpNodeForSingle elm) {
        visitUnaryExpNode(elm.unaryExp);
    }

    public void visitMulExpNode(MulExpNode elm) {
        if (elm instanceof MulExpNodeForDouble) {
            visitMulExpNodeForDouble((MulExpNodeForDouble) elm);
        } else {
            visitMulExpNodeForSingle((MulExpNodeForSingle) elm);
        }
    }

    public void visitNumberNode(NumberNode elm) {

    }

    public void visitPrimaryExpNodeForExp(PrimaryExpNodeForExp elm) {
        visitExpNode(elm.exp);
    }

    public void visitPrimaryExpNodeForLVal(PrimaryExpNodeForLVal elm) {
        visitLValNode(elm.lVal);
    }

    public void visitPrimaryExpNodeForNumber(PrimaryExpNodeForNumber elm) {
        visitNumberNode(elm.number);
    }

    public void visitPrimaryExpNode(PrimaryExpNode elm) {
        if (elm instanceof PrimaryExpNodeForExp) {
            visitPrimaryExpNodeForExp((PrimaryExpNodeForExp) elm);
        } else if (elm instanceof PrimaryExpNodeForLVal) {
            visitPrimaryExpNodeForLVal((PrimaryExpNodeForLVal) elm);
        } else {
            visitPrimaryExpNodeForNumber((PrimaryExpNodeForNumber) elm);
        }
    }

    public void visitRelExpNodeForDouble(RelExpNodeForDouble elm) {
        visitRelExpNode(elm.relExp);
        visitAddExpNode(elm.addExp);
    }

    public void visitRelExpNodeForSingle(RelExpNodeForSingle elm) {
        visitAddExpNode(elm.addExp);
    }

    public void visitRelExpNode(RelExpNode elm) {
        if (elm instanceof RelExpNodeForDouble) {
            visitRelExpNodeForDouble((RelExpNodeForDouble) elm);
        } else {
            visitRelExpNodeForSingle((RelExpNodeForSingle) elm);
        }
    }

    public void visitStmtNodeForAssign(StmtNodeForAssign elm) {
        visitLValNode(elm.lVal);
        visitExpNode(elm.exp);
    }

    public void visitStmtNodeForBlock(StmtNodeForBlock elm) {
        visitBlockNode(elm.block);
    }

    public void visitStmtNodeForContinueBreak(StmtNodeForContinueBreak elm) {

    }

    public void visitStmtNodeForExp(StmtNodeForExp elm) {
        visitExpNode(elm.exp);
    }

    public void visitStmtNodeForGetInt(StmtNodeForGetInt elm) {
        visitLValNode(elm.lVal);
    }

    public void visitStmtNodeForIfElse(StmtNodeForIfElse elm) {
        visitCondNode(elm.cond);
        visitStmtNode(elm.ifStmt);
        if (elm.elseStmt != null) {
            visitStmtNode(elm.elseStmt);
        }
    }

    public void visitStmtNodeForLoop(StmtNodeForLoop elm) {
        if (elm.forStmt1 != null) {
            visitForStmtNode(elm.forStmt1);
        }

        if (elm.cond != null) {
            visitCondNode(elm.cond);
        }

        if (elm.forStmt2 != null) {
            visitForStmtNode(elm.forStmt2);
        }

        visitStmtNode(elm.stmt);
    }

    public void visitStmtNodeForPrintf(StmtNodeForPrintf elm) {

    }

    public void visitStmtNodeForReturn(StmtNodeForReturn elm) {

    }

    public void visitStmtNode(StmtNode elm) {

    }

    public void visitSyntaxNode(SyntaxNode elm) {

    }

    public void visitUnaryExpNodeForFuncCall(UnaryExpNodeForFuncCall elm) {

    }

    public void visitUnaryExpNodeForPrimaryExp(UnaryExpNodeForPrimaryExp elm) {

    }

    public void visitUnaryExpNodeForUnaryOp(UnaryExpNodeForUnaryOp elm) {

    }

    public void visitUnaryExpNode(UnaryExpNode elm) {

    }

    public void visitUnaryOpNode(UnaryOpNode elm) {

    }

    public void visitVarDeclNode(VarDeclNode elm) {

    }

    public void visitVarDefNode(VarDefNode elm) {

    }
}
