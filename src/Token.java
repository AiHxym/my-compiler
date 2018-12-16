public class Token {
    private SymType type; //The type of token
    private int line; //The line of token, which is used for error handling
    private String value; //The value of token

    public Token(SymType type, int line, String value) {
        this.type = type;
        this.line = line;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public SymType getType() {
        return type;
    }

    public void setType(SymType type) {
        this.type = type;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }
}
