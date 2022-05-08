package saka1029.lambda;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import static saka1029.lambda.LambdaCalculus.*;

public class TestReduce {

    @Test
    public void testFreeVariable() {
        Binder<FreeVariable, Expression> frees = new Binder<>();
        Lambda lambda = (Lambda)parse("λx.x");
        try (Unbind u = frees.bind(FreeVariable.of("a"), lambda)) {
            assertEquals(lambda, reduce(parse("a"), frees));
        }
        assertNull(frees.get(FreeVariable.of("a")));
    }
    
}
