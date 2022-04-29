package saka1029.lambda;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Test;

public class TestVariable {

    @Test
    public void testBoundVariable() {
        assertEquals("a", BoundVariable.of("a").name);
        assertEquals("a", BoundVariable.of("a").toString());
        assertNotEquals(BoundVariable.of("a"), BoundVariable.of("a"));
    }

    @Test
    public void testFreeVariable() {
        assertEquals("a", FreeVariable.of("a").name);
        assertEquals("a", FreeVariable.of("a").toString());
        assertEquals(FreeVariable.of("a"), FreeVariable.of("a"));
    }
}
