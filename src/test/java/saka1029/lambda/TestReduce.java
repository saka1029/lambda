package saka1029.lambda;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import static saka1029.lambda.LambdaCalculus.*;

public class TestReduce {

    @Test
    public void testFreeVariable() {
        Binder<FreeVariable, Expression> frees = new Binder<>();
        try (Unbind u = frees.bind(FreeVariable.of("a"), parse("λx.x"))) {
            Lambda l = (Lambda)reduce(parse("a"), frees);
            BoundVariable v = l.variable;
            assertEquals("x", v.name);
            assertEquals(v, l.body);
        }
        assertNull(frees.get(FreeVariable.of("a")));
    }
    
}
