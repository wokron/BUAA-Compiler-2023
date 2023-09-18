import lexer.Lexer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Compiler {
    public static void main(String[] args) throws IOException {
        String code = """
                const int array[2] = {1,2};
                                
                int main(){
                    int c;
                    c = getint();
                    printf("output is %d",c);
                    return c;
                }
                """;
        InputStream is = new ByteArrayInputStream(code.getBytes());

        var lexer = new Lexer(new InputStreamReader(is));

        while (lexer.next()) {
            System.out.printf("%s %s\n", lexer.getLexType().name(), lexer.getToken());
        }
    }
}
