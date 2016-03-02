package other;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by dave on 7/22/15.
 */
public class MapUtils {

    public static <K, V> Map<K, V> emptyMap(){
        return new HashMap<>();
    }

    public static Map<String, String> singlePropertyMap(Map<String, String[]> map) {
        return singlePropertyMapOfType(map, HashMap::new);
    }

    public static Map<String, String> singlePropertyMapOfType(Map<String, String[]> map, Supplier<Map<String, String>> mapType){
        return map.entrySet().parallelStream()
                .map(e -> {
                    String value = e.getValue().length > 0 ? e.getValue()[0] : null;
                    return new HashMap.SimpleEntry<>(e.getKey(), value);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, mapType));

    }

    public static <K, V> Builder<K, V> builder(K key, V value){
        return new Builder().put(key, value);
    }

    public static <K> Builder<K, Object> params(K key, Object value){
        return new Builder().put(key, value);
    }


    public static class Builder<K, V> {
        Map<K, V> entries = new HashMap<>();

        public Builder<K, V> put(K key, V value) {
            entries.put(key, value);
            return this;
        }

        public Map<K, V> build() {
            return entries;
        }

        public MultiValueMap<K, V> buildMulti(){
            LinkedMultiValueMap<K, V> result = new LinkedMultiValueMap<K, V>();
            entries.forEach( (k,v) -> result.add(k, v));
            return result;
        }
    }

}
