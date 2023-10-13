package sysy.visitor;

import sysy.error.CompileErrorType;
import sysy.error.ErrorRecorder;
import sysy.lexer.LexType;
import sysy.parser.syntaxtree.*;
import sysy.symtable.SymbolTable;
import sysy.symtable.symbol.FunctionSymbol;
import sysy.symtable.symbol.Type;
import sysy.symtable.symbol.VarSymbol;

import java.util.ArrayList;
import java.util.List;

public class Visitor {
    private final ErrorRecorder errorRecorder;
    private final SymbolTable table = new SymbolTable();
    private SymbolTable currTable = table;
    private int isInLoop = 0;
    private boolean isRetExpNotNeed = false;
    private List<Integer> expType = null;

    public Visitor(ErrorRecorder errorRecorder) {
        this.errorRecorder = errorRecorder;
    }

    public VisitResult visitAddExpNodeForDouble(AddExpNodeForDouble elm) {
        var rt = new VisitResult();
        var r1 = visitAddExpNode(elm.addExp);
        var r2 = visitMulExpNode(elm.mulExp);
        var val1 = r1.constVal;
        var val2 = r2.constVal;

        assert r1.expType.equals(r2.expType);
        rt.expType = r1.expType;
        if (val1 != null && val2 != null) {
            if (elm.op == LexType.PLUS) {
                rt.constVal = val1 + val2;
            } else {
                rt.constVal = val1 - val2;
            }
        }
        return rt;
    }

    public VisitResult visitAddExpNodeForSingle(AddExpNodeForSingle elm) {
        return visitMulExpNode(elm.mulExp);
    }

    public VisitResult visitAddExpNode(AddExpNode elm) {
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

        varSym.varType.type = "int";
        for (var dimension : elm.dimensions) {
            varSym.varType.dims.add(visitConstExpNode(dimension).constVal);
        }

        varSym.values.addAll(visitConstInitValNode(elm.constInitVal));

        currTable.insertSymbol(varSym);
    }

