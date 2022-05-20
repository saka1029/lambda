package saka1029.lambda;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import static saka1029.lambda.LambdaCalculus.*;

import java.util.HashMap;
import java.util.Map;

public class TestReduce {

	static FreeVariable free(String name) {
		return FreeVariable.of(name);
	}

    @Test
    public void testFreeVariable() {
        Map<FreeVariable, Expression> frees = new HashMap<>();
        frees.put(free("a"), parse("\\x.x"));
		assertEquals("\\x.x", reduce(parse("a"), frees).toString());
    }

    @Test
    public void testApplication() {
        assertEquals("\\y.y y", reduce(parse("(\\x.x) \\y.y y")).toString());
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
        Map<FreeVariable, Expression> global = new HashMap<>();
        global.put(free("0"), parse("λf x.x"));
        global.put(free("1"), parse("λf x.f x"));
        global.put(free("2"), parse("λf x.f (f x)"));
        global.put(free("succ"), parse("λn f x. f (n f x)"));
        System.out.println(tree(global.get(free("succ"))));
        System.out.println(normalize(reduce(free("succ"), global)));
        Expression r = reduce(parse("succ 0"), global);
        System.out.println(r);
        System.out.println(tree(r));
        System.out.println(normalize(r));
    }
}
