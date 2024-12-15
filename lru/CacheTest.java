package lru;


import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

// Cache interface adhering to the Dependency Inversion Principle
interface Cache<K, V> {
    V get(K key);
    void put(K key, V value);
}

// Eviction policy interface for Strategy Pattern
interface EvictionPolicy<K> {
    void keyAccessed(K key);
    K evictKey();
}

// Implementation of LRU eviction policy
class LRUEvictionPolicy<K> implements EvictionPolicy<K> {
    private final Deque<K> deque;

    public LRUEvictionPolicy() {
        this.deque = new LinkedList<>();
    }

    @Override
    public void keyAccessed(K key) {
        deque.remove(key);
        deque.addFirst(key);
    }

    @Override
    public K evictKey() {
        return deque.pollLast();
    }
}

// Thread-safe Cache implementation with customizable eviction policy
class ConcurrentCache<K, V> implements Cache<K, V> {
    private final EvictionPolicy<K> evictionPolicy;
    private final Map<K, V> map;
    private final int capacity;
    private final ReadWriteLock lock;

    public ConcurrentCache(int capacity, EvictionPolicy<K> evictionPolicy) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than zero");
        }
        this.capacity = capacity;
        this.evictionPolicy = evictionPolicy;
        this.map = new ConcurrentHashMap<>();
        this.lock = new ReentrantReadWriteLock();
    }

    @Override
    public V get(K key) {
        lock.readLock().lock();
        try {
            V value = map.get(key);
            if (value != null) {
                evictionPolicy.keyAccessed(key);
            }
            return value;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void put(K key, V value) {
        lock.writeLock().lock();
        try {
            if (map.containsKey(key)) {
                map.put(key, value);
                evictionPolicy.keyAccessed(key);
            } else {
                if (map.size() >= capacity) {
                    K evictedKey = evictionPolicy.evictKey();
                    if (evictedKey != null) {
                        map.remove(evictedKey);
                    }
                }
                map.put(key, value);
                evictionPolicy.keyAccessed(key);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
}

// Test the Cache implementation
public class CacheTest {
    public static void main(String[] args) {
        // Create a cache with capacity 3 and LRU eviction policy
        Cache<Integer, String> cache = new ConcurrentCache<>(3, new LRUEvictionPolicy<>());

        cache.put(1, "one");
        cache.put(2, "two");
        cache.put(3, "three");
        System.out.println(cache.get(1)); // Access 1, output: one

        cache.put(4, "four"); // Evicts 2 (least recently used)
        System.out.println(cache.get(2)); // Output: null
        System.out.println(cache.get(3)); // Output: three
        System.out.println(cache.get(4)); // Output: four

        cache.put(5, "five"); // Evicts 1
        System.out.println(cache.get(1)); // Output: null
        System.out.println(cache.get(5)); // Output: five
    }
}
