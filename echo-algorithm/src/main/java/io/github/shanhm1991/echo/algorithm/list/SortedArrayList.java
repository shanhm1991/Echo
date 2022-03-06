package io.github.shanhm1991.echo.algorithm.list;

import java.util.*;
import java.util.function.Consumer;

/**
 * 基于二分法插入的有序List，在ArrayList基础上修改
 *
 * @author shanhm1991@163.com
 * @param <E>
 */
public class SortedArrayList<E extends Comparable<? super E>> implements List<E> {

  private static final int DEFAULT_CAPACITY = 10;

  private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

  private static final Object[] EMPTY_ELEMENTDATA = {};

  private Object[] elementData;

  private int size;

  private final boolean asc;

  protected int modCount = 0;

  public SortedArrayList() {
    elementData = EMPTY_ELEMENTDATA;
    asc = true;
  }

  public SortedArrayList(boolean asc) {
    elementData = EMPTY_ELEMENTDATA;
    this.asc = asc;
  }

  public SortedArrayList(int initialCapacity, boolean asc) {
    if (initialCapacity < 0) {
      throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
    }
    elementData = new Object[initialCapacity];
    this.asc = asc;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean isEmpty() {
    return size == 0;
  }

  @Override
  public void clear() {
    modCount++;
    for (int i = 0; i < size; i++) {
      elementData[i] = null;
    }
    size = 0;
  }

  private void ensureCapacityInternal(int minCapacity) {
    if (elementData == EMPTY_ELEMENTDATA) {
      minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
    }
    ensureExplicitCapacity(minCapacity);
  }

  private void ensureExplicitCapacity(int minCapacity) {
    modCount++;
    if (minCapacity - elementData.length > 0) {
      grow(minCapacity);
    }
  }

  private void grow(int minCapacity) {
    // overflow-conscious code
    int oldCapacity = elementData.length;
    int newCapacity = oldCapacity + (oldCapacity >> 1);
    if (newCapacity - minCapacity < 0) {
      newCapacity = minCapacity;
    }
    if (newCapacity - MAX_ARRAY_SIZE > 0) {
      newCapacity = hugeCapacity(minCapacity);
    }
    // minCapacity is usually close to size, so this is a win:
    elementData = Arrays.copyOf(elementData, newCapacity);
  }

  private static int hugeCapacity(int minCapacity) {
    if (minCapacity < 0) // overflow
    {
      throw new OutOfMemoryError();
    }
    return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
  }

  @SuppressWarnings("unchecked")
  @Override
  public int indexOf(Object o) {
    if (size == 0) {
      return -1;
    }

    E e = (E) o;
    if (e.compareTo((E) elementData[0]) == 0) {
      return 0;
    }

    int index = -1;

    int left = 0;
    int right = size - 1;
    int middle = (left + right) / 2;
    while (middle != left) {
      if (e.compareTo((E) elementData[middle]) == 0) {
        index = middle;
        break;
      }
      if (asc == e.compareTo((E) elementData[middle]) < 0) {
        right = middle;
        middle = (left + right) / 2;
      } else if (asc == e.compareTo((E) elementData[middle]) > 0) {
        left = middle;
        middle = (left + right) / 2;
      }
    }

    if (index == -1) {
      return -1;
    }
    while (e.compareTo((E) elementData[--index]) == 0) {}
    return ++index;
  }

  @SuppressWarnings("unchecked")
  @Override
  public int lastIndexOf(Object o) {
    int index = indexOf(o);
    if (index == -1) {
      return -1;
    }
    E e = (E) o;
    while (e.compareTo((E) elementData[++index]) == 0 && index < size - 1) {}
    return --index;
  }

  @Override
  public boolean contains(Object o) {
    return indexOf(o) >= 0;
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    for (Object e : c) {
      if (!contains(e)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public E get(int index) {
    rangeCheck(index);
    return elementData(index);
  }

  @Override
  public E set(int index, E element) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void add(int index, E element) {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean add(E e) {
    ensureCapacityInternal(size + 1);
    if (size == 0) {
      elementData[0] = e;
      size++;
      modCount++;
      return true;
    }

    int left = 0;
    int right = size - 1;
    if (e.compareTo((E) elementData[left]) == 0) {
      System.arraycopy(elementData, 0, elementData, 1, size);
      elementData[0] = e;
    } else if (e.compareTo((E) elementData[right]) == 0) {
      elementData[right + 1] = e;
    } else if (asc == e.compareTo((E) elementData[left]) < 0) {
      System.arraycopy(elementData, 0, elementData, 1, size);
      elementData[0] = e;
    } else if (asc == e.compareTo((E) elementData[right]) > 0) {
      elementData[right + 1] = e;
    } else {
      int middle = (left + right) / 2;
      while (e.compareTo((E) elementData[middle]) != 0 && middle != left) {
        if (asc == e.compareTo((E) elementData[middle]) < 0) {
          right = middle;
          middle = (left + right) / 2;
        } else if (asc == e.compareTo((E) elementData[middle]) > 0) {
          left = middle;
          middle = (left + right) / 2;
        }
      }
      int index = middle + 1;
      System.arraycopy(elementData, index, elementData, index + 1, size - index);
      elementData[index] = e;
    }

    size++;
    modCount++;
    return true;
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    ensureCapacityInternal(size + c.size());
    for (E e : c) {
      add(e);
    }
    return true;
  }

  @Override
  public boolean addAll(int index, Collection<? extends E> c) {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("unchecked")
  E elementData(int index) {
    return (E) elementData[index];
  }

  private String outOfBoundsMsg(int index) {
    return "Index: " + index + ", Size: " + size;
  }

  private void rangeCheck(int index) {
    if (index >= size) {
      throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
  }

  @Override
  public E remove(int index) {
    rangeCheck(index);

    modCount++;
    E oldValue = elementData(index);

    int numMoved = size - index - 1;
    if (numMoved > 0) {
      System.arraycopy(elementData, index + 1, elementData, index, numMoved);
    }
    elementData[--size] = null;

    return oldValue;
  }

  @Override
  public boolean remove(Object o) {
    int index = indexOf(o);
    if (index == -1) {
      return false;
    }
    remove(index);
    return true;
  }

  @Deprecated
  @Override
  public boolean removeAll(Collection<?> c) {
    for (Object o : c) {
      remove(o);
    }
    return true;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterator<E> iterator() {
    return new Itr();
  }

  @Override
  public ListIterator<E> listIterator() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ListIterator<E> listIterator(int index) {
    throw new UnsupportedOperationException();
  }

  private class Itr implements Iterator<E> {
    int cursor; // index of next element to return
    int lastRet = -1; // index of last element returned; -1 if no such
    int expectedModCount = modCount;

    @Override
    public boolean hasNext() {
      return cursor != size;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E next() {
      checkForComodification();
      int i = cursor;
      if (i >= size) {
        throw new NoSuchElementException();
      }
      Object[] elementData = SortedArrayList.this.elementData;
      if (i >= elementData.length) {
        throw new ConcurrentModificationException();
      }
      cursor = i + 1;
      return (E) elementData[lastRet = i];
    }

    @Override
    public void remove() {
      if (lastRet < 0) {
        throw new IllegalStateException();
      }
      checkForComodification();

      try {
        SortedArrayList.this.remove(lastRet);
        cursor = lastRet;
        lastRet = -1;
        expectedModCount = modCount;
      } catch (IndexOutOfBoundsException ex) {
        throw new ConcurrentModificationException();
      }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void forEachRemaining(Consumer<? super E> consumer) {
      Objects.requireNonNull(consumer);
      final int size = SortedArrayList.this.size;
      int i = cursor;
      if (i >= size) {
        return;
      }
      final Object[] elementData = SortedArrayList.this.elementData;
      if (i >= elementData.length) {
        throw new ConcurrentModificationException();
      }
      while (i != size && modCount == expectedModCount) {
        consumer.accept((E) elementData[i++]);
      }
      // update once at end of iteration to reduce heap write traffic
      cursor = i;
      lastRet = i - 1;
      checkForComodification();
    }

    final void checkForComodification() {
      if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
      }
    }
  }

  @Override
  public Object[] toArray() {
    return Arrays.copyOf(elementData, size);
  }

  @Override
  public String toString() {
    Iterator<E> it = iterator();
    if (!it.hasNext()) {
      return "[]";
    }

    StringBuilder sb = new StringBuilder();
    sb.append('[');
    for (; ; ) {
      E e = it.next();
      sb.append(e == this ? "(this Collection)" : e);
      if (!it.hasNext()) {
        return sb.append(']').toString();
      }
      sb.append(',').append(' ');
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T[] toArray(T[] a) {
    if (a.length < size) {
      return (T[]) Arrays.copyOf(elementData, size, a.getClass());
    }
    System.arraycopy(elementData, 0, a, 0, size);
    if (a.length > size) {
      a[size] = null;
    }
    return a;
  }

  @Override
  public List<E> subList(int fromIndex, int toIndex) {
    subListRangeCheck(fromIndex, toIndex, size);
    return new SubList(this, 0, fromIndex, toIndex);
  }

  private void subListRangeCheck(int fromIndex, int toIndex, int size) {
    if (fromIndex < 0) {
      throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
    }
    if (toIndex > size) {
      throw new IndexOutOfBoundsException("toIndex = " + toIndex);
    }
    if (fromIndex > toIndex) {
      throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
    }
  }

  protected void removeRange(int fromIndex, int toIndex) {
    modCount++;
    int numMoved = size - toIndex;
    System.arraycopy(elementData, toIndex, elementData, fromIndex, numMoved);

    // clear to let GC do its work
    int newSize = size - (toIndex - fromIndex);
    for (int i = newSize; i < size; i++) {
      elementData[i] = null;
    }
    size = newSize;
  }

  private class SubList extends SortedArrayList<E> {
    private final SortedArrayList<E> parent;
    private final int parentOffset;
    private final int offset;
    int size;

    SubList(SortedArrayList<E> parent, int offset, int fromIndex, int toIndex) {
      this.parent = parent;
      parentOffset = fromIndex;
      this.offset = offset + fromIndex;
      size = toIndex - fromIndex;
      modCount = SortedArrayList.this.modCount;
    }

    @Override
    public E set(int index, E e) {
      throw new UnsupportedOperationException();
    }

    @Override
    public E get(int index) {
      rangeCheck(index);
      checkForComodification();
      return SortedArrayList.this.elementData(offset + index);
    }

    @Override
    public int size() {
      checkForComodification();
      return size;
    }

    @Override
    public void add(int index, E e) {
      throw new UnsupportedOperationException();
    }

    @Override
    public E remove(int index) {
      rangeCheck(index);
      checkForComodification();
      E result = parent.remove(parentOffset + index);
      modCount = parent.modCount;
      size--;
      return result;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
      checkForComodification();
      parent.removeRange(parentOffset + fromIndex, parentOffset + toIndex);
      modCount = parent.modCount;
      size -= toIndex - fromIndex;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
      return addAll(size, c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<E> iterator() {
      return listIterator();
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
      subListRangeCheck(fromIndex, toIndex, size);
      return new SubList(this, offset, fromIndex, toIndex);
    }

    private void rangeCheck(int index) {
      if (index < 0 || index >= size) {
        throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
      }
    }

    private void rangeCheckForAdd(int index) {
      if (index < 0 || index > size) {
        throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
      }
    }

    private String outOfBoundsMsg(int index) {
      return "Index: " + index + ", Size: " + size;
    }

    private void checkForComodification() {
      if (SortedArrayList.this.modCount != modCount) {
        throw new ConcurrentModificationException();
      }
    }

    @Override
    public ListIterator<E> listIterator(final int index) {
      checkForComodification();
      rangeCheckForAdd(index);
      final int offset = this.offset;

      return new ListIterator<E>() {
        int cursor = index;
        int lastRet = -1;
        int expectedModCount = SortedArrayList.this.modCount;

        @Override
        public boolean hasNext() {
          return cursor != size;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E next() {
          checkForComodification();
          int i = cursor;
          if (i >= size) {
            throw new NoSuchElementException();
          }
          Object[] elementData = SortedArrayList.this.elementData;
          if (offset + i >= elementData.length) {
            throw new ConcurrentModificationException();
          }
          cursor = i + 1;
          return (E) elementData[offset + (lastRet = i)];
        }

        @Override
        public boolean hasPrevious() {
          return cursor != 0;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E previous() {
          checkForComodification();
          int i = cursor - 1;
          if (i < 0) {
            throw new NoSuchElementException();
          }
          Object[] elementData = SortedArrayList.this.elementData;
          if (offset + i >= elementData.length) {
            throw new ConcurrentModificationException();
          }
          cursor = i;
          return (E) elementData[offset + (lastRet = i)];
        }

        @Override
        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> consumer) {
          Objects.requireNonNull(consumer);
          final int size = SubList.this.size;
          int i = cursor;
          if (i >= size) {
            return;
          }
          final Object[] elementData = SortedArrayList.this.elementData;
          if (offset + i >= elementData.length) {
            throw new ConcurrentModificationException();
          }
          while (i != size && modCount == expectedModCount) {
            consumer.accept((E) elementData[offset + (i++)]);
          }
          // update once at end of iteration to reduce heap write traffic
          lastRet = cursor = i;
          checkForComodification();
        }

        @Override
        public int nextIndex() {
          return cursor;
        }

        @Override
        public int previousIndex() {
          return cursor - 1;
        }

        @Override
        public void remove() {
          if (lastRet < 0) {
            throw new IllegalStateException();
          }
          checkForComodification();

          try {
            SubList.this.remove(lastRet);
            cursor = lastRet;
            lastRet = -1;
            expectedModCount = SortedArrayList.this.modCount;
          } catch (IndexOutOfBoundsException ex) {
            throw new ConcurrentModificationException();
          }
        }

        @Override
        public void set(E e) {
          throw new UnsupportedOperationException();
        }

        @Override
        public void add(E e) {
          checkForComodification();

          try {
            int i = cursor;
            SubList.this.add(i, e);
            cursor = i + 1;
            lastRet = -1;
            expectedModCount = SortedArrayList.this.modCount;
          } catch (IndexOutOfBoundsException ex) {
            throw new ConcurrentModificationException();
          }
        }

        final void checkForComodification() {
          if (expectedModCount != SortedArrayList.this.modCount) {
            throw new ConcurrentModificationException();
          }
        }
      };
    }
  }
}
