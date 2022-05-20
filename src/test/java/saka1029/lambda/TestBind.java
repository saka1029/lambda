package saka1029.lambda;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class TestBind {

    @Test
    public void testGet() {
        Binder<String, Integer> binder = new Binder<>();
        try (Unbind a = binder.bind("zero", 0)) {
            try (Unbind b = binder.bind("one", 1)) {
                assertEquals((Integer) 0, binder.get("zero"));
                assertEquals((Integer) 1, binder.get("one"));
				assertNull(binder.get("three"));
                try (Unbind c = binder.bind("zero", 1000)) {
                    assertEquals((Integer) 1000, binder.get("zero"));
                }
                assertEquals((Integer) 0, binder.get("zero"));
                assertEquals((Integer) 1, binder.get("one"));
            }
        }
    }
}
