package saka1029.lambda;

public class Application implements Expression {

    public final Expression head, tail;
    
    private Application(Expression head, Expression tail) {
        this.head = head;
        this.tail = tail;
    }

    public static Application of(Expression head, Expression tail) {
        return new Application(head, tail);
    }

    static String paren(Expression e, boolean paren) {
        return paren ? "(" + e + ")" : "" + e;
    }

    @Override
    public String toString() {
        return paren(head, head instanceof Lambda)
            + " " + paren(tail, !(tail instanceof Variable));
    }
}
