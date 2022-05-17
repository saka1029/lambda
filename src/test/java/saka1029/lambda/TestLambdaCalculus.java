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
        assertEquals("\\a.b", parse("\\a.b").toString());
        assertEquals("\\a b.c", parse("\\a.\\b.c").toString());
        assertEquals("\\a b.c", parse("\\a b.c").toString());
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
    public void testString() {
        assertEquals("\\a.b", string(parse("\\a.b")));
        assertEquals("\\a.a", string(parse("\\a.a")));
        assertEquals("\\a b c.a b c", string(parse("\\a b c.a b c")));
        assertEquals("\\a b c.a b c", string(parse("\\a b c.(a b c)")));
        assertEquals("\\a a a.a", string(parse("\\a.\\a.\\a.a")));
        assertEquals("\\a.a (\\b.a) b", string(parse("\\a.((a \\b.a) b)")));
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
