package saka1029.lambda;

public class Application extends Expression {

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
}
