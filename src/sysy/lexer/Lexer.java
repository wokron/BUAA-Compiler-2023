package sysy.lexer;

import sysy.exception.LexerException;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public class Lexer {
    private Reader in;
    private int pushbackChar;
    private boolean hasPushbackChar = false;
    private String token = null;
    private LexType type = null;
    private final static Map<String, LexType> reserveWords = Map.ofEntries(
            Map.entry("main", LexType.MAINTK),
            Map.entry("const", LexType.CONSTTK),
            Map.entry("int", LexType.INTTK),
            Map.entry("break", LexType.BREAKTK),
            Map.entry("continue", LexType.CONTINUETK),
            Map.entry("if", LexType.IFTK),
            Map.entry("else", LexType.ELSETK),
            Map.entry("for", LexType.FORTK),
            Map.entry("getint", LexType.GETINTTK),
            Map.entry("printf", LexType.PRINTFTK),
            Map.entry("return", LexType.RETURNTK),
            Map.entry("void", LexType.VOIDTK)
    );
    private int lineNum = 0;

    private static boolean isDigit(int ch) {
        return '0' <= ch && ch <= '9';
    }

    private static boolean isWord(int ch) {
        return  ('a' <= ch && ch <= 'z') || ch == '_';
    }

    private static boolean isNonZeroDigit(int ch) {
        return '1' <= ch && ch <= '9';
    }

    private static boolean isNormalChar(int ch) {
        return ch == 32 || ch == 33 || (40 <= ch && ch <= 126);
    }

    private static boolean isWhitespaceWithoutNextLine(int ch) {
        return Character.isWhitespace(ch) && ch != '\n';
    }

    public Lexer(Reader in) {
        this.in = in;
    }

    public boolean next() throws IOException, LexerException {
        StringBuilder sb = new StringBuilder();
        int ch = getChar();
        sb.append((char)ch);

        if (isWord(ch)) {
            ch = getChar();
            while (isWord(ch) || isDigit(ch)) {
                sb.append((char)ch);
                ch = getChar();
            }
            ungetChar(ch);
            token = sb.toString();
            type = reserveWords.getOrDefault(token, LexType.IDENFR);
        } else if (isNonZeroDigit(ch)) {
            ch = getChar();
            while (isDigit(ch)) {
                sb.append((char)ch);
                ch = getChar();
            }
            ungetChar(ch);
            token = sb.toString();
            type = LexType.INTCON;
        } else if (ch == '0') {
            token = sb.toString();
            type = LexType.INTCON;
        } else if (ch == '\"') {
            ch = getChar();
            while (ch == '%' || ch == '\\' || isNormalChar(ch)) {
                sb.append((char)ch);
                if (ch == '%') {
                    ch = getChar();
                    if (ch == 'd') {
                        sb.append((char)ch);
                    } else {
                        throw new LexerException();
                    }
                } else if (ch == '\\') {
                    ch = getChar();
                    if (ch == 'n') {
                        sb.append((char)ch);
                    } else {
                        throw new LexerException();
                    }
                }
                ch = getChar();
            }
            if (ch == '\"') {
                sb.append((char)ch);
                token = sb.toString();
                type = LexType.STRCON;
            } else {
                throw new LexerException();
            }
        } else if (ch == '!') {
            ch = getChar();
            if (ch == '=') {
                sb.append((char)ch);
                token = sb.toString();
                type = LexType.NEQ;
            } else {
                ungetChar(ch);
                token = sb.toString();
                type = LexType.NOT;
            }
        } else if (ch == '&') {
            ch = getChar();
            if (ch == '&') {
                sb.append((char)ch);
                token = sb.toString();
                type = LexType.AND;
            } else {
                throw new LexerException();
            }
        } else if (ch == '|') {
            ch = getChar();
            if (ch == '|') {
                sb.append((char)ch);
                token = sb.toString();
                type = LexType.OR;
            } else {
                throw new LexerException();
            }
        } else if (ch == '+') {
            token = sb.toString();
            type = LexType.PLUS;
        } else if (ch == '-') {
            token = sb.toString();
            type = LexType.MINU;
        } else if (ch == '*') {
            token = sb.toString();
            type = LexType.MULT;
        } else if (ch == '/') {
            ch = getChar();
            if (ch == '/') {
                sb.append((char)ch);
                ch = getChar();
                while (ch != '\n' && ch != -1) {
                    sb.append((char)ch);
                    ch = getChar();
                }
                ungetChar(ch);
                return next();  // note is not a token
            } else if (ch == '*') {
                sb.append((char)ch);
                ch = getChar();
                while (ch != -1) {
                    while (ch != -1 && ch != '*') {
                        sb.append((char)ch);
                        if (ch == '\n') {
                            lineNum++;
                        }
                        ch = getChar();
                    }
                    while (ch != -1 && ch == '*') {
                        sb.append((char)ch);
                        ch = getChar();
                    }
                    if (ch != -1 && ch == '/') {
                        sb.append((char)ch);
                        return next(); // note is not a token
                    }
                }
                ungetChar(ch);
            } else {
                ungetChar(ch);
                token = sb.toString();
                type = LexType.DIV;
            }
        } else if (ch == '%') {
            token = sb.toString();
            type = LexType.MOD;
        } else if (ch == '<') {
            ch = getChar();
            if (ch == '=') {
                sb.append((char)ch);
                token = sb.toString();
                type = LexType.LEQ;
            } else {
                ungetChar(ch);
                token = sb.toString();
                type = LexType.LSS;
            }
        } else if (ch == '>') {
            ch = getChar();
            if (ch == '=') {
                sb.append((char)ch);
                token = sb.toString();
                type = LexType.GEQ;
            } else {
                ungetChar(ch);
                token = sb.toString();
                type = LexType.GRE;
            }
        } else if (ch == '=') {
            ch = getChar();
            if (ch == '=') {
                sb.append((char)ch);
                token = sb.toString();
                type = LexType.EQL;
            } else {
                ungetChar(ch);
                token = sb.toString();
                type = LexType.ASSIGN;
            }
        } else if (ch == ';') {
            token = sb.toString();
            type = LexType.SEMICN;
        } else if (ch == ',') {
            token = sb.toString();
            type = LexType.COMMA;
        } else if (ch == '(') {
            token = sb.toString();
            type = LexType.LPARENT;
        } else if (ch == ')') {
            token = sb.toString();
            type = LexType.RPARENT;
        } else if (ch == '[') {
            token = sb.toString();
            type = LexType.LBRACK;
        } else if (ch == ']') {
            token = sb.toString();
            type = LexType.RBRACK;
        } else if (ch == '{') {
            token = sb.toString();
            type = LexType.LBRACE;
        } else if (ch == '}') {
            token = sb.toString();
            type = LexType.RBRACE;
        } else if (ch == '\n') {
            lineNum++;
            return next();
        } else if (isWhitespaceWithoutNextLine(ch)) {
            return next();
        } else if (ch == -1) {
            return false;
        } else {
            throw new LexerException();
        }
        return true;
    }

    private int getChar() throws IOException{
        if (hasPushbackChar) {
            hasPushbackChar = false;
            return pushbackChar;
        } else {
            int chByte = in.read();
            return chByte;
        }
    }

    private void ungetChar(int chByte) {
        hasPushbackChar = true;
        pushbackChar = chByte;
    }

    public String getToken() {
        assert token != null;  // call after next is required
        return token;
    }

    public LexType getLexType() {
        assert type != null;  // CALL AFTER NEXT IS REQUIRED
        return type;
    }

    public int getLineNum() {
        return lineNum;
    }
}
