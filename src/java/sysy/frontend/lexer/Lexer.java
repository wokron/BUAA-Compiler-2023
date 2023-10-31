package sysy.frontend.lexer;

import sysy.error.CompileErrorType;
import sysy.error.ErrorRecorder;
import sysy.exception.LexerException;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public class Lexer {
    private final Reader in;
    private final ErrorRecorder errorRecorder;
    private int pushbackChar;
    private boolean hasPushbackChar = false;
    private Token token = null;
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
    private static final int EOF = -1;
    private int lineNum = 1;

    private static boolean isDigit(int ch) {
        return '0' <= ch && ch <= '9';
    }

    private static boolean isWord(int ch) {
        return ('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z') || ch == '_';
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

    public Lexer(Reader in, ErrorRecorder errorRecorder) {
        this.in = in;
        this.errorRecorder = errorRecorder;
    }

    public boolean next() throws LexerException {
        String value;
        LexType type;
        StringBuilder sb = new StringBuilder();
        int ch = getChar();
        sb.append((char) ch);

        if (isWord(ch)) {
            ch = getChar();
            while (isWord(ch) || isDigit(ch)) {
                sb.append((char) ch);
                ch = getChar();
            }
            ungetChar(ch);
            value = sb.toString();
            type = reserveWords.getOrDefault(value, LexType.IDENFR);
            token = new Token(value, type, lineNum);
        } else if (isNonZeroDigit(ch)) {
            ch = getChar();
            while (isDigit(ch)) {
                sb.append((char) ch);
                ch = getChar();
            }
            ungetChar(ch);
            value = sb.toString();
            type = LexType.INTCON;
            token = new Token(value, type, lineNum);
        } else if (ch == '0') {
            value = sb.toString();
            type = LexType.INTCON;
            token = new Token(value, type, lineNum);
        } else if (ch == '\"') {
            ch = getChar();
            while (ch != EOF && ch != '\"') {
                sb.append((char) ch);
                if (ch == '%') {
                    ch = getChar();
                    if (ch == 'd') {
                        sb.append((char) ch);
                    } else {
                        errorRecorder.addError(CompileErrorType.ILLEGAL_SYMBOL, lineNum);
                    }
                    if (ch == '\"') {
                        break;
                    }
                } else if (ch == '\\') {
                    ch = getChar();
                    if (ch == 'n') {
                        sb.append((char) ch);
                    } else {
                        errorRecorder.addError(CompileErrorType.ILLEGAL_SYMBOL, lineNum);
                    }
                    if (ch == '\"') {
                        break;
                    }
                } else if (!isNormalChar(ch)) {
                    errorRecorder.addError(CompileErrorType.ILLEGAL_SYMBOL, lineNum);
                }
                ch = getChar();
            }
            if (ch == '\"') {
                sb.append((char) ch);
                value = sb.toString();
                type = LexType.STRCON;
                token = new Token(value, type, lineNum);
            } else {
                throw new LexerException();
            }
        } else if (ch == '!') {
            ch = getChar();
            if (ch == '=') {
                sb.append((char) ch);
                value = sb.toString();
                type = LexType.NEQ;
                token = new Token(value, type, lineNum);
            } else {
                ungetChar(ch);
                value = sb.toString();
                type = LexType.NOT;
                token = new Token(value, type, lineNum);
            }
        } else if (ch == '&') {
            ch = getChar();
            if (ch == '&') {
                sb.append((char) ch);
                value = sb.toString();
                type = LexType.AND;
                token = new Token(value, type, lineNum);
            } else {
                throw new LexerException();
            }
        } else if (ch == '|') {
            ch = getChar();
            if (ch == '|') {
                sb.append((char) ch);
                value = sb.toString();
                type = LexType.OR;
                token = new Token(value, type, lineNum);
            } else {
                throw new LexerException();
            }
        } else if (ch == '+') {
            value = sb.toString();
            type = LexType.PLUS;
            token = new Token(value, type, lineNum);
        } else if (ch == '-') {
            value = sb.toString();
            type = LexType.MINU;
            token = new Token(value, type, lineNum);
        } else if (ch == '*') {
            value = sb.toString();
            type = LexType.MULT;
            token = new Token(value, type, lineNum);
        } else if (ch == '/') {
            ch = getChar();
            if (ch == '/') {
                sb.append((char) ch);
                ch = getChar();
                while (ch != '\n' && ch != EOF) {
                    sb.append((char) ch);
                    ch = getChar();
                }
                ungetChar(ch);
                return next();  // note is not a token
            } else if (ch == '*') {
                sb.append((char) ch);
                ch = getChar();
                while (ch != EOF) {
                    while (ch != EOF && ch != '*') {
                        sb.append((char) ch);
                        if (ch == '\n') {
                            lineNum++;
                        }
                        ch = getChar();
                    }
                    while (ch == '*') {
                        sb.append((char) ch);
                        ch = getChar();
                    }
                    if (ch == '/') {
                        sb.append((char) ch);
                        return next(); // note is not a token
                    }
                }
                ungetChar(ch);
            } else {
                ungetChar(ch);
                value = sb.toString();
                type = LexType.DIV;
                token = new Token(value, type, lineNum);
            }
        } else if (ch == '%') {
            value = sb.toString();
            type = LexType.MOD;
            token = new Token(value, type, lineNum);
        } else if (ch == '<') {
            ch = getChar();
            if (ch == '=') {
                sb.append((char) ch);
                value = sb.toString();
                type = LexType.LEQ;
                token = new Token(value, type, lineNum);
            } else {
                ungetChar(ch);
                value = sb.toString();
                type = LexType.LSS;
                token = new Token(value, type, lineNum);
            }
        } else if (ch == '>') {
            ch = getChar();
            if (ch == '=') {
                sb.append((char) ch);
                value = sb.toString();
                type = LexType.GEQ;
                token = new Token(value, type, lineNum);
            } else {
                ungetChar(ch);
                value = sb.toString();
                type = LexType.GRE;
                token = new Token(value, type, lineNum);
            }
        } else if (ch == '=') {
            ch = getChar();
            if (ch == '=') {
                sb.append((char) ch);
                value = sb.toString();
                type = LexType.EQL;
                token = new Token(value, type, lineNum);
            } else {
                ungetChar(ch);
                value = sb.toString();
                type = LexType.ASSIGN;
                token = new Token(value, type, lineNum);
            }
        } else if (ch == ';') {
            value = sb.toString();
            type = LexType.SEMICN;
            token = new Token(value, type, lineNum);
        } else if (ch == ',') {
            value = sb.toString();
            type = LexType.COMMA;
            token = new Token(value, type, lineNum);
        } else if (ch == '(') {
            value = sb.toString();
            type = LexType.LPARENT;
            token = new Token(value, type, lineNum);
        } else if (ch == ')') {
            value = sb.toString();
            type = LexType.RPARENT;
            token = new Token(value, type, lineNum);
        } else if (ch == '[') {
            value = sb.toString();
            type = LexType.LBRACK;
            token = new Token(value, type, lineNum);
        } else if (ch == ']') {
            value = sb.toString();
            type = LexType.RBRACK;
            token = new Token(value, type, lineNum);
        } else if (ch == '{') {
            value = sb.toString();
            type = LexType.LBRACE;
            token = new Token(value, type, lineNum);
        } else if (ch == '}') {
            value = sb.toString();
            type = LexType.RBRACE;
            token = new Token(value, type, lineNum);
        } else if (ch == '\n') {
            lineNum++;
            return next();
        } else if (isWhitespaceWithoutNextLine(ch)) {
            return next();
        } else if (ch == EOF) {
            ungetChar(EOF);  // EOF will always in the stream
            return false;
        } else {
            throw new LexerException();
        }
        return true;
    }

    private int getChar() throws LexerException {
        try {
            if (hasPushbackChar) {
                hasPushbackChar = false;
                return pushbackChar;
            } else {
                int chByte = in.read();
                return chByte;
            }
        } catch (IOException e) {
            throw new LexerException(e);
        }
    }

    private void ungetChar(int chByte) {
        hasPushbackChar = true;
        pushbackChar = chByte;
    }

    public Token getToken() {
        assert token != null;
        return token;
    }

    public int getLineNum() {
        return lineNum;
    }
}
