package lru_self;

import java.util.*;

public class LruCache<K,V> {

    Storage<K,V> storage;
    EvictionPolicy<K,V> evictionPolicy;
    int cap;
    

    LruCache(Storage<K,V> storage, EvictionPolicy<K,V> evictionPolicy, int cap){
        this.storage = storage;
        this.evictionPolicy = evictionPolicy;
        this.cap = cap;
    }

    public synchronized void put(K key, V value){
        if(storage.size() == cap){
            if(storage.containsKey(key)){
                storage.put(key, value);
                evictionPolicy.accessed(key);
            }
            else{
                K evictKey = evictionPolicy.evictKey();
                if(evictKey != null){
                    storage.remove(evictKey);
                    evictionPolicy.accessed(key);
                    storage.put(key, value);
                }
                else{
                    System.out.println("Cap is zero");
                }              
            }
        }
        else{
            storage.put(key, value);
            evictionPolicy.accessed(key);

        }
    }

    public synchronized V get(K key){
        V v = storage.get(key);
        if(v != null){
            evictionPolicy.accessed(key);
            return v;
        }
        else return null;
    }

    
}
interface Storage<K,V>{
    abstract public void put(K key, V value);
    abstract public V get(K key);
    abstract public int size();
    abstract public void remove(K key);
    abstract public boolean containsKey(K key);

}
class MapStorage<K,V> implements Storage<K,V>{
    Map<K,V> map;
    int cap;

    MapStorage(int cap){
        map = new HashMap<>();
        this.cap = cap;
    }
    public void put(K key, V value){
        map.put(key, value);
    }

    public V get(K key){
        if(!map.containsKey(key)) return null;
        else return map.get(key);
    }

    public int size(){
        return map.size();
    }
    public void remove(K k){
        map.remove(k);
    }

    public boolean containsKey(K key){
        return map.containsKey(key);
    }

    

}

interface EvictionPolicy<K, V>{
    public void accessed(K key);
    public K evictKey();
    
}

class LRUEvictionPolicy<K,V> implements EvictionPolicy<K,V>{

    LinkedHashMap<K,V> linkedHashMap;

    LRUEvictionPolicy(){
        linkedHashMap = new LinkedHashMap<>();
    }
    @Override
    public void accessed(K key) {
        if(linkedHashMap.containsKey(key)){
            V v = linkedHashMap.get(key);
            linkedHashMap.remove(key);
            linkedHashMap.put(key, v);
        }
        else{
            linkedHashMap.put(key, null);
        }
    }

    @Override
    public K evictKey() {
        if(linkedHashMap.size() == 0){
            return  null;
        }
        else{
            K k =  linkedHashMap.keySet().iterator().next();
            linkedHashMap.remove(k);
            return k;
        }
    }
    
}

 class CacheDriver{

    public static void main(String[] args) {
        Storage<String, Integer> storage = new MapStorage<String, Integer>(5);
        EvictionPolicy evictionPolicy = new LRUEvictionPolicy<String, Integer>();
        LruCache<String, Integer> cache = new LruCache<String, Integer>(storage, evictionPolicy, 3);

        cache.put("k1", 1);
        cache.put("k2", 2);
        cache.put("k3", 3);
        System.out.println(cache.get("k1")); 
        cache.put("k4", 4);// 2 removed
        System.out.println(cache.get("k4"));
        cache.put("k5", 5); // 3 removed
        System.out.println(cache.get("k4"));
        cache.get("k1");
        cache.get("k5");
        cache.put("k6", 6);
        System.out.println("Now "+cache.get("k4"));



    }

}


