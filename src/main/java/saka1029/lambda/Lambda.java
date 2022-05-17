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
        StringBuilder sb = new StringBuilder();
        sb.append("\\").append(variable);
        Expression b = body;
        while (b instanceof Lambda l) {
            sb.append(" ").append(l.variable);
            b = l.body;
        }
        sb.append(".").append(b);
        return sb.toString();
    }
}
