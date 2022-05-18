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
            assertEquals("\\x.x", reduce(a, frees).toString());
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
        assertEquals("\\y.y y", reduce(apply(xx, yyy)).toString());
    }

    /**
     * 0    := λf x.x
     * 1    := λf x.f x
     * 2    := λf x.f (f x)
     * 3    := λf x.f (f (f x))
     * SUCC := λn f x.f (n f x)
     * 
     * SUCC 0
     * = (λn.λf.λx.f (n f x)) (λf.λx.x)
     * = λf.λx.f (n f x) [n:=(λf.λx.x)]
     * = λf.λx.f ((λf.λx.x) f x) [n:=(λf.λx.x)]
     * =         ===============
     * =   ((λf.λx.x) f x) [n:=(λf.λx.x)]
     * =   ((λa.λb.b) f x) [n:=(λf.λx.x)]
     * =    ===========
     * =     (λa.λb.b) f [n:=(λf.λx.x)]
     * =     λb.b [a:=f, n:=(λf.λx.x)]
     * =     λb.b [n:=(λf.λx.x)]
     * =   (λb.b x) [n:=(λf.λx.x)]
     * =   b [b:=x, n:=(λf.λx.x)]
     * =   x [n:=(λf.λx.x)]
     * = λf.λx.f x
     */
    @Test
    public void testChurchNumerals() {
        Binder<FreeVariable, Expression> global = new Binder<>();
        global.bind(free("0"), parse("λf x.x"));
        global.bind(free("1"), parse("λf x.f x"));
        global.bind(free("2"), parse("λf x.f (f x)"));
        global.bind(free("succ"), parse("λn f x. f (n f x)"));
        System.out.println(tree(global.get(free("succ"))));
        System.out.println(normalize(reduce(free("succ"), global)));
        Expression r = reduce(apply(free("succ"), free("0")), global);
        System.out.println(r);
        System.out.println(tree(r));
        System.out.println(normalize(r));
    }
}
