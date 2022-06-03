package saka1029.lambda;

import static saka1029.lambda.LambdaCalculus.*;
import java.util.stream.Collectors;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestLambdaCalculus {

    @Test
    public void testParseApplication() {
        assertEquals("a b", parse("a b").toString());
        assertEquals("a b c", parse("a b c").toString());
    }

    @Test
    public void testParseLambda() {
        assertEquals("λa.b", parse("λa.b").toString());
        assertEquals("λa b.c", parse("λa.λb.c").toString());
        assertEquals("λa b.c", parse("λa b.c").toString());
    }

    static Lambda asLambda(Expression e) {
    	return (Lambda)e;
    }

    @Test
    public void testParseLambdaRefCount() {
        assertEquals(1, asLambda(parse("λa.a")).refCount);
        assertEquals(0, asLambda(parse("λa.b")).refCount);
        Expression e = parse("λa a a.a");
        assertEquals(0, asLambda(e).refCount);
        assertEquals(0, asLambda(asLambda(e).body).refCount);
        assertEquals(1, asLambda(asLambda(asLambda(e).body).body).refCount);
    }

    @Test
    public void testParseLambdaBackslash() {
        assertEquals("λa.b", parse("\\a.b").toString());
        assertEquals("λa b.c", parse("\\a.\\b.c").toString());
        assertEquals("λa b.c", parse("\\a b.c").toString());
    }

    @Test
    public void testParseLambdaScope() {
        Expression e = parse("\\x.\\x.x");
        Lambda first = (Lambda)e;
        Lambda second = (Lambda)first.body;
        // 最後のxは2番目のラムダ式の束縛変数
        assertSame(second.variable, second.body);
    }

    @Test
    public void testParseSurrogatePair() {
        Expression e = parse("\\𩸽.𩸽");
        Lambda l = (Lambda)e;
        assertEquals(l.variable, l.body);
        assertEquals("λ𩸽.𩸽", parse("\\𩸽.𩸽").toString());
    }

    @Test
    public void testToString() {
        assertEquals("λa.b", parse("\\a.b").toString());
        assertEquals("λa.a", parse("\\a.a").toString());
        assertEquals("λa b c.a b c", parse("\\a b c.a b c").toString());
        assertEquals("λa b c.a b c", parse("\\a b c.(a b c)").toString());
        assertEquals("λa a a.a", parse("\\a.\\a.\\a.a").toString());
        assertEquals("λa.a (λb.a) b", parse("\\a.((a \\b.a) b)").toString());
    }

    @Test
    public void testString() {
        assertEquals("λa.b", string(parse("\\a.b")));
        assertEquals("λa.a", string(parse("\\a.a")));
        assertEquals("λa b c.a b c", string(parse("\\a b c.a b c")));
        assertEquals("λa b c.a b c", string(parse("\\a b c.(a b c)")));
        assertEquals("λa a a.a", string(parse("\\a.\\a.\\a.a")));
        assertEquals("λa.a (λb.a) b", string(parse("\\a.((a \\b.a) b)")));
    }

    static void assertMatches(String expected, String actual) {
//    	System.out.println("expected=" + expected + " actual=" + actual);
    	assertTrue(actual.matches(expected));
    }

    @Test
    public void testStringDetail() {
        assertMatches("λa_\\d+.b", stringDetail(parse("\\a.b")));
        assertMatches("λa(_\\d+).a\\1", stringDetail(parse("\\a.a")));
        assertMatches("λa(_\\d+) b(_\\d+) c(_\\d+).a\\1 b\\2 c\\3", stringDetail(parse("\\a b c.a b c")));
        assertMatches("λa(_\\d+) b(_\\d+) c(_\\d+).a\\1 b\\2 c\\3", stringDetail(parse("\\a b c.(a b c)")));
        assertMatches("λa(_\\d+) a(_\\d+) a(_\\d+).a\\3", stringDetail(parse("\\a.\\a.\\a.a")));
        assertMatches("λa(_\\d+).a\\1 \\(λb(_\\d+).a\\1\\) b", stringDetail(parse("\\a.((a \\b.a) b)")));
    }

    @Test
    public void testNormalize() {
        assertEquals("λa.y", normalize(parse("\\x.y")));
        assertEquals("λa.a", normalize(parse("\\x.x")));
        assertEquals("λa b c.a b c", normalize(parse("\\x y z.x y z")));
        assertEquals("λa b c.a b c", normalize(parse("\\x y z.(x y z)")));
        assertEquals("λa b c.c", normalize(parse("\\x.\\x.\\x.x")));
        assertEquals("(λa.a) (λa.a)", normalize(parse("(\\x.x)(\\y.y)")));
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
            """, nl(tree(parse("\\a.b"))));
        assertEquals(
            """
            lambda a
                a
            """, nl(tree(parse("\\a.a"))));
        assertEquals(
            """
            lambda x
                lambda y
                    apply
                        apply
                            x
                            y
                        x
            """, nl(tree(parse("\\x.\\y.x y x"))));
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
            """, nl(tree(parse("\\a b c.a b c"))));
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
            """, nl(tree(parse("\\a b c.(a b c)"))));
        assertEquals(
            """
            lambda a
                lambda a
                    lambda a
                        a
            """, nl(tree(parse("\\a.\\a.\\a.a"))));
    }
    
    @Test
    public void testSame() {
    	assertTrue(same(parse("λa.a"), parse("λx.x")));
    	assertFalse(same(parse("λa.a"), parse("λx.a")));
    	assertTrue(same(parse("λa.λb.λc.c"), parse("λx.λx.λx.x")));
    }
}
