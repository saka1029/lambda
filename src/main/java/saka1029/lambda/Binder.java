package saka1029.lambda;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

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

    final Map<K, Value<V>> bind = new HashMap<>();

    public <T> T bind(K key, V value, Supplier<T> bindProc) {
    	Value<V> old = bind.get(key);
		bind.put(key, new Value<>(value));
    	T result = bindProc.get();
    	if (old != null)
    		bind.put(key, old);
    	else
    		bind.remove(key);
    	return result;
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
