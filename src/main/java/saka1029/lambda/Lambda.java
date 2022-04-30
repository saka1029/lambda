package saka1029.lambda;

public class Lambda implements Expression {

    public final BoundVariable variable;
    public final Expression body;

    private Lambda(BoundVariable variable, Expression body) {
        this.variable = variable;
        this.body = body;
    }

    public static Lambda of(BoundVariable variable, Expression body) {
        return new Lambda(variable, body);
    }

    @Override
    public String toString() {
        return "λ(%s %s)".formatted(variable, body);
    }
}
