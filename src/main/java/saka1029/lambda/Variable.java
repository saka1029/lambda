package saka1029.lambda;

public abstract class Variable implements Expression {

    public final String name;

    protected Variable(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
