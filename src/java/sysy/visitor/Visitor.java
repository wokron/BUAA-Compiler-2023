package sysy.visitor;

import sysy.error.CompileErrorType;
import sysy.error.ErrorRecorder;
import sysy.lexer.LexType;
import sysy.parser.syntaxtree.*;
import sysy.symtable.SymbolTable;
import sysy.symtable.symbol.FunctionSymbol;
import sysy.symtable.symbol.ParamType;
import sysy.symtable.symbol.VarSymbol;

import java.util.ArrayList;

public class Visitor {
    private final ErrorRecorder errorRecorder;
    private final SymbolTable table = new SymbolTable();
    private SymbolTable currTable = table;
    private int isInLoop = 0;

    public Visitor(ErrorRecorder errorRecorder) {
        this.errorRecorder = errorRecorder;
    }

    public Integer visitAddExpNodeForDouble(AddExpNodeForDouble elm) {
        var val1 = visitAddExpNode(elm.addExp);
        var val2 = visitMulExpNode(elm.mulExp);
        if (val1 == null || val2 == null) {
            return null;
        } else {
            if (elm.op == LexType.PLUS) {
                return val1 + val2;
            } else {
                return val1 - val2;
            }
        }
    }

    public Integer visitAddExpNodeForSingle(AddExpNodeForSingle elm) {
        return visitMulExpNode(elm.mulExp);
    }

