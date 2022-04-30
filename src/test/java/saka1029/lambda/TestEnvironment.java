package saka1029.lambda;

import static org.junit.Assert.assertEquals;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.junit.Test;

public class TestEnvironment {

    interface Unbind extends AutoCloseable {
        @Override
        void close();
    }

    static class Environment<K, V> {
        Deque<V> stack = new LinkedList<>();
        Map<K, V> bind = new HashMap<>();

        public V get(K key) {
            return bind.get(key);
        }

        public Unbind bind(K key, V value) {
            stack.push(bind.get(key));
            bind.put(key, value);
            return () -> bind.put(key, stack.pop());
        }

        @Override
        public String toString() {
            return bind.toString() + " " + stack;
        }
    }

    @Test
    public void testEnvironment() {
        Environment<String, Integer> env = new Environment<>();
        System.out.println(env);
        assertEquals(null, env.get("x"));
        try (Unbind a = env.bind("x", 0)) {
            System.out.println(env);
            assertEquals((Integer) 0, env.get("x"));
            try (Unbind b = env.bind("x", 1)) {
                System.out.println(env);
                assertEquals((Integer) 1, env.get("x"));
                try (Unbind c = env.bind("x", 2)) {
                    System.out.println(env);
                    assertEquals((Integer) 2, env.get("x"));
                }
                System.out.println(env);
                assertEquals((Integer) 1, env.get("x"));
            }
            System.out.println(env);
            assertEquals((Integer) 0, env.get("x"));
        }
        System.out.println(env);
        assertEquals(null, env.get("x"));
    }
}
