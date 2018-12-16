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
                errorHandle(18, "");
            }
        } else {
            errorHandle(17, "");
        }
    }

    private void block() {
        //<分程序>::=[<常量说明部分>][<变量说明部分>][<过程说明部分>]<语句>
        int keep_address = address;

        int start = symbolTable.getPtr();
        int pos = 0;
        address = 3;
        if (start > 0) {
            pos = symbolTable.getLevelProc(level);
        }

        int tmpPcodePtr = pcodeTable.getPcodePtr();
        pcodeTable.add(Operator.JMP, 0, 0);

        if (allToken.get(tokenPtr).getType() == SymType.CON) {
            conDeclare();
        }

        if (allToken.get(tokenPtr).getType() == SymType.VAR) {
            varDeclare();
        }

        if (allToken.get(tokenPtr).getType() == SymType.PROC) {
            proc();
        }

        pcodeTable.getAllPcode().get(tmpPcodePtr).setA(pcodeTable.getPcodePtr());
        pcodeTable.add(Operator.INT, 0, address);

        if (start != 0) {
            //非主程序体要在这里保留
            symbolTable.getAllSymbol().get(pos).setValue(pcodeTable.getPcodePtr() - symbolTable.getAllSymbol().get(pos).getSize() - 1);
        }

        statement();

        pcodeTable.add(Operator.OPR, 0, 0);

        address = keep_address;
    }

    private void conDeclare() {
        //<常量说明部分>::=const <常量定义>{,<常量定义>};
        if (allToken.get(tokenPtr).getType() == SymType.CON) {
            ++tokenPtr;
            conHandle();
            while (allToken.get(tokenPtr).getType() == SymType.COMMA || allToken.get(tokenPtr).getType() == SymType.SYM) {
                if (allToken.get(tokenPtr).getType() == SymType.COMMA) {
                    ++tokenPtr;
                } else {
                    errorHandle(23, "");
                }
                conHandle();
            }
            if (allToken.get(tokenPtr).getType() == SymType.SEMIC) {
                ++tokenPtr;
            } else { //缺少；
                errorHandle(0, "");
            }
        } else { //缺少const
            errorHandle(-1, "");
        }
    }

    private void conHandle() {
        //<常量定义>::=<标识符>=<无符号整数>
        String name;
        int value;
        if (allToken.get(tokenPtr).getType() == SymType.SYM) {
            name = allToken.get(tokenPtr).getValue();
            ++tokenPtr;
            if (allToken.get(tokenPtr).getType() == SymType.EQU || allToken.get(tokenPtr).getType() == SymType.CEQU) {
                if (allToken.get(tokenPtr).getType() == SymType.CEQU) {
                    errorHandle(3, "");
                }
                ++tokenPtr;
                if (allToken.get(tokenPtr).getType() == SymType.CONST) {
                    value = Integer.parseInt(allToken.get(tokenPtr).getValue());
                    if (symbolTable.isNowExists(name, level)) {
                        errorHandle(15, name);
                    }
                    symbolTable.insertConst(name, level, value, address);
                    ++tokenPtr;
                }
            } else { //赋值没用=
                errorHandle(3, "");
            }
        } else { //标识符不合法
            errorHandle(1, "");
        }
    }

    private void varDeclare() {
        //<变量说明部分>::=var<标识符>{,<标识符>};
        String name;
        if (allToken.get(tokenPtr).getType() == SymType.VAR) {
            ++tokenPtr;
            if (allToken.get(tokenPtr).getType() == SymType.SYM) {
                name = allToken.get(tokenPtr).getValue();
                if (symbolTable.isNowExists(name, level)) {
                    errorHandle(15, name);
                }
                symbolTable.insertVar(name, level, address);
                address += addIncrement;
                ++tokenPtr;
                while (allToken.get(tokenPtr).getType() == SymType.COMMA || allToken.get(tokenPtr).getType() == SymType.SYM) {
                    if (allToken.get(tokenPtr).getType() == SymType.COMMA) {
                        ++tokenPtr;
                    } else {
                        errorHandle(23, "");
                    }
                    if (allToken.get(tokenPtr).getType() == SymType.SYM) {
                        name = allToken.get(tokenPtr).getValue();
                        if (symbolTable.isNowExists(name, level)) {
                            errorHandle(15, name);
                        }
                        symbolTable.insertVar(name, level, address);
                        address += addIncrement;
                        tokenPtr++;
                    } else { //非法标识符
                        errorHandle(1, "");
                        return;
                    }
                }
                if (allToken.get(tokenPtr).getType() != SymType.SEMIC) { //缺少；
                    errorHandle(0, "");
                    return;
                } else {
                    ++tokenPtr;
                }
            } else { //非法标识符
                errorHandle(1, "");
                return;
            }
        } else { //缺少var
            errorHandle(-1, "");
            return;
        }
    }

    private void proc() {
        //<过程说明部分>::=<过程首部><分程序>{;<过程说明部分>};
        //<过程首部>::=procedure<标识符>;
        if (allToken.get(tokenPtr).getType() == SymType.PROC) {
            ++tokenPtr;
            int count = 0; //记录参数个数
            int pos; //记录该过程在符号表中的位置
            if (allToken.get(tokenPtr).getType() == SymType.SYM) {
                String name = allToken.get(tokenPtr).getValue();
                if (symbolTable.isNowExists(name, level)) {
                    errorHandle(15, name);
                }
                pos = symbolTable.getPtr();
                symbolTable.insertProc(name, level, address);
                address += addIncrement;
                level++;
                tokenPtr++;

                if (allToken.get(tokenPtr).getType() == SymType.SEMIC) {
                    tokenPtr++;
                } else {
                    errorHandle(0, "");
                }

                block();

                while (allToken.get(tokenPtr).getType() == SymType.SEMIC || allToken.get(tokenPtr).getType() == SymType.PROC) {
                    if (allToken.get(tokenPtr).getType() == SymType.SEMIC) {
                        tokenPtr++;
                    } else {
                        errorHandle(0, "");
                    }
                    level--;
                    proc();
                }
            } else {
                errorHandle(-1, "");
                return;
            }
        }
    }

    private void body() {
        //<复合语句>::=begin<语句>{;<语句>}end
        if (allToken.get(tokenPtr).getType() == SymType.BEGIN) {
            ++tokenPtr;
            statement();
            while (allToken.get(tokenPtr).getType() == SymType.SEMIC || isHeadOfStatement()) {
                if (allToken.get(tokenPtr).getType() == SymType.SEMIC) {
                    ++tokenPtr;
                } else {
                    if (allToken.get(tokenPtr).getType() != SymType.END) {
                        errorHandle(0, "");
                    }
                }
                /*if (allToken.get(tokenPtr).getType() == SymType.END) {
                    errorHandle(21, "");
                    break;
                }*/
                statement();
            }
            if (allToken.get(tokenPtr).getType() == SymType.END) {
                ++tokenPtr;
            } else { //缺少end
                errorHandle(7, "");
                return;
            }
        } else { //缺少begin
            errorHandle(6, "");
            return;
        }
    }

    private void statement() {
        //<语句>::=<赋值语句> | <条件语句> | <当循环语句> | <过程调用语句> | <复合语句> | <读语句> | <写语句> | <空>
        if (allToken.get(tokenPtr).getType() == SymType.IF) {
            //<条件语句>::=if<条件>then<语句>
            ++tokenPtr;
            condition();
            if (allToken.get(tokenPtr).getType() == SymType.THEN) {
                int pos = pcodeTable.getPcodePtr();
                pcodeTable.add(Operator.JPC, 0, 0);
                ++tokenPtr;
                statement();
                pcodeTable.getAllPcode().get(pos).setA(pcodeTable.getPcodePtr());

            } else {
                errorHandle(8, "");
                return;
            }
        } else if (allToken.get(tokenPtr).getType() == SymType.WHILE) {
            //<当循环语句>::=while<条件>do<语句>
            int pos1 = pcodeTable.getPcodePtr();
            ++tokenPtr;
            condition();
            if (allToken.get(tokenPtr).getType() == SymType.DO) {
                int pos2 = pcodeTable.getPcodePtr();
                pcodeTable.add(Operator.JPC, 0, 0);
                ++tokenPtr;
                statement();
                pcodeTable.add(Operator.JMP, 0, pos1);
                pcodeTable.getAllPcode().get(pos2).setA(pcodeTable.getPcodePtr());
            } else {
                errorHandle(9, "");
                return;
            }
        } else if (allToken.get(tokenPtr).getType() == SymType.CAL) {
            //<过程调用语句>::=call<标识符>
            tokenPtr++;
            int count = 0; //参数数目
            EachSymbol tmp;
            if (allToken.get(tokenPtr).getType() == SymType.SYM) {
                String name = allToken.get(tokenPtr).getValue();
                if (symbolTable.isPreExists(name, level)) {
                    tmp = symbolTable.getSymbol(name);
                    if (tmp.getType() == symbolTable.getProc()) {
                        pcodeTable.add(Operator.CAL, level - tmp.getLevel(), tmp.getValue());
                    } else {
                        errorHandle(11, "");
                        return;
                    }
                } else { //不存在该过程
                    errorHandle(10, "");
                    return;
                }
                ++tokenPtr;
            } else {
                errorHandle(1, "");
                return;
            }
        } else if (allToken.get(tokenPtr).getType() == SymType.REA) {
            //<读语句>::=read'('<标识符>{,<标识符>}')'
            tokenPtr++;
            if (allToken.get(tokenPtr).getType() == SymType.LBR) {
                tokenPtr++;
                if (allToken.get(tokenPtr).getType() == SymType.SYM) {
                    String name = allToken.get(tokenPtr).getValue();
                    if (!symbolTable.isPreExists(name, level)) {
                        errorHandle(10, "");
                        return;
                    } else {
                        EachSymbol tmp = symbolTable.getSymbol(name);
                        if (tmp.getType() == symbolTable.getVar()) {
                            pcodeTable.add(Operator.OPR, 0, 16);
                            pcodeTable.add(Operator.STO, level - tmp.getLevel(), tmp.getAddress());
                        } else {
                            errorHandle(12, "");
                            return;
                        }
                    }
                }
                tokenPtr++;
                while (allToken.get(tokenPtr).getType() == SymType.COMMA) {
                    tokenPtr++;
                    if (allToken.get(tokenPtr).getType() == SymType.SYM) {
                        String name = allToken.get(tokenPtr).getValue();
                        if (!symbolTable.isPreExists(name, level)) {
                            errorHandle(10, "");
                            return;
                        } else {
                            EachSymbol tmp = symbolTable.getSymbol(name);
                            if (tmp.getType() == symbolTable.getVar()) {
                                pcodeTable.add(Operator.OPR, 0, 16);
                                pcodeTable.add(Operator.STO, level - tmp.getLevel(), tmp.getAddress());
                            } else {
                                errorHandle(12, "");
                                return;
                            }
                        }
                        tokenPtr++;
                    } else {
                        errorHandle(1, "");
                        return;
                    }
                }
                if (allToken.get(tokenPtr).getType() == SymType.RBR) {
                    tokenPtr++;
                } else {
                    errorHandle(5, "");
                    return;
                }
            } else {
                errorHandle(4, "");
                return;
            }
        } else if (allToken.get(tokenPtr).getType() == SymType.WRI) {
            //<写语句>::=write '('<表达式>{,<表达式>}')'
            tokenPtr++;
            if (allToken.get(tokenPtr).getType() == SymType.LBR) {
                tokenPtr++;
                expression();
                pcodeTable.add(Operator.OPR, 0, 14);
                while (allToken.get(tokenPtr).getType() == SymType.COMMA) {
                    tokenPtr++;
                    expression();
                    pcodeTable.add(Operator.OPR, 0, 14);
                }
                pcodeTable.add(Operator.OPR, 0, 15);
                if (allToken.get(tokenPtr).getType() == SymType.RBR) {
                    tokenPtr++;
                } else { //缺少)
                    errorHandle(5, "");
                    return;
                }
            } else { //缺少（
                errorHandle(4, "");
            }
        } else if (allToken.get(tokenPtr).getType() == SymType.BEGIN) {
            //<复合语句>::=begin<语句>{;<语句>}end
            body();
        } else if (allToken.get(tokenPtr).getType() == SymType.SYM) {
            //<赋值语句>::=<标识符>:=<表达式>
            String name = allToken.get(tokenPtr).getValue();
            tokenPtr++;
            if (allToken.get(tokenPtr).getType() == SymType.CEQU || allToken.get(tokenPtr).getType() == SymType.EQU || allToken.get(tokenPtr).getType() == SymType.COL) {
                if (allToken.get(tokenPtr).getType() == SymType.EQU || allToken.get(tokenPtr).getType() == SymType.COL) {
                    errorHandle(3, "");
                }
                tokenPtr++;
                expression();
                if (!symbolTable.isPreExists(name, level)) {
                    errorHandle(14, name);
                    return;
                } else {
                    EachSymbol tmp = symbolTable.getSymbol(name);
                    if (tmp.getType() == symbolTable.getVar()) {
                        pcodeTable.add(Operator.STO, level - tmp.getLevel(), tmp.getAddress());
                    } else {
                        errorHandle(13, name);
                        return;
                    }
                }
            } else {
                errorHandle(3, "");
                return;
            }
        } else if (allToken.get(tokenPtr).getType() == SymType.REP) {
            //<重复语句> ::= repeat<语句>{;<语句>}until<条件>
            tokenPtr++;
            int pos = pcodeTable.getPcodePtr();
            statement();
            while (allToken.get(tokenPtr).getType() == SymType.SEMIC || isHeadOfStatement()) {
                if (isHeadOfStatement()) {
                    errorHandle(1, "");
                } else {
                    tokenPtr++;
                }
                if (allToken.get(tokenPtr).getType() == SymType.UNT) {
                    errorHandle(22, "");
                    break;
                }
                tokenPtr++;
                statement();
            }
            if (allToken.get(tokenPtr).getType() == SymType.UNT) {
                tokenPtr++;
                condition();
                pcodeTable.add(Operator.JPC, 0, pos);
            } else {
                errorHandle(19, "");
                return;
            }
        } else {
//            errorHandle(1, "");
            return;
        }
    }

    private void condition() {
        //<条件>::=<表达式><关系运算符><表达式> | odd<表达式>
        if (allToken.get(tokenPtr).getType() == SymType.ODD) {
            pcodeTable.add(Operator.OPR, 0, 6);
            tokenPtr++;
            expression();
        } else {
            expression();
            SymType tmp = allToken.get(tokenPtr).getType();
            tokenPtr++;
            expression();
            if (tmp == SymType.EQU) { //两个结果是否相等
                pcodeTable.add(Operator.OPR, 0, 8);
            } else if (tmp == SymType.NE) { //两个结果是否不等
                pcodeTable.add(Operator.OPR, 0, 9);
            } else if (tmp == SymType.LT) { //小于
                pcodeTable.add(Operator.OPR, 0, 10);
            } else if (tmp == SymType.GTE) { //大于等于
                pcodeTable.add(Operator.OPR, 0, 11);
            } else if (tmp == SymType.GT) { //大于
                pcodeTable.add(Operator.OPR, 0, 12);
            } else if (tmp == SymType.LTE) { //小于等于
                pcodeTable.add(Operator.OPR, 0, 13);
            } else { //不合法的比较运算符
                errorHandle(2, "");
            }
        }
    }

    private void expression() {
        //<表达式>::=[+|-]<项>{<加法运算符><项>}
        //<加法运算符>::=+|-
        SymType tmp = allToken.get(tokenPtr).getType();
        if (tmp == SymType.ADD || tmp == SymType.SUB) {
            tokenPtr++;
        }
        term();
        if (tmp == SymType.SUB) {
            pcodeTable.add(Operator.OPR, 0, 1);
        }
        while (allToken.get(tokenPtr).getType() == SymType.ADD || allToken.get(tokenPtr).getType() == SymType.SUB) {
            tmp = allToken.get(tokenPtr).getType();
            tokenPtr++;
            term();
            if (tmp == SymType.ADD) {
                pcodeTable.add(Operator.OPR, 0, 2);
            } else if (tmp == SymType.SUB) {
                pcodeTable.add(Operator.OPR, 0, 3);
            }
        }
    }

    private void term() {
        //<项>::=<因子>{<乘法运算符><因子>}
        //<乘法运算符>::=*|/
        factor();
        while (allToken.get(tokenPtr).getType() == SymType.MUL || allToken.get(tokenPtr).getType() == SymType.DIV) {
            SymType tmp = allToken.get(tokenPtr).getType();
            tokenPtr++;
            factor();
            if (tmp == SymType.MUL) {
                pcodeTable.add(Operator.OPR, 0, 4);
            } else if (tmp == SymType.DIV) {
                pcodeTable.add(Operator.OPR, 0, 5);
            }
        }
    }

    private void factor() {
        //<因子>::=<标识符> | <无符号整数> | '('<表达式>')'
        if (allToken.get(tokenPtr).getType() == SymType.CONST) {
            pcodeTable.add(Operator.LIT, 0, Integer.parseInt(allToken.get(tokenPtr).getValue()));
            ++tokenPtr;
        } else if (allToken.get(tokenPtr).getType() == SymType.LBR) {
            ++tokenPtr;
            expression();
            if (allToken.get(tokenPtr).getType() == SymType.RBR) {
                ++tokenPtr;
            } else { //缺少右括号
                errorHandle(5, "");
            }
        } else if (allToken.get(tokenPtr).getType() == SymType.SYM) {
            String name = allToken.get(tokenPtr).getValue();
            if (!symbolTable.isPreExists(name, level)) {
                errorHandle(10, "");
                return;
            } else {
                EachSymbol tmp = symbolTable.getSymbol(name);
                if (tmp.getType() == symbolTable.getVar()) {
                    pcodeTable.add(Operator.LOD, level - tmp.getLevel(), tmp.getAddress());
                } else if (tmp.getType() == symbolTable.getCon()) {
                    pcodeTable.add(Operator.LIT, 0, tmp.getValue());
                } else {
                    errorHandle(12, "");
                    return;
                }
            }
            ++tokenPtr;
        } else {
            errorHandle(1, "");
            return;
        }
    }

    private boolean isHeadOfStatement() {
        return (allToken.get(tokenPtr).getType() == SymType.IF ||
                allToken.get(tokenPtr).getType() == SymType.WHILE ||
                allToken.get(tokenPtr).getType() == SymType.CAL ||
                allToken.get(tokenPtr).getType() == SymType.REA ||
                allToken.get(tokenPtr).getType() == SymType.WRI ||
                allToken.get(tokenPtr).getType() == SymType.BEGIN ||
                allToken.get(tokenPtr).getType() == SymType.SYM ||
                allToken.get(tokenPtr).getType() == SymType.REP);
    }

    private void errorHandle(int k, String name) {
        isErrorHappen = true;
        String error = "";
        switch (k) {
            case -1: //常量定义不是const开头，变量定义不是var开头
                error = "Error happened in line " + allToken.get(tokenPtr).getLine() + ":" + "wrong token";
                break;
            case 0: //缺少分号
                if (allToken.get(tokenPtr).getType() == SymType.SYM) {
                    error = "Error happened in line " + allToken.get(tokenPtr).getLine() + ":" + "Missing ; before " + allToken.get(tokenPtr).getValue();
                } else {
                    error = "Error happened in line " + allToken.get(tokenPtr).getLine() + ":" + "Missing ; before " + allToken.get(tokenPtr).getType();
                }
                break;
            case 1: //标识符不合法
                error = "Error happened in line " + allToken.get(tokenPtr).getLine() + ":" + "Identifier illegal";
                break;
            case 2: //不合法的比较符
                error = "Error happened in line " + allToken.get(tokenPtr).getLine() + ":" + "illegal compare symbol";
                break;
            case 3: //常量赋值没用=
                error = "Error happened in line " + allToken.get(tokenPtr).getLine() + ":" + "Const assign must be =";
                break;
            case 4: //缺少（
                error = "Error happened in line " + allToken.get(tokenPtr).getLine() + ":" + "Missing (";
                break;
            case 5: //缺少）
                error = "Error happened in line " + allToken.get(tokenPtr).getLine() + ":" + "Missind )";
                break;
            case 6: //缺少begin
                error = "Error happened in line " + allToken.get(tokenPtr).getLine() + ":" + "Missing begin";
                break;
            case 7: //缺少end
                error = "Error happened in line " + allToken.get(tokenPtr).getLine() + ":" + "Missing end";
                break;
            case 8: //缺少then
                error = "Error happened in line " + allToken.get(tokenPtr).getLine() + ":" + "Missing then";
                break;
            case 9: //缺少do
                error = "Error happened in line " + allToken.get(tokenPtr).getLine() + ":" + "Missing do";
                break;
            case 10: //call, write, read语句中，不存在标识符
                error = "Error happened in line " + allToken.get(tokenPtr).getLine() + ":" + "Not exist" + allToken.get(tokenPtr).getValue();
                break;
            case 11: //该标识符不是proc类型
                error = "Error happened in line " + allToken.get(tokenPtr).getLine() + ":" + allToken.get(tokenPtr).getValue() + "is not a procedure";
                break;
            case 12: //read, write语句中，该标识符不是var类型
                error = "Error happened in line " + allToken.get(tokenPtr).getLine() + ":" + allToken.get(tokenPtr).getValue() + "is not a variable";
                break;
            case 13: //赋值语句中，该标识符不是var类型
                error = "Error happened in line " + allToken.get(tokenPtr).getLine() + ":" + name + "is not a varible";
                break;
            case 14: //赋值语句中，该标识符不存在
                error = "Error happened in line " + allToken.get(tokenPtr).getLine() + ":" + "not exist " + name;
                break;
            case 15: //该标识符已存在
                error = "Error happened in line " + allToken.get(tokenPtr).getLine() + ":" + "Already exist " + name;
                break;
            case 16: //调用函数参数错误
                error = "Error happened in line " + allToken.get(tokenPtr).getLine() + ":" + "Number of parameters of procedure " + name + "is incorrect";
                break;
            case 17: //缺少.
                error = "Error happened in line " + allToken.get(tokenPtr).getLine() + ":" + "Missing .";
                break;
            case 18: //多余代码
                error = "Error happened in line " + allToken.get(tokenPtr).getLine() + ":" + "too much code after .";
                break;
            case 19: //缺少until
                error = "Error happened in line " + allToken.get(tokenPtr).getLine() + ":" + "Missing until";
                break;
            case 20: //赋值符应为：=
                error = "Error happened in line " + allToken.get(tokenPtr).getLine() + ":" + "Assign must be :=";
                break;
            case 21: //end前多了；
                error = "Error happened in line " + allToken.get(tokenPtr).getLine() + ":" + "; is no need before end";
                break;
            case 22: //until前多了；
                error = "Error happened in line " + allToken.get(tokenPtr).getLine() + ":" + "; is no need before ubtil";
                break;
            case 23: //缺少,
                error = "Error happened in line " + allToken.get(tokenPtr).getLine() + ":" + "Missing ,";
                break;
        }
        errorMsg.add(error);
    }

    public List<String> getErrorMsg() {
        return errorMsg;
    }


    public List<Token> getAllToken() {
        return allToken;
    }


    public List<EachSymbol> getAllSymbol() {
        return symbolTable.getAllSymbol();
    }

    public List<EachPcode> getAllPcode() { return pcodeTable.getAllPcode(); }

    public void interpreter() {
        Interpreter one = new Interpreter();
        one.setAllPcode(pcodeTable);
        one.interpreter();
    }

}





























