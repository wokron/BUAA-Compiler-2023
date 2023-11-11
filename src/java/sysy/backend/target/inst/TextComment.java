package sysy.backend.target.inst;

import sysy.backend.ir.inst.Instruction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class TextComment extends TextEntry {
    private final String comment;

    public TextComment(String comment) {
        this.comment = comment;
    }

    public TextComment(Instruction inst) {
        String printText;

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             PrintStream printStream = new PrintStream(outputStream, true))
        {
            inst.dump(printStream);

            printText = outputStream.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.comment = printText.substring(0, printText.length()-1);
    }

    @Override
    public String toString() {
        return "# " + comment;
    }
}
