package saka1029.lambda;

public class BoundVariable extends Variable {

    private BoundVariable(String name) {
        super(name);
    }

    public static BoundVariable of(String name) {
        return new BoundVariable(name);
    }
}
