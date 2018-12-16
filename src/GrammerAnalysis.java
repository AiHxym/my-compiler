import java.io.File;
import java.util.EventListener;
import java.util.List;
import java.util.ArrayList;

public class GrammerAnalysis {
    private LexAnalysis lexer;  //Lexer
    private List<Token> allToken;  //Keep all tokens
    private SymbolTable symbolTable;  //Keep symbols
    private PcodeTable pcodeTable;  //Keep Pcode
    private List<String> errorMsg;  //Keep error message
    private boolean isErrorHappen = false;

    private int tokenPtr = 0;

    private int level = 0;
    private int address = 0;
    private int addIncrement = 1;

    GrammerAnalysis(File file) {
        lexer = new LexAnalysis(file);
        allToken = lexer.getAllToken();
        pcodeTable = new PcodeTable();
        symbolTable = new SymbolTable();
        errorMsg = new ArrayList<String>();
    }

    public boolean compile() {
        program();
        return (!isErrorHappen);
    }

    private void program() {
        block();
        if (allToken.get(tokenPtr).getType() == SymType.POI) {
            ++tokenPtr;
            if (allToken.get(tokenPtr).getType() != SymType.EOF) {

            } else {

            }

        }
    }

    private void block() {
        int keep_address = address;

    }

}
