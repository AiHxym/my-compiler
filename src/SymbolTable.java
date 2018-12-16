import java.util.ArrayList;
import java.util.List;

public class SymbolTable {
    List<EachSymbol> allSymbol;

    private static final int con = 1;  //const
    private static final int var = 2;  //variable
    private static final int proc = 3;  //procedure

    private int ptr = 0;

    public SymbolTable() {
        allSymbol = new ArrayList<EachSymbol>();
    }

    //Insert a const to symbol table
    public void insertConst(String name, int level, int value, int address) {
        allSymbol.add(new EachSymbol(con, value, level, address, 0, name));
        ptr++;
    }

    //Insert a variable to symbol table
    public void insertVar(String name, int level, int address) {
        allSymbol.add(new EachSymbol(var, level, address, 0, name));
        ptr++;
    }

    public void insertProc(String name, int level, int address) {
        allSymbol.add(new EachSymbol(proc, level, address, 0, name));
        ptr++;
    }

    //在符号表当前层查找变量是否存在
    //存疑？
    //这样暴力查找好像存在一些问题
    public boolean isNowExists(String name, int level) {
        for (int i = allSymbol.size() - 1; i >= 0; --i) {
            if (allSymbol.get(i).getLevel() == level) {
                if (allSymbol.get(i).getName().equals(name)) {
                    return true;
                }
            } else {
                break;
            }
        }
        return false;
    }

    //在符号表之前层查找符号是否存在
    //存疑？
    //暴力查找存在问题
    public boolean isPreExists(String name, int level) {
        for (int i = 0; i < allSymbol.size(); i++) {
            if (allSymbol.get(i).getName().equals(name) && allSymbol.get(i).getLevel() <= level) {
                return true;
            }
        }
        return false;
    }

    //按名称查找变量
    public EachSymbol getSymbol(String name) {
        for (int i = allSymbol.size() - 1; i >= 0; i--) {
            if (allSymbol.get(i).getName().equals(name)) {
                return allSymbol.get(i);
            }
        }
        return null;
    }

    //查找当前层所在的过程
    public int getLevelProc(int level) {
        for (int i = allSymbol.size() - 1; i >= 0; i--) {
            if (allSymbol.get(i).getType() == proc) {
                return i;
            }
        }
        return -1;
    }

    public List<EachSymbol> getAllSymbol() {
        return allSymbol;
    }

    public static int getCon() {
        return con;
    }

    public static int getVar() {
        return var;
    }

    public static int getProc() {
        return proc;
    }

    public int getPtr() {
        return ptr;
    }
}
