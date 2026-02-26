package net.vanfleteren.fv.rules;

import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import net.vanfleteren.fv.ErrorMessage;
import net.vanfleteren.fv.Rule;
import net.vanfleteren.fv.Validation;

public class MapRules {

    public static <K,V> Rule<Map<K,V>> notEmpty() {
        return Rule.of(map -> map != null && !map.isEmpty(),"cannot.be.empty");
    }

    public static <K,V> Rule<Map<K,V>> containsKey(K key) {
        return Rule.of(map -> map.containsKey(key), ErrorMessage.of("must.contain.key", "key", key));
    }

    public static <K,V> Rule<Map<K,V>> containsKeys(K... keys) {
        Set<K> keySet = HashSet.of(keys);
        return Rule.of(map -> map.keySet().containsAll(keySet), ErrorMessage.of("must.contain.keys", "keys", keySet));
    }

    public static <K,V> Rule<Map<K,V>> valuesNotNull() {
        return map -> {
            Set<K> keysWithNulls = map.filter((key, value) -> value == null).keySet();
            if(keysWithNulls.nonEmpty()) {
                return Validation.invalid(ErrorMessage.of("must.not.contain.null.values", "keys", keysWithNulls));
            } else {
                return Validation.valid(map);
            }
        };
    }

    //TODO do similar thing for Collections?
    public static <K,V> Rule<Map<K,V>> validateValuesWith(Rule<? super V> rule) {
        return map -> {
            Rule<V> castedRule = (Rule<V>) rule;
            Rule<Map<K, V>> rule2 =  castedRule.liftToMap();
            return rule2.test(map);
        };
    }

}
