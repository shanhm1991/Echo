package io.github.shanhm1991.echo.algorithm;

import java.util.Comparator;
import java.util.List;

/**
 * 堆
 *
 * @author shanhm1991@163.com
 */
public class SortHeap<T extends Comparable<? super T>> {

  private final Comparator<? super T> comp; // 比较器

  private final Object[] array; // 元素数组

  private final int capacity; // 容量

  private int size; // 元素实际个数

  private int compTimes; // 记录比较次数

  public SortHeap(int capacity, Comparator<? super T> comp) {
    this.comp = comp;
    this.capacity = capacity;
    array = new Object[capacity + 1];
  }

  public SortHeap(int capacity, List<T> list, Comparator<? super T> comp) {
    this.comp = comp;
    this.capacity = capacity;
    array = new Object[capacity + 1];
    for (T t : list) {
      put(t);
    }
  }

  public int getCompTimes() {
    return compTimes;
  }

  public int size() {
    return size;
  }

  @SuppressWarnings("all")
  public T get() {
    return (T) (array[0]);
  }

  @SuppressWarnings("all")
  public T take() {
    if (size == 0) {
      return null;
    }

    T t = (T) array[0];
    array[0] = array[size - 1];
    if (size > 2) {
      down(0, size, array);
    }
    size--;
    return t;
  }

  public void put(T t) {
    array[size] = t;
    if (size > 0) {
      up(size);
    }
    if (++size > capacity) {
      take();
    }
  }

  private void down(int k, int len, Object[] array) {
    int child;
    while ((child = 2 * k + 1) < len) {
      if (child + 1 < len && comp(array, child, child + 1, comp) < 0) {
        child++; // 从左右子节点中选取一个较大值
      }
      if (comp(array, k, child, comp) > 0) {
        break; // 如果父结点已经不小于左右子节点，则直接结束
      }

      swap(array, k, child); // 父节点下沉，子节点上浮
      k = child; // 继续向下比较
    }
  }

  private void up(int k) {
    while (k > 0 && comp(array, (k - 1) / 2, k, comp) < 0) {
      swap(array, (k - 1) / 2, k);
      k = (k - 1) / 2;
    }
  }

  private void swap(Object[] array, int a, int b) {
    Object t = array[a];
    array[a] = array[b];
    array[b] = t;
  }

  @SuppressWarnings("all")
  private int comp(Object[] array, int a, int b, Comparator comp) {
    compTimes++;
    if (comp != null) {
      return comp.compare(array[b], array[a]);
    } else {
      return ((Comparable) array[b]).compareTo(array[a]);
    }
  }
}
