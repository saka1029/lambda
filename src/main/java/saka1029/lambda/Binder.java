package saka1029.lambda;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.plaf.basic.BasicFormattedTextFieldUI;

public class Binder<K, V> {

	private static class Value<V> {
		final V value;
		int refCount = 0;
		
		Value(V value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return "%s(%s)".formatted(value, refCount);
		}
	}

    final Deque<Value<V>> stack = new LinkedList<>();
    final Map<K, Value<V>> bind = new HashMap<>();

    public Unbind bind(K key, V value) {
        stack.push(bind.get(key));
        bind.put(key, new Value<>(value));
        return () -> {
        	Value<V> prev = stack.pop();
        	if (prev == null)
        		bind.remove(key);
        	else
        		bind.put(key, prev);
        };
    }

    public int refCount(K key) {
        Value<V> value = bind.get(key);
        if (value == null)
			throw new NullPointerException("undefined key " + key + " in " + this);
        return value.refCount;
    }

    public V get(K key) {
        Value<V> value = bind.get(key);
        if (value == null)
        	return null;
        ++value.refCount;
        return value.value;
    }

    @Override
    public String toString() {
        return bind.toString();
    }
}