    public VisitResult visitConstExpNode(ConstExpNode elm) {
        var rt = visitAddExpNode(elm.addExp);
        assert rt.constVal != null;
        return rt;
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
        rt.add(visitConstExpNode(elm.constExp).constVal);
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

    public VisitResult visitExpNode(ExpNode elm) {
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

        var retType = visitFuncTypeNode(elm.funcType);
        sym.retType = retType;

        currTable = currTable.createSubTable();
        if (elm.params != null) {
            sym.paramTypeList.addAll(visitFuncFParamsNode(elm.params));
        }

        currTable.getPreTable().insertSymbol(sym);

        isRetExpNotNeed = sym.retType.type.equals("void");

        visitBlockNode(elm.block);

        if (!sym.retType.type.equals("void") && elm.block.isWithoutReturn()) {
            errorRecorder.addError(CompileErrorType.RETURN_IS_MISSING, elm.block.blockRLineNum);
        }

        currTable = currTable.getPreTable();
    }

    public Type visitFuncFParamNode(FuncFParamNode elm) {
        if (currTable.contains(elm.ident)) {
            errorRecorder.addError(CompileErrorType.NAME_REDEFINE, elm.identLineNum);
            return null;
        }

        var varSym = new VarSymbol();
        varSym.ident = elm.ident;
        varSym.isConst = false;

        visitBTypeNode(elm.type);
        varSym.varType.type = "int";
        if (elm.dimensions != null) {
            varSym.varType.dims.add(null); // for dim 0
            for (var dim : elm.dimensions) {
                varSym.varType.dims.add(visitConstExpNode(dim).constVal);
            }
        }

        currTable.insertSymbol(varSym);

        var rt = new Type();
        rt.type = "int";
        rt.dims.addAll(varSym.varType.dims);
        return rt;
    }

    public ArrayList<Type> visitFuncFParamsNode(FuncFParamsNode elm) {
        var rt = new ArrayList<Type>();
        for (var param : elm.params) {
            var paramType = visitFuncFParamNode(param);
            if (paramType != null) {
                rt.add(paramType);
            }
        }
        return rt;
    }

    public List<List<Integer>> visitFuncRParamsNode(FuncRParamsNode elm) {
        List<List<Integer>> rtTypeDims = new ArrayList<>();
        for (var exp : elm.exps) {
            var r = visitExpNode(exp);
            rtTypeDims.add(expType);
        }
        return rtTypeDims;
    }

    public Type visitFuncTypeNode(FuncTypeNode elm) {
        var rt = new Type();
        rt.type = elm.type.toString();
        return rt;
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

    public VisitResult visitLValNode(LValNode elm) {
        var sym = currTable.getSymbol(elm.ident);
        if (sym == null) {
            errorRecorder.addError(CompileErrorType.UNDEFINED_NAME, elm.identLineNum);
            return null;
        }
        var varSym = (VarSymbol) sym;

        List<Integer> accessDims = new ArrayList<>();
        for (var dim : elm.dimensions) {
            accessDims.add(visitExpNode(dim).constVal);
        }

        expType = new ArrayList<>();
        for (int i = accessDims.size(); i < varSym.varType.dims.size(); i++) {
            expType.add(varSym.varType.dims.get(i));
        }
        if (!expType.isEmpty()) {
            expType.set(0, null);
        }

        Type type = new Type();
        type.type = "int";
        type.dims.addAll(expType);

        var rt = new VisitResult();
        rt.expType = type;
        rt.constVal = null; // todo: what if lVal is const

        return rt;
    }

    public void visitMainFuncDefNode(MainFuncDefNode elm) {
        var sym = new FunctionSymbol();
        sym.retType.type = "int";
        sym.ident = "main";
        currTable.insertSymbol(sym);

        currTable = currTable.createSubTable();

        isRetExpNotNeed = false;

        visitBlockNode(elm.mainBlock);

        if (!sym.retType.type.equals("void") && elm.mainBlock.isWithoutReturn()) {
            errorRecorder.addError(CompileErrorType.RETURN_IS_MISSING, elm.mainBlock.blockRLineNum);
        }

        currTable = currTable.getPreTable();
    }

    public VisitResult visitMulExpNodeForDouble(MulExpNodeForDouble elm) {
        var rt = new VisitResult();
        var r1 = visitMulExpNode(elm.mulExp);
        var r2 = visitUnaryExpNode(elm.unaryExp);
        var val1 = r1.constVal;
        var val2 = r2.constVal;

        assert r1.expType.equals(r2.expType);
        rt.expType = r1.expType;

        if (val1 != null && val2 != null) {
            if (elm.op == LexType.MULT) {
                rt.constVal = val1 * val2;
            } else if (elm.op == LexType.DIV) {
                rt.constVal = val1 / val2;
            } else {
                rt.constVal = val1 % val2;
            }
        }

        return rt;
    }

    public VisitResult visitMulExpNodeForSingle(MulExpNodeForSingle elm) {
        return visitUnaryExpNode(elm.unaryExp);
    }

    public VisitResult visitMulExpNode(MulExpNode elm) {
        if (elm instanceof MulExpNodeForDouble) {
            return visitMulExpNodeForDouble((MulExpNodeForDouble) elm);
        } else {
            return visitMulExpNodeForSingle((MulExpNodeForSingle) elm);
        }
    }

    public VisitResult visitNumberNode(NumberNode elm) {
        var rt = new VisitResult();
        rt.expType.type = "int";
        rt.constVal = Integer.parseInt(elm.intConst);
        return rt;
    }

    public VisitResult visitPrimaryExpNodeForExp(PrimaryExpNodeForExp elm) {
        return visitExpNode(elm.exp);
    }

    public VisitResult visitPrimaryExpNodeForLVal(PrimaryExpNodeForLVal elm) {
        var r = visitLValNode(elm.lVal);
        return r;
    }

    public VisitResult visitPrimaryExpNodeForNumber(PrimaryExpNodeForNumber elm) {
        expType = new ArrayList<>(); // just int

        return visitNumberNode(elm.number);
    }

    public VisitResult visitPrimaryExpNode(PrimaryExpNode elm) {
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
        visitLValNode(elm.lVal);
        var lValSym = currTable.getSymbol(elm.lVal.ident);
        if (lValSym instanceof VarSymbol lValVarSym && lValVarSym.isConst) {
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
        if (elm.exp != null) {
            visitExpNode(elm.exp);
        }
    }

    public void visitStmtNodeForGetInt(StmtNodeForGetInt elm) {
        visitLValNode(elm.lVal);
        var lValSym = currTable.getSymbol(elm.lVal.ident);
        if (lValSym instanceof VarSymbol lValVarSym && lValVarSym.isConst) {
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
            if (isRetExpNotNeed) {
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

    public VisitResult visitUnaryExpNodeForFuncCall(UnaryExpNodeForFuncCall elm) {
        var rt = new VisitResult();
        var sym = currTable.getSymbol(elm.ident);
        if (sym == null) {
            errorRecorder.addError(CompileErrorType.UNDEFINED_NAME, elm.identLineNum);
            rt.expType.type = "int"; // maybe wrong
            return rt;
        }
        FunctionSymbol funcSym = (FunctionSymbol) sym;

        rt.expType = funcSym.retType;

        if (elm.params != null) {
            var typeDims = visitFuncRParamsNode(elm.params);

            if (elm.params.exps.size() != funcSym.paramTypeList.size()) {
                errorRecorder.addError(CompileErrorType.NUM_OF_PARAM_NOT_MATCH, elm.identLineNum);
                return rt;
            }

            for (int i = 0; i < funcSym.paramTypeList.size(); i++) {
                if (!funcSym.paramTypeList.get(i).dims.equals(typeDims.get(i))) {
                    errorRecorder.addError(CompileErrorType.TYPE_OF_PARAM_NOT_MATCH, elm.identLineNum);
                }
            }
        } else {
            if (!funcSym.paramTypeList.isEmpty()) {
                errorRecorder.addError(CompileErrorType.NUM_OF_PARAM_NOT_MATCH, elm.identLineNum);
                return rt;
            }
        }

        expType = new ArrayList<>(); // just int
        if (funcSym.retType.type.equals("void")) {
            expType.add(null);
            expType.add(null);
        }

        return rt;
    }

    public VisitResult visitUnaryExpNodeForPrimaryExp(UnaryExpNodeForPrimaryExp elm) {
        return visitPrimaryExpNode(elm.primaryExp);
    }

    public VisitResult visitUnaryExpNodeForUnaryOp(UnaryExpNodeForUnaryOp elm) {
        var rt = new VisitResult();
        var r = visitUnaryExpNode(elm.exp);
        var val = r.constVal;

        assert r.expType != null;
        rt.expType = r.expType;
        var op = visitUnaryOpNode(elm.op);
        if (op == LexType.MINU) {
            rt.constVal = -val;
        } else if (op == LexType.PLUS) {
            rt.constVal = val;
        } else {
            rt.constVal = val == 0 ? 0 : 1;
        }
        return rt;
    }

    public VisitResult visitUnaryExpNode(UnaryExpNode elm) {
        if (elm instanceof UnaryExpNodeForFuncCall) {
            return visitUnaryExpNodeForFuncCall((UnaryExpNodeForFuncCall) elm);
        } else if (elm instanceof UnaryExpNodeForPrimaryExp) {
            return visitUnaryExpNodeForPrimaryExp((UnaryExpNodeForPrimaryExp) elm);
        } else {
            return visitUnaryExpNodeForUnaryOp((UnaryExpNodeForUnaryOp) elm);
        }
    }

    public LexType visitUnaryOpNode(UnaryOpNode elm) {
        return elm.opType;
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

        varSym.varType.type = "int";
        for (var dim : elm.dimensions) {
            varSym.varType.dims.add(visitConstExpNode(dim).constVal);
        }

        if (elm.initVal != null) {
            visitInitValNode(elm.initVal);
        }

        currTable.insertSymbol(varSym);
    }
}
