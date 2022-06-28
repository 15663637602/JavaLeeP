package ltcode.design.medium.lruCacheImpl146;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCacheJavaApi {
    Map<Integer, Integer> cache;
    public LRUCacheJavaApi(int capacity) {
        cache = new LinkedHashMap<Integer, Integer>(capacity, .75F, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
                return size() > capacity;
            }
        };
    }

    public int get(int key) {
        return cache.getOrDefault(key, -1);
    }

    public void put(int key, int value) {
        cache.put(key, value);
    }
}
