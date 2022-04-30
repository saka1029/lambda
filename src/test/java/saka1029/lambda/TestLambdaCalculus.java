package saka1029.lambda;

import static saka1029.lambda.LambdaCalculus.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import org.junit.Test;

public class TestLambdaCalculus {

    @Test
    public void testParseApplication() {
        assertEquals("A(a b)", parse("a b").toString());
        assertEquals("A(A(a b) c)", parse("a b c").toString());
    }

    @Test
    public void testParseLambda() {
        assertEquals("λ(a b)", parse("λa.b").toString());
        assertEquals("λ(a λ(b c))", parse("λa.λb.c").toString());
        assertEquals("λ(a λ(b c))", parse("λa b.c").toString());
    }

    @Test
    public void testParseLambdaBackslash() {
        assertEquals("λ(a b)", parse("\\a.b").toString());
        assertEquals("λ(a λ(b c))", parse("\\a.\\b.c").toString());
        assertEquals("λ(a λ(b c))", parse("\\a b.c").toString());
    }

    @Test
    public void testParseLambdaScope() {
        Expression e = parse("λx.λx.x");
        Lambda first = (Lambda)e;
        Lambda second = (Lambda)first.body;
        // 最後のxは2番目のラムダ式の束縛変数
        assertSame(second.variable, second.body);
    }

    @Test
    public void testParseSurrogatePair() {
        Expression e = parse("λ𩸽.𩸽");
        Lambda l = (Lambda)e;
        assertEquals(l.variable, l.body);
        assertEquals("λ(𩸽 𩸽)", parse("λ𩸽.𩸽").toString());
    }

    @Test
    public void testToNormalizedString() {
        assertEquals("λ%0.b", toNormalizedString(parse("λa.b")));
        assertEquals("λ%0.%0", toNormalizedString(parse("λa.a")));
        assertEquals("λ%0.λ%1.λ%2.%0 %1 %2", toNormalizedString(parse("λa b c.(a b c)")));
        assertEquals("λ%0.λ%1.λ%2.%2", toNormalizedString(parse("λa.λa.λa.a")));
    }
}
