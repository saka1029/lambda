package saka1029.lambda;

public abstract class Variable extends Expression {

    final String name;

    Variable(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
