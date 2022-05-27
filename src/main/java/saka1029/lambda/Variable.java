package saka1029.lambda;

public abstract class Variable extends Expression {

    public final String name;

    protected Variable(String name) {
        this.name = name;
    }
}
