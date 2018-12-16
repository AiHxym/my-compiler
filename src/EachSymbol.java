public class EachSymbol {
    private int type;           //表示常量、变量或过程
    private int value;          //表示常量或变量的值
    private int level;          //嵌套层次
    private int address;        //相对于所在嵌套过程基地址的地址
    private int size;           //表示常量，变量，过程所占的大小(这一项其实默认为0， 并没有用到)
    private String name;        //变量、常量或过程名

    public EachSymbol(int type, int value, int level, int address, int size, String name) {
        this.type = type;
        this.value = value;
        this.level = level;
        this.address = address;
        this.size = size;
        this.name = name;
    }

    public EachSymbol(int type, int level, int address, int size, String name) {
        this.type = type;
        this.level = level;
        this.address = address;
        this.size = size;
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
