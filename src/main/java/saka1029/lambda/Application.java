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

    @Override
    public String toString() {
        return "Application(%s, %s)".formatted(head, tail);
    }
}
