package saka1029.lambda;

import java.util.Objects;

public class BoundVariable extends Variable {
	static int nextId = 0;
	public final int id;

    private BoundVariable(String name) {
        super(name);
        Objects.requireNonNull(name);
        this.id = nextId++;
    }

    public static BoundVariable of(String name) {
        return new BoundVariable(name);
    }
}
