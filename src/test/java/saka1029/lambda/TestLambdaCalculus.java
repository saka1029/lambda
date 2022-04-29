package saka1029.lambda;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class TestLambdaCalculus {

    @Test
    public void testParseApplication() {
        assertEquals("Application(a, b)", LambdaCalculus.parse("a b").toString());
        assertEquals("Application(Application(a, b), c)", LambdaCalculus.parse("a b c").toString());
    }

    @Test
    public void testParseLambda() {
        assertEquals("Lambda(a, b)", LambdaCalculus.parse("λa.b").toString());
        assertEquals("Lambda(a, Lambda(b, c))", LambdaCalculus.parse("λa.λb.c").toString());
        assertEquals("Lambda(a, Lambda(b, c))", LambdaCalculus.parse("λa b.c").toString());
    }

    @Test
    public void testParseLambdaScope() {
        Expression e = LambdaCalculus.parse("λx.λx.x");
        Lambda first = (Lambda)e;
        Lambda second = (Lambda)first.body;
        assertEquals(second.variable, second.body);
    }
}
