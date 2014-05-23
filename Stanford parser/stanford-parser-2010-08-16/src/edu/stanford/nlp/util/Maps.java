package edu.stanford.nlp.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utilities for Maps, including inverting, composing, and support for list/set values.
 * <p/>
 * @author Dan Klein (klein@cs.stanford.edu)
 * Date: Oct 22, 2003
 * Time: 8:56:16 PM
 */
public class Maps {
  /**
   * Adds the value to the HashSet given by map.get(key), creating a new HashMap if needed.
   *
   */
  public static <K, V> void putIntoValueHashSet(Map<K, Set<V>> map, K key, V value) {
    CollectionFactory<V> factory = CollectionFactory.hashSetFactory();
    putIntoValueCollection(map, key, value, factory);
  }

  /**
   * Adds the value to the ArrayList given by map.get(key), creating a new ArrayList if needed.
   *
   */
  public static <K, V> void putIntoValueArrayList(Map<K, List<V>> map, K key, V value) {
    CollectionFactory<V> factory = CollectionFactory.arrayListFactory();
    putIntoValueCollection(map, key, value, factory);
  }

  /**
   * Adds the value to the collection given by map.get(key).  A new collection is created using the supplied CollectionFactory.
   *
   */
  public static <K, V, C extends Collection<V>> void putIntoValueCollection(Map<K, C> map, K key, V value, CollectionFactory<V> cf) {
    C c = map.get(key);
    if (c == null) {
      c = ErasureUtils.<C>uncheckedCast(cf.newCollection());
      map.put(key, c);
    }
    c.add(value);
  }

  /**
   * Compose two maps map1:x->y and map2:y->z to get a map x->z
   *
   * @return The composed map
   */
  public static <X, Y, Z> Map<X, Z> compose(Map<X, Y> map1, Map<Y, Z> map2) {
    Map<X, Z> composedMap = new HashMap<X, Z>();
    for (X key : map1.keySet()) {
      composedMap.put(key, map2.get(map1.get(key)));
    }
    return composedMap;
  }

  /**
   * Inverts a map x->y to a map y->x assuming unique preimages.  If they are not unique, you get an arbitrary ones as the values in the inverted map.
   *
   * @return The inverted map
   */
  public static <X, Y> Map<Y, X> invert(Map<X, Y> map) {
    Map<Y, X> invertedMap = new HashMap<Y, X>();
    for (Map.Entry<X, Y> entry : map.entrySet()) {
      X key = entry.getKey();
      Y value = entry.getValue();
      invertedMap.put(value, key);
    }
    return invertedMap;
  }

  /**
   * Inverts a map x->y to a map y->pow(x) not assuming unique preimages.
   *
   * @return The inverted set
   */
  public static <X, Y> Map<Y, Set<X>> invertSet(Map<X, Y> map) {
    Map<Y, Set<X>> invertedMap = new HashMap<Y, Set<X>>();
    for (Map.Entry<X, Y> entry : map.entrySet()) {
      X key = entry.getKey();
      Y value = entry.getValue();
      putIntoValueHashSet(invertedMap, value, key);
    }
    return invertedMap;
  }

  public static void main(String[] args) {
    Map<String, String> map1 = new HashMap<String, String>();
    map1.put("a", "1");
    map1.put("b", "2");
    map1.put("c", "2");
    map1.put("d", "4");
    Map<String, String> map2 = new HashMap<String, String>();
    map2.put("1", "x");
    map2.put("2", "y");
    map2.put("3", "z");
    System.out.println("map1: " + map1);
    System.out.println("invert(map1): " + Maps.invert(map1));
    System.out.println("invertSet(map1): " + Maps.invertSet(map1));
    System.out.println("map2: " + map2);
    System.out.println("compose(map1,map2): " + Maps.compose(map1, map2));
    Map<String, Set<String>> setValues = new HashMap<String, Set<String>>();
    Map<String, List<String>> listValues = new HashMap<String, List<String>>();
    Maps.putIntoValueArrayList(listValues, "a", "1");
    Maps.putIntoValueArrayList(listValues, "a", "1");
    Maps.putIntoValueArrayList(listValues, "a", "2");
    Maps.putIntoValueHashSet(setValues, "a", "1");
    Maps.putIntoValueHashSet(setValues, "a", "1");
    Maps.putIntoValueHashSet(setValues, "a", "2");
    System.out.println("listValues: " + listValues);
    System.out.println("setValues: " + setValues);
  }
}
