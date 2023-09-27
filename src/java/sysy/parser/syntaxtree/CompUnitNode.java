package sysy.parser.syntaxtree;

import sysy.lexer.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CompUnitNode extends SyntaxNode{
    public final List<DeclNode> declares = new ArrayList<>();
    public final List<FuncDefNode> funcs = new ArrayList<>();
    public MainFuncDefNode mainFunc;

    @Override
    public void walk(Consumer<Token> terminalConsumer, Consumer<SyntaxNode> nonTerminalConsumer) {
        for (var declare : declares) {
            declare.walk(terminalConsumer, nonTerminalConsumer);
        }

        for (var func : funcs) {
            func.walk(terminalConsumer, nonTerminalConsumer);
        }

        mainFunc.walk(terminalConsumer, nonTerminalConsumer);

        nonTerminalConsumer.accept(this);
    }
}