    public Integer visitAddExpNode(AddExpNode elm) {
        if (elm instanceof AddExpNodeForSingle) {
            return visitAddExpNodeForSingle((AddExpNodeForSingle) elm);
        } else {
            return visitAddExpNodeForDouble((AddExpNodeForDouble) elm);
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
        if (currTable.contains(elm.ident)) {
            errorRecorder.addError(CompileErrorType.NAME_REDEFINE, elm.identLineNum);
            return;
        }

        var varSym = new VarSymbol();
        varSym.ident = elm.ident;
        varSym.isConst = true;

        for (var dimension : elm.dimensions) {
            varSym.dims.add(visitConstExpNode(dimension));
        }

        varSym.values.addAll(visitConstInitValNode(elm.constInitVal));

        currTable.insertSymbol(varSym);
    }

    public Integer visitConstExpNode(ConstExpNode elm) {
        var val = visitAddExpNode(elm.addExp);
        assert val != null;
        return val;
    }

    public ArrayList<Integer> visitConstInitValNodeForArrayInit(ConstInitValNodeForArrayInit elm) {
        var rt = new ArrayList<Integer>();
        for (var init : elm.initValues) {
            rt.addAll(visitConstInitValNode(init));
        }
        return rt;
    }

    public ArrayList<Integer> visitConstInitValNodeForConstExp(ConstInitValNodeForConstExp elm) {
        var rt = new ArrayList<Integer>();
        rt.add(visitConstExpNode(elm.constExp));
        return rt;
    }

    public ArrayList<Integer> visitConstInitValNode(ConstInitValNode elm) {
        if (elm instanceof ConstInitValNodeForArrayInit) {
            return visitConstInitValNodeForArrayInit((ConstInitValNodeForArrayInit) elm);
        } else {
            return visitConstInitValNodeForConstExp((ConstInitValNodeForConstExp) elm);
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

    public Integer visitExpNode(ExpNode elm) {
        return visitAddExpNode(elm.addExp);
    }

    public void visitForStmtNode(ForStmtNode elm) {
        visitLValNode(elm.lVal);
        visitExpNode(elm.exp);
    }

    public void visitFuncDefNode(FuncDefNode elm) {
        if (currTable.contains(elm.ident)) {
            errorRecorder.addError(CompileErrorType.NAME_REDEFINE, elm.identLineNum);
            return;
        }

        var sym = new FunctionSymbol();
        sym.ident = elm.ident;

        String retType = visitFuncTypeNode(elm.funcType);
        sym.retType = retType;

        currTable = currTable.createSubTable();
        if (elm.params != null) {
            sym.paramTypeList.addAll(visitFuncFParamsNode(elm.params));
        }

        currTable.getPreTable().insertSymbol(sym);

        visitBlockNode(elm.block);

        if (!sym.retType.equals("void") && elm.block.isWithoutReturn()) {
            errorRecorder.addError(CompileErrorType.RETURN_IS_MISSING, elm.block.blockRLineNum);
        }

        currTable = currTable.getPreTable();
    }

    public ParamType visitFuncFParamNode(FuncFParamNode elm) {
        if (currTable.contains(elm.ident)) {
            errorRecorder.addError(CompileErrorType.NAME_REDEFINE, elm.identLineNum);
            return null; // this may be error
        }

        var varSym = new VarSymbol();
        varSym.ident = elm.ident;
        varSym.isConst = false;

        visitBTypeNode(elm.type);
        if (elm.dimensions != null) {
            varSym.dims.add(null); // for dim 0
            for (var dim : elm.dimensions) {
                varSym.dims.add(visitConstExpNode(dim));
            }
        }

        currTable.insertSymbol(varSym);

        var rt = new ParamType();
        rt.type = "int";
        rt.dims.addAll(varSym.dims);
        return rt;
    }

    public ArrayList<ParamType> visitFuncFParamsNode(FuncFParamsNode elm) {
        var rt = new ArrayList<ParamType>();
        for (var param : elm.params) {
            rt.add(visitFuncFParamNode(param));
        }
        return rt;
    }

    public void visitFuncRParamsNode(FuncRParamsNode elm) {
        for (var exp : elm.exps) {
            visitExpNode(exp);
        }
    }

    public String visitFuncTypeNode(FuncTypeNode elm) {
        return elm.type.toString();
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

    public VarSymbol visitLValNode(LValNode elm) {
        var rt = currTable.getSymbol(elm.ident);
        if (rt == null || rt instanceof FunctionSymbol) {
            errorRecorder.addError(CompileErrorType.UNDEFINED_NAME, elm.identLineNum);
            return null;
        }

        for (var dim : elm.dimensions) {
            visitExpNode(dim);
        }

        return (VarSymbol) rt;
    }

    public void visitMainFuncDefNode(MainFuncDefNode elm) {
        var sym = new FunctionSymbol();
        sym.retType = "int";
        sym.ident = "main";
        currTable.insertSymbol(sym);

        currTable = currTable.createSubTable();

        visitBlockNode(elm.mainBlock);

        if (!sym.retType.equals("void") && elm.mainBlock.isWithoutReturn()) {
            errorRecorder.addError(CompileErrorType.RETURN_IS_MISSING, elm.mainBlock.blockRLineNum);
        }

        currTable = currTable.getPreTable();
    }

    public Integer visitMulExpNodeForDouble(MulExpNodeForDouble elm) {
        var val1 = visitMulExpNode(elm.mulExp);
        var val2 = visitUnaryExpNode(elm.unaryExp);
        if (val1 == null || val2 == null) {
            return null;
        } else {
            if (elm.op == LexType.MULT) {
                return val1 * val2;
            } else if (elm.op == LexType.DIV) {
                return val1 / val2;
            } else {
                return val1 % val2;
            }
        }
    }

    public Integer visitMulExpNodeForSingle(MulExpNodeForSingle elm) {
        return visitUnaryExpNode(elm.unaryExp);
    }

    public Integer visitMulExpNode(MulExpNode elm) {
        if (elm instanceof MulExpNodeForDouble) {
            return visitMulExpNodeForDouble((MulExpNodeForDouble) elm);
        } else {
            return visitMulExpNodeForSingle((MulExpNodeForSingle) elm);
        }
    }

    public int visitNumberNode(NumberNode elm) {
        return Integer.parseInt(elm.intConst);
    }

    public Integer visitPrimaryExpNodeForExp(PrimaryExpNodeForExp elm) {
        return visitExpNode(elm.exp);
    }

    public Integer visitPrimaryExpNodeForLVal(PrimaryExpNodeForLVal elm) {
        visitLValNode(elm.lVal);
        return null;
    }

    public Integer visitPrimaryExpNodeForNumber(PrimaryExpNodeForNumber elm) {
        return visitNumberNode(elm.number);
    }

    public Integer visitPrimaryExpNode(PrimaryExpNode elm) {
        if (elm instanceof PrimaryExpNodeForExp) {
            return visitPrimaryExpNodeForExp((PrimaryExpNodeForExp) elm);
        } else if (elm instanceof PrimaryExpNodeForLVal) {
            return visitPrimaryExpNodeForLVal((PrimaryExpNodeForLVal) elm);
        } else {
            return visitPrimaryExpNodeForNumber((PrimaryExpNodeForNumber) elm);
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
        var lValSym = visitLValNode(elm.lVal);
        if (lValSym != null && lValSym.isConst) {
            errorRecorder.addError(CompileErrorType.TRY_TO_CHANGE_VAL_OF_CONST, elm.lVal.identLineNum);
        }

        visitExpNode(elm.exp);
    }

    public void visitStmtNodeForBlock(StmtNodeForBlock elm) {
        currTable = currTable.createSubTable();
        visitBlockNode(elm.block);
        currTable = currTable.getPreTable();
    }

    public void visitStmtNodeForContinueBreak(StmtNodeForContinueBreak elm) {
        if (isInLoop == 0) {
            errorRecorder.addError(CompileErrorType.BREAK_OR_CONTINUE_NOT_IN_LOOP, elm.tkLineNum);
        }
    }

    public void visitStmtNodeForExp(StmtNodeForExp elm) {
        visitExpNode(elm.exp);
    }

    public void visitStmtNodeForGetInt(StmtNodeForGetInt elm) {
        var lValSym = visitLValNode(elm.lVal);
        if (lValSym != null && lValSym.isConst) {
            errorRecorder.addError(CompileErrorType.TRY_TO_CHANGE_VAL_OF_CONST, elm.lVal.identLineNum);
        }
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

        isInLoop++;
        visitStmtNode(elm.stmt);
        isInLoop--;
    }

    public void visitStmtNodeForPrintf(StmtNodeForPrintf elm) {
        for (var exp : elm.exps) {
            visitExpNode(exp);
        }

        var fCharNum = (elm.formatString.length() - String.join("", elm.formatString.split("%d")).length()) / 2;
        if (fCharNum != elm.exps.size()) {
            errorRecorder.addError(CompileErrorType.NUM_OF_PARAM_IN_PRINTF_NOT_MATCH, elm.printfLineNum);
        }
    }

    public void visitStmtNodeForReturn(StmtNodeForReturn elm) {
        if (elm.exp != null) {
            if (elm.isExpNotNeed()) { // todo: need to modify
                errorRecorder.addError(CompileErrorType.RETURN_NOT_MATCH, elm.returnLineNum);
            }
            visitExpNode(elm.exp);
        }
    }

    public void visitStmtNode(StmtNode elm) {
        if (elm instanceof StmtNodeForAssign e) {
            visitStmtNodeForAssign(e);
        } else if (elm instanceof StmtNodeForBlock e) {
            visitStmtNodeForBlock(e);
        } else if (elm instanceof StmtNodeForContinueBreak e) {
            visitStmtNodeForContinueBreak(e);
        } else if (elm instanceof StmtNodeForExp e) {
            visitStmtNodeForExp(e);
        } else if (elm instanceof StmtNodeForGetInt e) {
            visitStmtNodeForGetInt(e);
        } else if (elm instanceof StmtNodeForIfElse e) {
            visitStmtNodeForIfElse(e);
        } else if (elm instanceof StmtNodeForLoop e) {
            visitStmtNodeForLoop(e);
        } else if (elm instanceof StmtNodeForPrintf e) {
            visitStmtNodeForPrintf(e);
        } else {
            visitStmtNodeForReturn((StmtNodeForReturn) elm);
        }
    }

    public Integer visitUnaryExpNodeForFuncCall(UnaryExpNodeForFuncCall elm) {
        var sym = currTable.getSymbol(elm.ident);
        if (sym == null || sym instanceof VarSymbol) {
            errorRecorder.addError(CompileErrorType.UNDEFINED_NAME, elm.identLineNum);
            return null;
        }
        FunctionSymbol funcSym = (FunctionSymbol) sym;

        if (elm.params != null) {
            visitFuncRParamsNode(elm.params);

            if (elm.params.exps.size() != funcSym.paramTypeList.size()) {
                errorRecorder.addError(CompileErrorType.NUM_OF_PARAM_NOT_MATCH, elm.identLineNum);
                return null;
            }

            // todo: param type not match error check
        }

        return null;
    }

    public Integer visitUnaryExpNodeForPrimaryExp(UnaryExpNodeForPrimaryExp elm) {
        return visitPrimaryExpNode(elm.primaryExp);
    }

    public Integer visitUnaryExpNodeForUnaryOp(UnaryExpNodeForUnaryOp elm) {
        var val = visitUnaryExpNode(elm.exp);
        if (elm.op.opType == LexType.MINU) {
            return -val;
        } else if (elm.op.opType == LexType.PLUS) {
            return val;
        } else {
            return val == 0 ? 0 : 1;
        }
    }

    public Integer visitUnaryExpNode(UnaryExpNode elm) {
        if (elm instanceof UnaryExpNodeForFuncCall) {
            return visitUnaryExpNodeForFuncCall((UnaryExpNodeForFuncCall) elm);
        } else if (elm instanceof UnaryExpNodeForPrimaryExp) {
            return visitUnaryExpNodeForPrimaryExp((UnaryExpNodeForPrimaryExp) elm);
        } else {
            return visitUnaryExpNodeForUnaryOp((UnaryExpNodeForUnaryOp) elm);
        }
    }

    public void visitUnaryOpNode(UnaryOpNode elm) {

    }

    public void visitVarDeclNode(VarDeclNode elm) {
        visitBTypeNode(elm.type);
        for (var varDef : elm.varDefs) {
            visitVarDefNode(varDef);
        }
    }

    public void visitVarDefNode(VarDefNode elm) {
        if (currTable.contains(elm.ident)) {
            errorRecorder.addError(CompileErrorType.NAME_REDEFINE, elm.identLineNum);
            return;
        }

        var varSym = new VarSymbol();
        varSym.isConst = false;
        varSym.ident = elm.ident;

        for (var dim : elm.dimensions) {
            varSym.dims.add(visitConstExpNode(dim));
        }

        if (elm.initVal != null) {
            visitInitValNode(elm.initVal);
        }

        currTable.insertSymbol(varSym);
    }
}
