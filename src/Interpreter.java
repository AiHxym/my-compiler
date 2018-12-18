import java.util.List;
import javax.swing.JOptionPane;

public class Interpreter {
    private final int STACK_SIZE = 1000;
    private int[] dataStack = new int[STACK_SIZE];
    private List<EachPcode> pcode;


    public Interpreter() {

    }

    public void setAllPcode(PcodeTable pcodeTable) {
        pcode = pcodeTable.getAllPcode();
    }

    public void interpreter() {
        int pc = 0;  //program counter
        int base = 0;  //the current base address
        int top = 0;  //the top point of stack
        do {
            EachPcode currentPcode = pcode.get(pc++);
            if (currentPcode.getF() == Operator.LIT) {
                dataStack[top++] = currentPcode.getA();
            } else if (currentPcode.getF() == Operator.OPR) {
                switch(currentPcode.getA()) {
                    case 0:
                        //OPR 0 0   过程调用结束后,返回调用点并退栈
                        top = base;
                        pc = dataStack[base + 2];
                        base = dataStack[base];
                        break;
                    case 1:
                        //OPR 0 1     栈顶元素取反
                        dataStack[top - 1] = -dataStack[top - 1];
                        break;
                    case 2:
                        //OPR 0 2   次栈顶与栈顶相加，退两个栈元素，结果值进栈
                        dataStack[top - 2] = dataStack[top - 1] + dataStack[top - 2];
                        top--;
                        break;
                    case 3:
                        //OPR 0 3   次栈顶减去栈顶，退两个栈元素，结果值进栈
                        dataStack[top - 2] = dataStack[top - 2] - dataStack[top - 1];
                        top--;
                        break;
                    case 4:
                        //OPR 0 4     次栈顶乘以栈顶，退两个栈元素，结果值进栈
                        dataStack[top - 2] = dataStack[top - 1] * dataStack[top - 2];
                        top--;
                        break;
                    case 5:
                        //OPR 0 5     次栈顶除以栈顶，退两个栈元素，结果值进栈
                        dataStack[top - 2] = dataStack[top - 2] / dataStack[top - 1];
                        top--;
                        break;
                    case 6:
                        //OPR 0 6   栈顶元素的奇偶判断，结果值在栈顶
                        dataStack[top - 1] = dataStack[top - 1] % 2;
                        break;
                    case 7:
                        break;
                    case 8:
                        //OPR 0 8   次栈顶与栈顶是否相等，退两个栈元素，结果值进栈
                        if (dataStack[top - 2] == dataStack[top - 1]) {
                            dataStack[top - 2] = 1;
                        } else {
                            dataStack[top - 2] = 0;
                        }
                        top--;
                        break;
                    case 9:
                        //OPR 0 9   次栈顶与栈顶是否不等，退两个栈元素，结果值进栈
                        if (dataStack[top - 2] != dataStack[top - 1]) {
                            dataStack[top - 2] = 1;
                        } else {
                            dataStack[top - 2] = 0;
                        }
                        top--;
                        break;
                    case 10:
                        //OPR 0 10  次栈顶是否小于栈顶，退两个栈元素，结果值进栈
                        if (dataStack[top - 2] < dataStack[top - 1]) {
                            dataStack[top - 2] = 1;
                        } else {
                            dataStack[top - 2] = 0;
                        }
                        top--;
                        break;
                    case 11:
                        //OPR 0 11    次栈顶是否大于等于栈顶，退两个栈元素，结果值进栈
                        if (dataStack[top - 2] >= dataStack[top - 1]) {
                            dataStack[top - 2] = 1;
                        } else {
                            dataStack[top - 2] = 0;
                        }
                        top--;
                        break;
                    case 12:
                        //OPR 0 12  次栈顶是否大于栈顶，退两个栈元素，结果值进栈
                        if (dataStack[top - 2] > dataStack[top - 1]) {
                            dataStack[top - 2] = 1;
                        } else {
                            dataStack[top - 2] = 0;
                        }
                        top--;
                        break;
                    case 13:
                        //OPR 0 13  次栈顶是否小于等于栈顶，退两个栈元素，结果值进栈
                        if (dataStack[top - 2] <= dataStack[top - 1]) {
                            dataStack[top - 2] = 1;
                        } else {
                            dataStack[top - 2] = 0;
                        }
                        top--;
                        break;
                    case 14:
                        //OPR 0 14  栈顶值输出至屏幕
                        System.out.print(dataStack[top - 1]);
                        System.out.print(" ");
                        break;
                    case 15:
                        //OPR 0 15  屏幕输出换行
                        System.out.println();
                        break;
                    case 16:
                        //OPR 0 16  从读入一个整数输入置于栈顶
                        int inputValue = Integer.parseInt(JOptionPane.showInputDialog("Please input a number"));
                        dataStack[top] = inputValue;
                        //dataStack[top] = input.get(inputPtr++);
                        top++;
                        break;
                }
            } else if (currentPcode.getF() == Operator.LOD) {
                // LOD：将变量送到运行栈S的栈顶，这时A段为变量所在说明层中的相对位置。
                dataStack[top++] = dataStack[currentPcode.getA() + getBase(base, currentPcode.getL())];
            } else if (currentPcode.getF() == Operator.STO) {
                //STO：将运行栈S的栈顶内容送入某个变量单元中，A段为变量所在说明层中的相对位置。
                dataStack[currentPcode.getA() + getBase(base, currentPcode.getL())] = dataStack[top - 1];
                --top;
            } else if (currentPcode.getF() == Operator.CAL) {
                //CAL：调用过程，这时A段为被调用过程的过程体（过程体之前一条指令）在目标程序区的入口地址。
                //跳转时，将该层基地址，跳转层基地址，pc指针保存在栈中
                //基地址base变为此时栈顶top，pc指向要跳转的地方
                //不修改top，因为前面代码已经将address+3，生成Pcode后会产生INT语句，修改top值
                dataStack[top] = base;
                dataStack[top + 1] = getBase(base, currentPcode.getL());
                dataStack[top + 2] = pc;
                base = top;
                pc = currentPcode.getA();
            } else if (currentPcode.getF() == Operator.INT) {
                //INT：为被调用的过程（包括主过程）在运行栈S中开辟数据区，这时A段为所需数据单元个数（包括三个连接数据）；L段恒为0。
                top = top + currentPcode.getA();
            } else if (currentPcode.getF() == Operator.JMP) {
                //JMP：无条件转移，这时A段为转向地址（目标程序）。
                pc = currentPcode.getA();
            } else if (currentPcode.getF() == Operator.JPC) {
                //JPC：条件转移，当运行栈S的栈顶的布尔值为假（0）时，则转向A段所指目标程序地址；否则顺序执行。
                if (dataStack[top - 1] == 0) {
                    pc = currentPcode.getA();
                }
            }
            //System.out.println(pc + " " + base + " " + top);
        } while (pc != 0);
    }


    private int getBase(int now, int l) {
        int res = now;
        while (l > 0) {
            res = dataStack[res + 1];
            --l;
        }
        return res;
    }
}
