package saka1029.lambda;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestBind {

    @Test
    public void testGet() {
        Bind<String, Integer> bind = Bind.of(Bind.of(null, "zero", 0), "one", 1);
        assertEquals((Integer)0, Bind.get(bind, "zero"));
        assertEquals((Integer)1, Bind.get(bind, "one"));
        assertEquals(null, Bind.get(bind, "three"));
        Bind<String, Integer> bind2 = Bind.of(bind, "zero", 1000);
        assertEquals((Integer)1000, Bind.get(bind2, "zero"));
        assertEquals((Integer)0, Bind.get(bind, "zero"));
    }

    @Test
    public void testToString() {
        assertEquals("{}", Bind.toString(null));
        Bind<String, Integer> bind = Bind.of(Bind.of(null, "zero", 0), "one", 1);
        assertEquals("{one=1, zero=0}", Bind.toString(bind));
    }
}
