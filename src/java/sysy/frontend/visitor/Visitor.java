package sysy.frontend.visitor;

import sysy.backend.ir.*;
import sysy.backend.ir.Module;
import sysy.backend.ir.inst.*;
import sysy.error.CompileErrorType;
import sysy.error.ErrorRecorder;
import sysy.frontend.lexer.LexType;
import sysy.frontend.parser.syntaxtree.*;
import sysy.frontend.symtable.SymbolTable;
import sysy.frontend.symtable.symbol.FunctionSymbol;
import sysy.frontend.symtable.symbol.Type;
import sysy.frontend.symtable.symbol.VarSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Visitor {
    private final Module irModule = new Module();
    private Function currFunction = null;
    private BasicBlock currBasicBlock = null;

    // for break and continue
    private final Stack<List<BrInst>> continueBrInsts = new Stack<>();
    private final Stack<List<BrInst>> breakBrInsts = new Stack<>();

    private final ErrorRecorder errorRecorder;
    private final SymbolTable table = new SymbolTable();
    private SymbolTable currTable = table;
    private int isInLoop = 0;
    private boolean isRetExpNotNeed = false;
    private boolean isGlobalVar = true;

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
        if (currBasicBlock != null) {
            if (elm.op == LexType.PLUS) {
                rt.irValue = currBasicBlock.createAddInst(r1.irValue, r2.irValue);
            } else {
                rt.irValue = currBasicBlock.createSubInst(r1.irValue, r2.irValue);
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

    public String visitBTypeNode(BTypeNode elm) {
        return "int";
    }

    public Module generateIR(SyntaxNode root) {
        if (root instanceof CompUnitNode compNode) {
            visitCompUnitNode(compNode);
            return irModule;
        } else {
            return null;
        }
    }

    public void visitCompUnitNode(CompUnitNode elm) {
        isGlobalVar = true;
        for (var declare : elm.declares) {
            visitDeclNode(declare);
        }
        isGlobalVar = false;

        for (var func : elm.funcs) {
            visitFuncDefNode(func);
        }

        visitMainFuncDefNode(elm.mainFunc);
    }

    public VisitResult visitCondNode(CondNode elm) {
        return visitLOrExpNode(elm.lOrExp);
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

        var r = visitConstInitValNode(elm.constInitVal);
        varSym.values.addAll(r.constInitVals);
        var initValues = r.irValues;

        if (isGlobalVar) {
            var globalVar = irModule.createGlobalValue(IRType.getInt().dims(varSym.varType.dims), varSym.values);
            globalVar.setName(varSym.ident);
            varSym.targetValue = globalVar;
        } else {
            var localVar = currFunction.getFirstBasicBlock().createAllocaInstAndInsertToFront(IRType.getInt().dims(varSym.varType.dims));
            varSym.targetValue = localVar;
            if (!varSym.isArray()) {
                currBasicBlock.createStoreInst(initValues.get(0), varSym.targetValue);
            } else { // array with init values
                for (int i = 0; i < initValues.size(); i++) {
                    int[] idxs = new int[varSym.varType.dims.size()];
                    var pos = i;
                    for (int j = idxs.length - 1; j >= 0; j--) {
                        idxs[j] = pos % varSym.varType.dims.get(j);
                        pos /= varSym.varType.dims.get(j);
                    }

                    var initVal = initValues.get(i);
                    var arrayPtr = currBasicBlock.createGetElementPtrInst(varSym.targetValue, List.of(new ImmediateValue(0), new ImmediateValue(0)));
                    for (int j = 0; j < idxs.length; j++) {
                        var visitIdx = idxs[j];
                        List<Value> offsets = j == idxs.length - 1 ? List.of(new ImmediateValue(visitIdx)) : List.of(new ImmediateValue(visitIdx), new ImmediateValue(0));
                        arrayPtr = currBasicBlock.createGetElementPtrInst(arrayPtr, offsets);
                    }
                    currBasicBlock.createStoreInst(initVal, arrayPtr);
                }
            }
        }

        currTable.insertSymbol(varSym);
    }

    public VisitResult visitConstExpNode(ConstExpNode elm) {
        var rt = visitAddExpNode(elm.addExp);
        assert rt.constVal != null;
        return rt;
    }

    public VisitResult visitConstInitValNodeForArrayInit(ConstInitValNodeForArrayInit elm) {
        var rt = new VisitResult();
        for (var init : elm.initValues) {
            var r = visitConstInitValNode(init);
            rt.constInitVals.addAll(r.constInitVals);
            rt.irValues.addAll(r.irValues);
        }
        return rt;
    }

    public VisitResult visitConstInitValNodeForConstExp(ConstInitValNodeForConstExp elm) {
        var rt = new VisitResult();
        var r = visitConstExpNode(elm.constExp);
        rt.constInitVals.add(r.constVal);
        rt.irValues.add(r.irValue);
        return rt;
    }

    public VisitResult visitConstInitValNode(ConstInitValNode elm) {
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

    public VisitResult visitEqExpNodeForDouble(EqExpNodeForDouble elm) {
        var rt = new VisitResult();
        var r1 = visitEqExpNode(elm.eqExp);
        var r2 = visitRelExpNode(elm.relExp);

        if (r1.irValue instanceof ICmpInst) {
            r1.irValue = currBasicBlock.createZExtInst(IRType.getInt(), r1.irValue);
        }
        if (r2.irValue instanceof ICmpInst) {
            r2.irValue = currBasicBlock.createZExtInst(IRType.getInt(), r2.irValue);
        }

        ICmpInstCond cond = elm.op == LexType.EQL ? ICmpInstCond.EQ : ICmpInstCond.NE;
        rt.irValue = currBasicBlock.createICmpInst(cond, r1.irValue, r2.irValue);
        return rt;
    }

    public VisitResult visitEqExpNodeForSingle(EqExpNodeForSingle elm) {
        return visitRelExpNode(elm.relExp);
    }

    public VisitResult visitEqExpNode(EqExpNode elm) {
        if (elm instanceof EqExpNodeForDouble) {
            return visitEqExpNodeForDouble((EqExpNodeForDouble) elm);
        } else {
            return visitEqExpNodeForSingle((EqExpNodeForSingle) elm);
        }
    }

    public VisitResult visitExpNode(ExpNode elm) {
        return visitAddExpNode(elm.addExp);
    }

    public void visitForStmtNode(ForStmtNode elm) {
        var r1 = visitLValNode(elm.lVal);
        var r2 = visitExpNode(elm.exp);
        currBasicBlock.createStoreInst(r2.irValue, r1.irValue);
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

        ArrayList<IRType> irArgTypes = new ArrayList<>();
        for (var type : sym.paramTypeList) {
            if (type.dims.isEmpty()) {
                irArgTypes.add(IRType.getInt());
            } else {
                irArgTypes.add(IRType.getInt().dims(type.dims.subList(1, type.dims.size())).ptr(1));
            }
        }
        currFunction = irModule.createFunction(sym.retType.type.equals("void") ? IRType.getVoid() : IRType.getInt(), irArgTypes);
        currFunction.setName(sym.ident);
        sym.targetValue = currFunction;
        currBasicBlock = currFunction.createBasicBlock();
        currBasicBlock.setLoopNum(isInLoop);

        if (elm.params != null) {
            for (int i = currFunction.getArguments().size()-1; i >= 0; i--) { // the order of alloca for args is reversed to fit mips
                var currArgVal = currFunction.getArguments().get(i);
                var currParamSym = currTable.getSymbol(elm.params.params.get(i).ident);
                var currArgPtr = currFunction.getFirstBasicBlock().createAllocaInstAndInsertToFront(currArgVal.getType());
                currBasicBlock.createStoreInst(currArgVal, currArgPtr);
                currParamSym.targetValue = currArgPtr;
            }
        }

        visitBlockNode(elm.block);

        if (sym.retType.type.equals("void") && elm.block.isWithoutReturn()) {
            currBasicBlock.createReturnInst(null);
        }

        currBasicBlock = null;
        currFunction = null;

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

        varSym.varType.type = visitBTypeNode(elm.type);
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

    public VisitResult visitFuncRParamsNode(FuncRParamsNode elm) {
        var rt = new VisitResult();
        for (var exp : elm.exps) {
            var r = visitExpNode(exp);
            rt.paramTypes.add(r.expType);
            rt.irValues.add(r.irValue);
        }
        return rt;
    }

    public Type visitFuncTypeNode(FuncTypeNode elm) {
        var rt = new Type();
        rt.type = elm.type.toString();
        return rt;
    }

    public VisitResult visitInitValNodeForArray(InitValNodeForArray elm) {
        var rt = new VisitResult();
        for (var init : elm.initVals) {
            var r = visitInitValNode(init);
            rt.constInitVals.addAll(r.constInitVals);
            rt.irValues.addAll(r.irValues);
        }
        return rt;
    }

    public VisitResult visitInitValNodeForExp(InitValNodeForExp elm) {
        var rt = new VisitResult();
        var r = visitExpNode(elm.exp);
        rt.constInitVals.add(r.constVal);
        rt.irValues.add(r.irValue);
        return rt;
    }

    public VisitResult visitInitValNode(InitValNode elm) {
        if (elm instanceof InitValNodeForArray) {
            return visitInitValNodeForArray((InitValNodeForArray) elm);
        } else {
            return visitInitValNodeForExp((InitValNodeForExp) elm);
        }
    }

    public VisitResult visitLAndExpNodeForDouble(LAndExpNodeForDouble elm) {
        var rt = new VisitResult();

        var r1 = visitLAndExpNode(elm.lAndExp);
        var lastAndBlock = r1.andBlocks.get(r1.andBlocks.size()-1);
        var brInLastAndBlock = (BrInst)lastAndBlock.getInstructions().get(lastAndBlock.getInstructions().size()-1);
        brInLastAndBlock.setTrueBranch(currBasicBlock);
        rt.andBlocks.addAll(r1.andBlocks);

        var r2 = visitEqExpNode(elm.eqExp);
        if (!(r2.irValue instanceof ICmpInst)) {
            r2.irValue = currBasicBlock.createICmpInst(ICmpInstCond.NE, new ImmediateValue(0), r2.irValue);
        }
        currBasicBlock.createBrInstWithCond(r2.irValue, null, null);
        rt.andBlocks.add(currBasicBlock);
        currBasicBlock = currFunction.createBasicBlock();
        currBasicBlock.setLoopNum(isInLoop);

        return rt;
    }

    public VisitResult visitLAndExpNodeForSingle(LAndExpNodeForSingle elm) {
        var rt = new VisitResult();

        var r = visitEqExpNode(elm.eqExp);
        if (!(r.irValue instanceof ICmpInst)) {
            r.irValue = currBasicBlock.createICmpInst(ICmpInstCond.NE, new ImmediateValue(0), r.irValue);
        }
        currBasicBlock.createBrInstWithCond(r.irValue, null, null);
        rt.andBlocks.add(currBasicBlock);
        currBasicBlock = currFunction.createBasicBlock();
        currBasicBlock.setLoopNum(isInLoop);

        return rt;
    }

    public VisitResult visitLAndExpNode(LAndExpNode elm) {
        if (elm instanceof  LAndExpNodeForDouble) {
            return visitLAndExpNodeForDouble((LAndExpNodeForDouble) elm);
        } else {
            return visitLAndExpNodeForSingle((LAndExpNodeForSingle) elm);
        }
    }

    public VisitResult visitLOrExpNodeForDouble(LOrExpNodeForDouble elm) {
        var rt = new VisitResult();

        var r1 = visitLOrExpNode(elm.lOrExp);
        rt.blocksToTrue.addAll(r1.blocksToTrue);

        var r2 = visitLAndExpNode(elm.lAndExp);
        rt.blocksToTrue.add(r2.andBlocks.get(r2.andBlocks.size()-1));
        var firstAndBlock = r2.andBlocks.get(0);
        for (var nearAndBlock : r1.nearAndBlocks) {
            var brInst = (BrInst)nearAndBlock.getInstructions().get(nearAndBlock.getInstructions().size()-1);
            brInst.setFalseBranch(firstAndBlock);
        }
        rt.nearAndBlocks.addAll(r2.andBlocks);
        rt.blocksToFalse.addAll(rt.nearAndBlocks);

        return rt;
    }

    public VisitResult visitLOrExpNodeForSingle(LOrExpNodeForSingle elm) {
        var rt = new VisitResult();

        var r = visitLAndExpNode(elm.lAndExp);
        rt.blocksToTrue.add(r.andBlocks.get(r.andBlocks.size()-1));
        rt.nearAndBlocks.addAll(r.andBlocks);
        rt.blocksToFalse.addAll(rt.nearAndBlocks);

        return rt;
    }

    public VisitResult visitLOrExpNode(LOrExpNode elm) {
        if (elm instanceof LOrExpNodeForDouble) {
            return visitLOrExpNodeForDouble((LOrExpNodeForDouble) elm);
        } else {
            return visitLOrExpNodeForSingle((LOrExpNodeForSingle) elm);
        }
    }

    public VisitResult visitLValNode(LValNode elm) {
        var rt = new VisitResult();

        var sym = currTable.getSymbol(elm.ident);
        if (sym == null) {
            errorRecorder.addError(CompileErrorType.UNDEFINED_NAME, elm.identLineNum);
            rt.expType.type = "int";
            return rt;
        }
        var varSym = (VarSymbol) sym;

        List<Integer> accessDims = new ArrayList<>();
        List<Value> irVisitDims = new ArrayList<>();
        for (var dim : elm.dimensions) {
            var rtExp = visitExpNode(dim);
            accessDims.add(rtExp.constVal);
            irVisitDims.add(rtExp.irValue);
        }

        List<Integer> typeDims = new ArrayList<>();
        for (int i = accessDims.size(); i < varSym.varType.dims.size(); i++) {
            typeDims.add(varSym.varType.dims.get(i));
        }
        if (!typeDims.isEmpty()) {
            typeDims.set(0, null);
        }

        Type type = new Type();
        type.type = "int";
        type.dims.addAll(typeDims);

        rt.expType = type;
        if (varSym.isConst) {
            if (!varSym.isArray()) {
                rt.constVal = varSym.values.get(0);
            } else {
                int accessIdx = 0;
                int stride = 1;
                boolean validConst = true;
                for (int i = accessDims.size() - 1, j = varSym.varType.dims.size()-1; i >= 0; i--, j--) {
                    var accessDim = accessDims.get(i);
                    if (accessDim == null) {
                        validConst = false;
                        break;
                    }
                    accessIdx += accessDim * stride;
                    stride *= varSym.varType.dims.get(j);
                }
                if (validConst) {
                    rt.constVal = varSym.values.get(accessIdx);
                }
            }
        }

        if (currBasicBlock != null) {

            if (varSym.isArray()) {
                var dims = varSym.varType.dims;

                Value arrayPtr;
                Value symValue = varSym.targetValue;
                if (symValue instanceof AllocaInst allcaSymVal && allcaSymVal.getDataType().getPtrNum() != 0) {
                    arrayPtr = currBasicBlock.createLoadInst(symValue);
                } else {
                    arrayPtr = currBasicBlock.createGetElementPtrInst(symValue, List.of(new ImmediateValue(0), new ImmediateValue(0)));
                }

                for (int i = 0; i < irVisitDims.size(); i++) {
                    var visitDim = irVisitDims.get(i);
                    var offsets = (i == dims.size() - 1) ? List.of(visitDim) : List.of(visitDim, new ImmediateValue(0));
                    arrayPtr = currBasicBlock.createGetElementPtrInst(arrayPtr, offsets);
                }

                rt.irValue = arrayPtr;
                rt.lvalLoadNotNeed = dims.size() != irVisitDims.size();
            } else {
                if (varSym.isConst) {
                    rt.irValue = new ImmediateValue(varSym.values.get(0));
                    rt.lvalLoadNotNeed = true;
                } else {
                    rt.irValue = varSym.targetValue;
                }
            }
        }

        return rt;
    }

    public void visitMainFuncDefNode(MainFuncDefNode elm) {
        var sym = new FunctionSymbol();
        sym.retType.type = "int";
        sym.ident = "main";
        currTable.insertSymbol(sym);

        currTable = currTable.createSubTable();

        isRetExpNotNeed = false;

        ArrayList<IRType> irArgTypes = new ArrayList<>();
        currFunction = irModule.createFunction(IRType.getInt(), irArgTypes);
        currFunction.setName("main");
        sym.targetValue = currFunction;
        currBasicBlock = currFunction.createBasicBlock();
        currBasicBlock.setLoopNum(isInLoop);

        visitBlockNode(elm.mainBlock);

        if (!sym.retType.type.equals("void") && elm.mainBlock.isWithoutReturn()) {
            errorRecorder.addError(CompileErrorType.RETURN_IS_MISSING, elm.mainBlock.blockRLineNum);
        }

        currBasicBlock = null;
        currFunction = null;

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
        if (currBasicBlock != null) {
            if (elm.op == LexType.MULT) {
                rt.irValue = currBasicBlock.createMulInst(r1.irValue, r2.irValue);
            } else if (elm.op == LexType.DIV) {
                rt.irValue = currBasicBlock.createSDivInst(r1.irValue, r2.irValue);
            } else {
                rt.irValue = currBasicBlock.createSRemInst(r1.irValue, r2.irValue);
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
        rt.irValue = new ImmediateValue(rt.constVal);
        return rt;
    }

    public VisitResult visitPrimaryExpNodeForExp(PrimaryExpNodeForExp elm) {
        return visitExpNode(elm.exp);
    }

    public VisitResult visitPrimaryExpNodeForLVal(PrimaryExpNodeForLVal elm) {
        var r = visitLValNode(elm.lVal);
        if (currBasicBlock != null) {
            if (r.lvalLoadNotNeed) {
                return r;
            }
            r.irValue = currBasicBlock.createLoadInst(r.irValue);
        }
        return r;
    }

    public VisitResult visitPrimaryExpNodeForNumber(PrimaryExpNodeForNumber elm) {
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

    public VisitResult visitRelExpNodeForDouble(RelExpNodeForDouble elm) {
        var rt = new VisitResult();
        var r1 = visitRelExpNode(elm.relExp);
        var r2 = visitAddExpNode(elm.addExp);

        if (r1.irValue instanceof ICmpInst) {
            r1.irValue = currBasicBlock.createZExtInst(IRType.getInt(), r1.irValue);
        }
        if (r2.irValue instanceof ICmpInst) {
            r2.irValue = currBasicBlock.createZExtInst(IRType.getInt(), r2.irValue);
        }

        ICmpInstCond cond = switch (elm.op) {
            case LSS -> ICmpInstCond.SLT;
            case GRE -> ICmpInstCond.SGT;
            case LEQ -> ICmpInstCond.SLE;
            case GEQ -> ICmpInstCond.SGE;
            default -> null; // impossible
        };

        rt.irValue = currBasicBlock.createICmpInst(cond, r1.irValue, r2.irValue);
        return rt;
    }

    public VisitResult visitRelExpNodeForSingle(RelExpNodeForSingle elm) {
        return visitAddExpNode(elm.addExp);
    }

    public VisitResult visitRelExpNode(RelExpNode elm) {
        if (elm instanceof RelExpNodeForDouble) {
            return visitRelExpNodeForDouble((RelExpNodeForDouble) elm);
        } else {
            return visitRelExpNodeForSingle((RelExpNodeForSingle) elm);
        }
    }

    public void visitStmtNodeForAssign(StmtNodeForAssign elm) {
        var rtLVal = visitLValNode(elm.lVal);
        var lValSym = currTable.getSymbol(elm.lVal.ident);
        if (lValSym instanceof VarSymbol lValVarSym && lValVarSym.isConst) {
            errorRecorder.addError(CompileErrorType.TRY_TO_CHANGE_VAL_OF_CONST, elm.lVal.identLineNum);
        }

        var rtExp = visitExpNode(elm.exp);
        currBasicBlock.createStoreInst(rtExp.irValue, rtLVal.irValue);
    }

    public void visitStmtNodeForBlock(StmtNodeForBlock elm) {
        currTable = currTable.createSubTable();
        visitBlockNode(elm.block);
        currTable = currTable.getPreTable();
    }

    public void visitStmtNodeForContinueBreak(StmtNodeForContinueBreak elm) {
        if (isInLoop == 0) {
            errorRecorder.addError(CompileErrorType.BREAK_OR_CONTINUE_NOT_IN_LOOP, elm.tkLineNum);
            return;
        }

        if (elm.type == LexType.CONTINUETK) {
            continueBrInsts.peek().add((BrInst) currBasicBlock.createBrInstWithoutCond(null));
            currBasicBlock = currFunction.createBasicBlock();
            currBasicBlock.setLoopNum(isInLoop);

        } else if (elm.type == LexType.BREAKTK) {
            breakBrInsts.peek().add((BrInst) currBasicBlock.createBrInstWithoutCond(null));
            currBasicBlock = currFunction.createBasicBlock();
            currBasicBlock.setLoopNum(isInLoop);

        } else {
            assert false; // impossible
        }
    }

    public void visitStmtNodeForExp(StmtNodeForExp elm) {
        if (elm.exp != null) {
            visitExpNode(elm.exp);
        }
    }

    public void visitStmtNodeForGetInt(StmtNodeForGetInt elm) {
        var r = visitLValNode(elm.lVal);
        var lValSym = currTable.getSymbol(elm.lVal.ident);
        if (lValSym instanceof VarSymbol lValVarSym && lValVarSym.isConst) {
            errorRecorder.addError(CompileErrorType.TRY_TO_CHANGE_VAL_OF_CONST, elm.lVal.identLineNum);
        }
        var getIntVal = currBasicBlock.createCallInst(Function.BUILD_IN_GETINT, List.of());
        currBasicBlock.createStoreInst(getIntVal, r.irValue);
    }

    public void visitStmtNodeForIfElse(StmtNodeForIfElse elm) {
        var r = visitCondNode(elm.cond);

        var trueBlock = currBasicBlock;
        visitStmtNode(elm.ifStmt);

        var lastBlockInTrue = currBasicBlock;
        currBasicBlock = currFunction.createBasicBlock();
        currBasicBlock.setLoopNum(isInLoop);

        var falseBlock = currBasicBlock;

        if (elm.elseStmt != null) {
            visitStmtNode(elm.elseStmt);
            var lastBlockInFalse = currBasicBlock;
            currBasicBlock = currFunction.createBasicBlock();
            currBasicBlock.setLoopNum(isInLoop);

            lastBlockInFalse.createBrInstWithoutCond(currBasicBlock);
        }
        lastBlockInTrue.createBrInstWithoutCond(currBasicBlock);

        for (var blockToTrue : r.blocksToTrue) {
            var brInst = (BrInst)blockToTrue.getInstructions().get(blockToTrue.getInstructions().size()-1);
            brInst.setTrueBranch(trueBlock);
        }

        for (var blockToFalse : r.blocksToFalse) {
            var brInst = (BrInst)blockToFalse.getInstructions().get(blockToFalse.getInstructions().size()-1);
            brInst.setFalseBranch(falseBlock);
        }
    }

    public void visitStmtNodeForLoop(StmtNodeForLoop elm) {
        breakBrInsts.push(new ArrayList<>());
        continueBrInsts.push(new ArrayList<>());

        var forStmt1Block = currBasicBlock;
        if (elm.forStmt1 != null) {
            visitForStmtNode(elm.forStmt1);
        }

        currBasicBlock = currFunction.createBasicBlock();
        currBasicBlock.setLoopNum(isInLoop);

        forStmt1Block.createBrInstWithoutCond(currBasicBlock);
        var loopEntryBlock = currBasicBlock;

        var condRt = new VisitResult();
        if (elm.cond != null) {
            condRt = visitCondNode(elm.cond);
        }
        var stmtBlock = currBasicBlock;

        isInLoop++;
        visitStmtNode(elm.stmt);
        isInLoop--;

        var lastBlockInStmt = currBasicBlock;
        currBasicBlock = currFunction.createBasicBlock();
        currBasicBlock.setLoopNum(isInLoop);

        lastBlockInStmt.createBrInstWithoutCond(currBasicBlock);
        var forStmt2Block = currBasicBlock;

        if (elm.forStmt2 != null) {
            visitForStmtNode(elm.forStmt2);
        }
        forStmt2Block.createBrInstWithoutCond(loopEntryBlock);

        currBasicBlock = currFunction.createBasicBlock();
        currBasicBlock.setLoopNum(isInLoop);

        var loopExitBlock = currBasicBlock;

        for (var blockToTrue : condRt.blocksToTrue) {
            var brInst = (BrInst)blockToTrue.getInstructions().get(blockToTrue.getInstructions().size()-1);
            brInst.setTrueBranch(stmtBlock);
        }

        for (var blockToFalse : condRt.blocksToFalse) {
            var brInst = (BrInst)blockToFalse.getInstructions().get(blockToFalse.getInstructions().size()-1);
            brInst.setFalseBranch(loopExitBlock);
        }

        for (var continueBrInst : continueBrInsts.pop()) {
            continueBrInst.setDest(forStmt2Block);
        }

        for (var breakBrInst : breakBrInsts.pop()) {
            breakBrInst.setDest(loopExitBlock);
        }
    }

    public void visitStmtNodeForPrintf(StmtNodeForPrintf elm) {
        List<Value> expValues = new ArrayList<>();
        for (var exp : elm.exps) {
           expValues.add(visitExpNode(exp).irValue);
        }

        var fCharNum = (elm.formatString.length() - String.join("", elm.formatString.split("%d")).length()) / 2;
        if (fCharNum != elm.exps.size()) {
            errorRecorder.addError(CompileErrorType.NUM_OF_PARAM_IN_PRINTF_NOT_MATCH, elm.printfLineNum);
            return;
        }

        try {
            for (int i = 1, j = 0; i < elm.formatString.length() - 1; i++) {
                char ch = elm.formatString.charAt(i);
                if (ch == '%') {
                    currBasicBlock.createCallInst(Function.BUILD_IN_PUTINT, List.of(expValues.get(j++)));
                    i++;
                } else if (ch == '\\') {
                    currBasicBlock.createCallInst(Function.BUILD_IN_PUTCH, List.of(new ImmediateValue('\n')));
                    i++;
                } else {
                    currBasicBlock.createCallInst(Function.BUILD_IN_PUTCH, List.of(new ImmediateValue(ch)));
                }
            }
        } catch (IndexOutOfBoundsException e) {
            return; // exception means that the format string is illegal
        }
    }

    public void visitStmtNodeForReturn(StmtNodeForReturn elm) {
        Value expIr = null;
        if (elm.exp != null) {
            if (isRetExpNotNeed) {
                errorRecorder.addError(CompileErrorType.RETURN_NOT_MATCH, elm.returnLineNum);
                return;
            }
            var result = visitExpNode(elm.exp);
            expIr = result.irValue;
        }
        currBasicBlock.createReturnInst(expIr);
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
            var r = visitFuncRParamsNode(elm.params);

            if (elm.params.exps.size() != funcSym.paramTypeList.size()) {
                errorRecorder.addError(CompileErrorType.NUM_OF_PARAM_NOT_MATCH, elm.identLineNum);
                return rt;
            }

            for (int i = 0; i < funcSym.paramTypeList.size(); i++) {
                if (!funcSym.paramTypeList.get(i).equals(r.paramTypes.get(i))) {
                    errorRecorder.addError(CompileErrorType.TYPE_OF_PARAM_NOT_MATCH, elm.identLineNum);
                }
            }

            rt.irValue = currBasicBlock.createCallInst((Function) funcSym.targetValue, r.irValues);
        } else {
            if (!funcSym.paramTypeList.isEmpty()) {
                errorRecorder.addError(CompileErrorType.NUM_OF_PARAM_NOT_MATCH, elm.identLineNum);
                return rt;
            }

            rt.irValue = currBasicBlock.createCallInst((Function) funcSym.targetValue, List.of());
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
        if (val != null) {
            var op = visitUnaryOpNode(elm.op);
            if (op == LexType.MINU) {
                rt.constVal = -val;
            } else if (op == LexType.PLUS) {
                rt.constVal = val;
            } else {
                rt.constVal = val == 0 ? 0 : 1;
            }
        }
        if (currBasicBlock != null) {
            var op = visitUnaryOpNode(elm.op);
            if (op == LexType.MINU) {
                rt.irValue = currBasicBlock.createSubInst(new ImmediateValue(0), r.irValue);
            } else if (op == LexType.PLUS) {
                rt.irValue = r.irValue; // if op is +, do nothing
            } else {
                rt.irValue = currBasicBlock.createICmpInst(ICmpInstCond.EQ, new ImmediateValue(0), r.irValue);
            }
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

        if (isGlobalVar) {
            if (elm.initVal != null) {
                var r = visitInitValNode(elm.initVal);
                varSym.values.addAll(r.constInitVals);
            }
            var globalVar = irModule.createGlobalValue(IRType.getInt().dims(varSym.varType.dims), varSym.values);
            globalVar.setName(varSym.ident);
            varSym.targetValue = globalVar;
        } else {
            var localVar = currFunction.getFirstBasicBlock().createAllocaInstAndInsertToFront(IRType.getInt().dims(varSym.varType.dims));
            varSym.targetValue = localVar;
            if (elm.initVal != null) {
                var r = visitInitValNode(elm.initVal);
                var initValues = r.irValues;
                if (!varSym.isArray()) {
                    currBasicBlock.createStoreInst(r.irValues.get(0), varSym.targetValue);
                } else { // array with init values
                    for (int i = 0; i < initValues.size(); i++) {
                        int[] idxs = new int[varSym.varType.dims.size()];
                        var pos = i;
                        for (int j = idxs.length - 1; j >= 0; j--) {
                            idxs[j] = pos % varSym.varType.dims.get(j);
                            pos /= varSym.varType.dims.get(j);
                        }

                        var initVal = initValues.get(i);
                        var arrayPtr = currBasicBlock.createGetElementPtrInst(varSym.targetValue, List.of(new ImmediateValue(0), new ImmediateValue(0)));
                        for (int j = 0; j < idxs.length; j++) {
                            var visitIdx = idxs[j];
                            List<Value> offsets = j == idxs.length - 1 ? List.of(new ImmediateValue(visitIdx)) : List.of(new ImmediateValue(visitIdx), new ImmediateValue(0));
                            arrayPtr = currBasicBlock.createGetElementPtrInst(arrayPtr, offsets);
                        }
                        currBasicBlock.createStoreInst(initVal, arrayPtr);
                    }
                }
            }
        }

        currTable.insertSymbol(varSym);
    }
}
