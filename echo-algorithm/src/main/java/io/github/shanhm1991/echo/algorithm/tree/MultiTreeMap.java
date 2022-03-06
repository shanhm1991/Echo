package io.github.shanhm1991.echo.algorithm.tree;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * TreeMap扩展接口，允许多值
 *
 * @author shanhm1991@163.com
 * @param <K>
 * @param <V>
 */
public interface MultiTreeMap<K, V> extends Map<K, V>, Iterable<Entry<K, V>> {

  List<V> getList(Object key);

  void removeList(Object key);

  Set<K> keySet(boolean reverse);

  Set<K> keySet(K from, boolean fromInclusive, K to, boolean toInclusive, boolean reverse);

  Collection<V> values(boolean reverse);

  Collection<V> values(K from, boolean fromInclusive, K to, boolean toInclusive, boolean reverse);

  Set<Entry<K, V>> entrySet(boolean reverse);

  Set<Entry<K, V>> entrySet(
      K from, boolean fromInclusive, K to, boolean toInclusive, boolean reverse);

  void printTree();
}
