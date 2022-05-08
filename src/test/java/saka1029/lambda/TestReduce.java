package saka1029.lambda;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import static saka1029.lambda.LambdaCalculus.*;

public class TestReduce {

    static BoundVariable local(String name) {
        return BoundVariable.of(name);
    }

    static Lambda lambda(BoundVariable v, Expression e) {
        return Lambda.of(v, e);
    }

    static FreeVariable free(String name) {
        return FreeVariable.of(name);
    }

    static Application apply(Expression h, Expression t) {
        return Application.of(h, t);
    }

    @Test
    public void testFreeVariable() {
        Binder<FreeVariable, Expression> frees = new Binder<>();
        BoundVariable x = local("x");
        Lambda lambda = lambda(x, x);
        FreeVariable a = free("a");
        try (Unbind u = frees.bind(a, lambda)) {
            assertEquals(lambda, reduce(a, frees));
        }
        assertNull(frees.get(a));
    }

    @Test
    public void testApplication() {
        // λx.x
        BoundVariable x = local("x");
        Lambda xx = lambda(x, x);
        // λy.y y
        BoundVariable y = local("y");
        Lambda yyy = lambda(y, apply(y, y));
        // λy.y y = reduce(λx.x (λy.y y))
        assertEquals(yyy, reduce(apply(xx, yyy)));
    }
    
}
