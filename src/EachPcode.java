public class EachPcode {
    private Operator F;
    private int L;
    private int A;

    public EachPcode(Operator f, int l, int a) {
        F = f;
        L = l;
        A = a;
    }

    public Operator getF() {
        return F;
    }

    public void setF(Operator f) {
        F = f;
    }

    public int getL() {
        return L;
    }

    public void setL(int l) {
        L = l;
    }

    public int getA() {
        return A;
    }

    public void setA(int a) {
        A = a;
    }
}
