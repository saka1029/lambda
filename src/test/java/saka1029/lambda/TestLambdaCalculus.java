package saka1029.lambda;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import org.junit.Test;

public class TestLambdaCalculus {

    @Test
    public void testParseApplication() {
        assertEquals("A(a b)", LambdaCalculus.parse("a b").toString());
        assertEquals("A(A(a b) c)", LambdaCalculus.parse("a b c").toString());
    }

    @Test
    public void testParseLambda() {
        assertEquals("λ(a b)", LambdaCalculus.parse("λa.b").toString());
        assertEquals("λ(a λ(b c))", LambdaCalculus.parse("λa.λb.c").toString());
        assertEquals("λ(a λ(b c))", LambdaCalculus.parse("λa b.c").toString());
    }

    @Test
    public void testParseLambdaBackslash() {
        assertEquals("λ(a b)", LambdaCalculus.parse("\\a.b").toString());
        assertEquals("λ(a λ(b c))", LambdaCalculus.parse("\\a.\\b.c").toString());
        assertEquals("λ(a λ(b c))", LambdaCalculus.parse("\\a b.c").toString());
    }

    @Test
    public void testParseLambdaScope() {
        Expression e = LambdaCalculus.parse("λx.λx.x");
        Lambda first = (Lambda)e;
        Lambda second = (Lambda)first.body;
        // 最後のxは2番目のラムダ式の束縛変数
        assertSame(second.variable, second.body);
    }
}
