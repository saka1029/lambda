package saka1029.lambda;

import java.util.HashMap;
import java.util.Map;

public class FreeVariable extends Variable {

    static final Map<String, FreeVariable> all = new HashMap<>();

    FreeVariable(String name) {
        super(name);
    }

    public static FreeVariable of(String name) {
        return all.computeIfAbsent(name, k -> new FreeVariable(k));
    }
}
