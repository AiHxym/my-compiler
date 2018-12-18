public enum SymType {
    BEGIN, END, IF, THEN, ELS, CON, PROC, VAR, DO, WHILE, CAL, REA, WRI, ODD, REP, UNT,
    //begin, end, if, then, else, const, procedure, var, do, while, call, read, write, odd, repeat, until
    EQU, LT, LTE, GTE, GT, NE, ADD, SUB, MUL, DIV,
    //=, <, <=, >=, >, <>, +, -, *, /
    SYM, CONST,
    //symbol, const
    CEQU, COMMA, SEMIC, POI, LBR, RBR,
    //:=, ',' , ';', '.', '(', ')'
    COL,
    //:
    EOF
    //end of file -- '.'
}
