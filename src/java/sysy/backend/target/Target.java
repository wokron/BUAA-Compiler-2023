package sysy.backend.target;

import sysy.backend.target.inst.TextComment;
import sysy.backend.target.inst.TextEntry;
import sysy.backend.target.inst.TextInst;
import sysy.backend.target.inst.TextLabel;
import sysy.backend.target.value.Offset;
import sysy.backend.target.value.Register;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Target {
    private final List<Data> dataList = new ArrayList<>();
    private final List<TextEntry> textList = new ArrayList<>();

    public void addData(Data data) {
        dataList.add(data);
    }

    public void  addText(TextEntry entry) {
        textList.add(entry);
    }

    public void dump(PrintStream out, boolean debugMode) {
        out.print(".data\n");
        for (var data: dataList) {
            out.print(data);
            out.print("\n");
        }
        out.println();

        out.print(".text\n");
        out.print("\tla $ra end.end\n");
        out.print("\tj main\n");

        int count = 0;
        for (var text : textList) {
            if (text instanceof TextComment && !debugMode) {
                continue;
            }
            if (text instanceof TextInst || text instanceof TextComment)
                out.print("\t");
            out.print(text);
            out.print("\n");
        }
        out.print("end.end:");
    }

    public static void main(String[] args) {
        var target = new Target();
        target.addData(new Data("a", "word", List.of(1, 2, 3, 4, 5)));
        target.addData(new Data("str", "asciiz", List.of("\"this is string\"")));

        target.addText(new TextInst("add", new Register("t0"), new Register("t1"), new Register("t2")));
        target.addText(new TextInst("sub", new Register("t0"), new Offset(new Register("t1"), -4)));
        target.addText(new TextInst("erjfe", new Register("t0")));
        target.addText(new TextLabel("label1"));

        target.dump(System.out, true);
    }
}
