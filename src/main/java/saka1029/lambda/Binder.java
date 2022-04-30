package saka1029.lambda;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Binder<K, V> {
    final Deque<V> stack = new LinkedList<>();
    final Map<K, V> bind = new HashMap<>();

    public Unbind bind(K key, V value) {
        stack.push(bind.get(key));
        bind.put(key, value);
        return () -> bind.put(key, stack.pop());
    }

    public V get(K key) {
        return bind.get(key);
    }

    @Override
    public String toString() {
        return bind.toString();
    }
}
