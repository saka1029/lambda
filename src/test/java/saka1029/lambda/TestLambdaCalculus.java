package saka1029.lambda;

import static saka1029.lambda.LambdaCalculus.*;
import java.util.stream.Collectors;
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
    public void testString() {
        assertEquals("λa.b", string(parse("λa.b")));
        assertEquals("λa.a", string(parse("λa.a")));
        assertEquals("λa.λb.λc.a b c", string(parse("λa b c.a b c")));
        assertEquals("λa.λb.λc.a b c", string(parse("λa b c.(a b c)")));
        assertEquals("λa.λa.λa.a", string(parse("λa.λa.λa.a")));
        assertEquals("λa.a (λb.a) b", string(parse("λa.((a λb.a) b)")));
    }

    @Test
    public void testNormalize() {
        assertEquals("λⓐ.b", normalize(parse("λa.b")));
        assertEquals("λⓐ.ⓐ", normalize(parse("λa.a")));
        assertEquals("λⓐ.λⓑ.λⓒ.ⓐ ⓑ ⓒ", normalize(parse("λa b c.a b c")));
        assertEquals("λⓐ.λⓑ.λⓒ.ⓐ ⓑ ⓒ", normalize(parse("λa b c.(a b c)")));
        assertEquals("λⓐ.λⓑ.λⓒ.ⓒ", normalize(parse("λa.λa.λa.a")));
        assertEquals("(λⓐ.ⓐ) (λⓐ.ⓐ)", normalize(parse("(λa.a)(λb.b)")));
    }

    static String nl(String s) {
        return s.lines()
            .map(line -> line + "\n")
            .collect(Collectors.joining());
    }

    @Test
    public void testTree() {
        assertEquals(
            """
            lambda a
                b
            """, nl(tree(parse("λa.b"))));
        assertEquals(
            """
            lambda a
                a
            """, nl(tree(parse("λa.a"))));
        assertEquals(
            """
            lambda x
                lambda y
                    apply
                        apply
                            x
                            y
                        x
            """, nl(tree(parse("λx.λy.x y x"))));
        assertEquals(
            """
            lambda a
                lambda b
                    lambda c
                        apply
                            apply
                                a
                                b
                            c
            """, nl(tree(parse("λa b c.a b c"))));
        assertEquals(
            """
            lambda a
                lambda b
                    lambda c
                        apply
                            apply
                                a
                                b
                            c
            """, nl(tree(parse("λa b c.(a b c)"))));
        assertEquals(
            """
            lambda a
                lambda a
                    lambda a
                        a
            """, nl(tree(parse("λa.λa.λa.a"))));
    }
}
