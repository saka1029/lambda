package saka1029.lambda;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FreeVariable extends Variable {

    private static final Map<String, FreeVariable> all = new HashMap<>();

    private FreeVariable(String name) {
        super(name);
        Objects.requireNonNull(name);
    }

    public static FreeVariable of(String name) {
        return all.computeIfAbsent(name, k -> new FreeVariable(k));
    }
}
