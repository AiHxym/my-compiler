import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//Lexer

public class LexAnalysis {

    private String[] keyWords = {
            "begin", "end", "if", "then", "else", "const", "procedure", "var",
            "do", "while", "call", "read", "write", "odd", "repeat", "until"
    };

    private List<Token> allToken; //Save all tokens
    private char[] buffer; //Save the code
    private char ch = ' '; //The current character
    private int currCharPtr = 0; //The pointer point to the current character
    private int line = 1; //The current line
    private String currStr; //The current string

    public LexAnalysis(File file) {
        allToken = new ArrayList<Token>();
        BufferedReader bf = null;
        try {
            bf = new BufferedReader(new FileReader(file));
            String temp1 = "", temp2 = "";
            while((temp1 = bf.readLine()) != null) {
                temp2 = temp2 + temp1 + String.valueOf('\n');
            }
            buffer = temp2.toCharArray();
            bf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        doAnalysis();
    }

    private void doAnalysis() {
        while (currCharPtr < buffer.length) {
            allToken.add(getToken());
        }
    }

    private Token getToken() {
        currStr = "";
        getChar();
        while ((ch == ' ' || ch == '\n' || ch == '\t' || ch == '\0') && currCharPtr < buffer.length) {
            if (ch == '\n') {
                line++;
            }
            getChar();
        }
        if (ch == '$' && currCharPtr >= buffer.length) {  //At the end of the program
            return new Token(SymType.EOF, line, "-1");
        }
        if (isLetter()) {  //Maybe a keyword or variable
            while (isLetter() || isDigit()) {
                currStr += ch;
                getChar();
            }
            retract();
            for (int i = 0; i < keyWords.length; i++) {
                if (currStr.equals(keyWords[i])) {  //It's keyword
                    return new Token(SymType.values()[i], line, "-");
                }
            }
            //It's not keyword, so it must be variable
            return new Token(SymType.SYM, line, currStr);
        } else if (isDigit()) {  //It's const
            while (isDigit()) {
                currStr += ch;
                getChar();
            }
            retract();
            return new Token(SymType.CONST, line, currStr);
        } else if (ch == '=') {  //equal sign
            return new Token(SymType.EQU, line, "-");
        } else if (ch == '+') {  //plus sign
            return new Token(SymType.ADD, line, "-");
        } else if (ch == '-') { //minus
            return new Token(SymType.SUB, line, "-");
        } else if (ch == '*') { //times sign
            return new Token(SymType.MUL, line, "-");
        } else if (ch == '/') { //division sign
            return new Token(SymType.DIV, line, "-");
        } else if (ch == '#') {  //NE
            return new Token(SymType.NE, line, "-");
        } else if (ch == '<') {  //LT or LTE
            getChar();
            if (ch == '=') {
                return new Token(SymType.LTE, line, "-");
            } else {
                retract();
                return new Token(SymType.LT, line, "-");
            }
        } else if (ch == '>') {  //GT or GTE
            getChar();
            if (ch == '=') {
                return new Token(SymType.GTE, line, "-");
            } else {
                retract();
                return new Token(SymType.GT, line, "-");
            }
        } else if (ch == ',') {  //comma symbol
            return new Token(SymType.COMMA, line, "-");
        } else if (ch == ';') {  //semicolon
            return new Token(SymType.SEMIC, line, "-");
        } else if (ch == '.') {  //point
            return new Token(SymType.POI, line, "-");
        } else if (ch == '(') {  //left bracket
            return new Token(SymType.LBR, line, "-");
        } else if (ch == ')') {  //right bracket
            return new Token(SymType.RBR, line, "-");
        } else if (ch == ':') {  //assignment symbol
            getChar();
            if (ch == '=') {
                return new Token(SymType.CEQU, line, "-");
            } else {
                retract();
                return new Token(SymType.COL, line, "-");
            }
        }
        return new Token(SymType.EOF, line, "-");
    }

    public List<Token> getAllToken() {
        return allToken;
    }

    private char getChar() {
        if (currCharPtr < buffer.length) {
            ch = buffer[currCharPtr];
            currCharPtr++;
        } else {
            ch = '$';
        }
        return ch;
    }

    private void retract() {
        currCharPtr--;
        ch = ' ';
    }

    private boolean isLetter() {
        if(Character.isLetter(ch)) {
            return true;
        }
        return false;
    }

    private boolean isDigit() {
        if(Character.isDigit(ch)) {
            return true;
        }
        return false;
    }
}