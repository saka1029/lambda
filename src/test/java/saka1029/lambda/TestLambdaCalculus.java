package saka1029.lambda;

import static saka1029.lambda.LambdaCalculus.*;
import java.util.stream.Collectors;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import org.junit.Test;

public class TestLambdaCalculus {

    @Test
    public void testParseApplication() {
        assertEquals("a b", parse("a b").toString());
        assertEquals("a b c", parse("a b c").toString());
    }

    @Test
    public void testParseLambda() {
        assertEquals("\\a.b", parse("λa.b").toString());
        assertEquals("\\a b.c", parse("λa.λb.c").toString());
        assertEquals("\\a b.c", parse("λa b.c").toString());
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
        assertEquals("\\a.b", parse("\\a.b").toString());
        assertEquals("\\a b.c", parse("\\a.\\b.c").toString());
        assertEquals("\\a b.c", parse("\\a b.c").toString());
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
        assertEquals("\\𩸽.𩸽", parse("\\𩸽.𩸽").toString());
    }

    @Test
    public void testToString() {
        assertEquals("\\a.b", parse("\\a.b").toString());
        assertEquals("\\a.a", parse("\\a.a").toString());
        assertEquals("\\a b c.a b c", parse("\\a b c.a b c").toString());
        assertEquals("\\a b c.a b c", parse("\\a b c.(a b c)").toString());
        assertEquals("\\a a a.a", parse("\\a.\\a.\\a.a").toString());
        assertEquals("\\a.a (\\b.a) b", parse("\\a.((a \\b.a) b)").toString());
    }

    @Test
    public void testNormalize() {
        assertEquals("\\a.y", normalize(parse("\\x.y")));
        assertEquals("\\a.a", normalize(parse("\\x.x")));
        assertEquals("\\a.\\b.\\c.a b c", normalize(parse("\\x y z.x y z")));
        assertEquals("\\a.\\b.\\c.a b c", normalize(parse("\\x y z.(x y z)")));
        assertEquals("\\a.\\b.\\c.c", normalize(parse("\\x.\\x.\\x.x")));
        assertEquals("(\\a.a) (\\a.a)", normalize(parse("(\\x.x)(\\y.y)")));
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
}
