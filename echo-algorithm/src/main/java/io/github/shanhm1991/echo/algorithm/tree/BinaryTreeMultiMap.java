package io.github.shanhm1991.echo.algorithm.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * MultiTreeMap的二叉树实现
 *
 * @author shanhm1991@163.com
 * @param <K>
 * @param <V>
 *
 */
public class BinaryTreeMultiMap<K extends Comparable<? super K>, V> implements MultiTreeMap<K, V> {

	protected Node<K, V> header;

	protected Node<K, V> nil;

	protected int size;

	protected final boolean deduplication;

	protected final boolean asc;

	public BinaryTreeMultiMap() {
		this(true, true);
	}

	public BinaryTreeMultiMap(boolean deduplication, boolean asc) {
		this.deduplication = deduplication;
		this.asc = asc;
		init();
	}

	protected void init() {
		nil = new Node<>(null, null, false, null, null);
		nil.left = nil;
		nil.right = nil;
		header = new Node<>(null, null, false, nil, nil);
		header.next = nil;
		nil.prev = header;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void clear() {
		header.right = nil;
		header.next = nil;
		nil.prev = header;
		size = 0;
	}

	@Override
	public V get(Object key) {
		Node<K, V> node = get(key, header.right);
		if (node != nil) {
			return node.getValue();
		}
		return null;
	}

	@Override
	public List<V> getList(Object key) {
		List<V> list = new LinkedList<>();
		Node<K, V> node = get(key, header.right);
		if (node != nil) {
			list.add(node.getValue());
			while ((node = node.next).isRepeat) {
				list.add(node.getValue());
			}
		}
		return list;
	}

	private Node<K, V> get(Object key, Node<K, V> node) {
		if (node == nil) {
			return node;
		}

		@SuppressWarnings("unchecked")
		int cmp = ((Comparable<? super K>) key).compareTo(node.getKey());
		if (cmp < 0) {
			return get(key, node.left);
		} else if (cmp > 0) {
			return get(key, node.right);
		} else {
			return node;
		}
	}

	@Override
	public V put(K key, V value) {
		if (isEmpty()) {
			Node<K, V> node = new Node<>(key, value, false, nil, nil);
			header.right = node;
			node.parent = header;
			linkIn(node, header, true);
			return null;
		} else {
			return put(key, value, header.right);
		}
	}

	private V put(K key, V value, Node<K, V> node) {
		int cmp = key.compareTo(node.getKey());
		if (cmp < 0) {
			if (node.left != nil) {
				return put(key, value, node.left);
			} else {
				Node<K, V> created = new Node<>(key, value, false, nil, nil);
				node.left = created;
				created.parent = node;
				linkIn(created, node, false);
				return null;
			}
		} else if (cmp > 0) {
			if (node.right != nil) {
				return put(key, value, node.right);
			} else {
				Node<K, V> created = new Node<>(key, value, false, nil, nil);
				node.right = created;
				created.parent = node;
				// node在链表中可能存在等值的节点
				while ((node = node.next).isRepeat) {
				}
				linkIn(created, node, false);
				return null;
			}
		} else if (deduplication) { // 覆盖
			V old = node.value;
			node.key = key;
			node.value = value;
			return old;
		} else { // 插入链表, Tree保持不变
			while ((node = node.next).isRepeat) {
			}
			V old = node.prev.value;
			linkIn(new Node<>(key, value, true, nil, nil), node, false);
			return old;
		}
	}

	protected void linkIn(Node<K, V> newNode, Node<K, V> node, boolean linkNext) {
		if (linkNext) {
			newNode.prev = node;
			newNode.next = node.next;
			node.next.prev = newNode;
			node.next = newNode;
		} else {
			newNode.next = node;
			newNode.prev = node.prev;
			node.prev.next = newNode;
			node.prev = newNode;
		}
		size++;
	}

	protected void linkOut(Node<K, V> node) {
		node.prev.next = node.next;
		node.next.prev = node.prev;
		size--;
	}

	@Override
	public V remove(Object key) {
		Node<K, V> node = get(key, header.right);
		if (node == nil) {
			return null;
		}

		V value = node.value;
		Node<K, V> next = node.next;
		if (next.isRepeat) {
			linkOut(next);
			node.key = next.key;
			node.value = next.value;
		} else {
			linkOut(node);
			remove(node);
		}
		return value;
	}

	@Override
	public void removeList(Object key) {
		List<V> list = new LinkedList<>();
		Node<K, V> node = get(key, header.right);
		if (node != nil) {
			list.add(node.getValue());
			linkOut(node);
			remove(node);
			while ((node = node.next).isRepeat) {
				list.add(node.getValue());
				linkOut(node);
			}
		}
	}

	protected void remove(Node<K, V> node) {
		Node<K, V> left = node.left;
		Node<K, V> right = node.right;
		Node<K, V> parent = node.parent;
		if (left == nil) {
			replaceChild(parent, node, right);
		} else if (right == nil) {
			replaceChild(parent, node, left);
		} else {
			Node<K, V> removed = removeLeftMax(node);
			replaceChild(parent, node, removed);
			removed.right = node.right;
			removed.right.parent = removed;
			if (removed != node.left) {
				removed.left = node.left;
				removed.left.parent = removed;
			}
		}
	}

	protected void replaceChild(Node<K, V> parent, Node<K, V> oldChild, Node<K, V> newChild) {
		newChild.parent = parent;
		if (parent.right == oldChild) {
			parent.right = newChild;
		} else {
			parent.left = newChild;
		}
	}

	// removeRightMin(BinaryNode<K, V> node)
	private Node<K, V> removeLeftMax(Node<K, V> node) {
		Node<K, V> left = node.left;
		if (left.right == nil) {
			node.left = left.left;
			left.left.parent = node;
			return left;
		}

		Node<K, V> right = left.right;
		while (right.right != nil) {
			left = right;
			right = right.right;
		}
		left.right = right.left;
		right.left.parent = left;
		return right;
	}

	@Override
	public void printTree() {
		Map<Integer, List<Node<K, V>>> map = new LinkedHashMap<>();
		build(header.right, map, 0);
		for (List<Node<K, V>> list : map.values()) {
			System.out.println(list);
		}
	}

	private void build(Node<K, V> node, Map<Integer, List<Node<K, V>>> map, int ht) {
		if (node == nil) {
			return;
		}

		List<Node<K, V>> list = map.computeIfAbsent(ht, k -> new ArrayList<>());
		list.add(node);
		build(node.left, map, ht + 1);
		build(node.right, map, ht + 1);
	}

	private Node<K, V> getHigher(K key, Node<K, V> node, Node<K, V> higher, boolean includeSelf) {
		int cmp = node.getKey().compareTo(key);
		if (cmp == 0) {
			if (includeSelf) {
				return node;
			}
			Node<K, V> right = node.right;
			if (right == nil) {
				return higher;
			}
			int cmpRight = right.getKey().compareTo(key);
			if (cmpRight > 0) {
				if (right.left == nil) { // right已经是边界， 找不到更接近的
					return right;
				}
				return getHigher(key, right.left, right, includeSelf);
			} else { // =0
				if (right.right == nil) {
					return higher;
				}
				return getHigher(key, right.right, higher, includeSelf);
			}
		} else if (cmp < 0) { // right
			Node<K, V> right = node.right;
			if (right == nil) {
				return higher;
			}
			int cmpRight = right.getKey().compareTo(key);
			if (cmpRight == 0) {
				if (includeSelf) {
					return right;
				}
				if (right.right == nil) {
					return higher;
				}
				return getHigher(key, right.right, higher, includeSelf);
			} else if (cmpRight < 0) {
				if (right.right == nil) {
					return higher;
				}
				return getHigher(key, right.right, higher, includeSelf);
			} else { // >0
				if (right.left == nil) { // right已经是边界， 找不到更接近的
					return right;
				}
				return getHigher(key, right.left, right, includeSelf);
			}
		} else { // left
			Node<K, V> left = node.left;
			if (left == nil) {
				return node;
			}
			int cmpLeft = left.getKey().compareTo(key);
			if (cmpLeft == 0) {
				if (includeSelf) {
					return left;
				}
				if (left.right == nil) {
					return node;
				}
				return getHigher(key, left.right, node, includeSelf);
			} else if (cmpLeft < 0) {
				if (left.right == nil) {
					return node;
				}
				return getHigher(key, left.right, node, includeSelf);
			} else {
				if (left.left == nil) {
					return left;
				}
				return getHigher(key, left.left, left, includeSelf);
			}
		}
	}

	@SuppressWarnings({ "rawtypes" })
	private ListIterator getIterator(K from, boolean fromInclusive, K to, boolean toInclusive, int type,
			boolean reverse) {
		Node<K, V> head;
		Node<K, V> tail;
		if (from != null) {
			head = getHigher(from, header.right, nil, fromInclusive);
			if (head == nil) {
				return createIterator(nil.prev, header.next, type, reverse);
			}
			if (to == null || to.compareTo(from) < 0) {
				return createIterator(head.prev, nil, type, reverse);
			}

			tail = getHigher(to, header.right, nil, toInclusive);
			if (tail == nil) {
				return createIterator(head.prev, nil, type, reverse);
			} else if (toInclusive) {
				if (to.compareTo(tail.getKey()) == 0) {
					// 找到的是第一个max, 定位到最后一个max的next
					while ((tail = tail.next).isRepeat) {
					}
				}
				return createIterator(head.prev, tail, type, reverse);
			} else {
				tail = tail.prev;
				if (tail == header) {
					return createIterator(head.prev, tail.next, type, reverse);
				}
				// 找到的是最后一个max的next, 定位到第一个max
				if (to.compareTo(tail.getKey()) == 0) {
					if (tail.isRepeat) {
						while ((tail = tail.prev).isRepeat) {
						}
					}
					return createIterator(head.prev, tail, type, reverse);
				}
				return createIterator(head.prev, tail.next, type, reverse); // tail
																			// <
																			// max
			}
		} else if (to != null) {
			tail = getHigher(to, header.right, nil, toInclusive);
			if (tail == nil) {
				if (toInclusive) {
					// includeMax=true -> tail.prev < max
					return createIterator(header, nil, type, reverse);
				} else {
					tail = tail.prev;
					if (tail == header) {
						return createIterator(header, tail.next, type, reverse);
					}
					// 找到的是最后一个max的next, 定位到第一个max
					if (to.compareTo(tail.getKey()) == 0) {
						if (tail.isRepeat) {
							while ((tail = tail.prev).isRepeat) {
							}
						}
						return createIterator(header, tail, type, reverse);
					}
					return createIterator(header, tail.next, type, reverse); // tail
																				// <
																				// max
				}
			} else {
				if (toInclusive) {
					// 找到的是第一个max, 定位到最后一个max的next
					if (to.compareTo(tail.getKey()) == 0) {
						while ((tail = tail.next).isRepeat) {
						}
					}
					return createIterator(header, tail, type, reverse);
				} else {
					tail = tail.prev;
					if (tail == header) {
						return createIterator(header, tail.next, type, reverse);
					}
					// 找到的是最后一个max的next, 定位到第一个max
					if (to.compareTo(tail.getKey()) == 0) {
						if (tail.isRepeat) {
							while ((tail = tail.prev).isRepeat) {
							}
						}
						return createIterator(header, tail, type, reverse);
					}
					return createIterator(header, tail.next, type, reverse); // tail
																				// <
																				// max
				}
			}
		} else {
			throw new IllegalArgumentException();
		}
	}

	@SuppressWarnings("rawtypes")
	private ListIterator createIterator(Node<K, V> prev, Node<K, V> next, int type, boolean reverse) {
		if (type == 1) {
			return new NodeIterator(prev, next, reverse);
		} else if (type == 2) {
			return new KeyIterator(prev, next, reverse);
		} else {
			return new valueIterator(prev, next, reverse);
		}
	}

	@Override
	public boolean containsKey(Object key) {
		return new KeySet(new KeyIterator(header, nil, false)).contains(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return new ValueSet(new valueIterator(header, nil, false)).contains(value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
			put(entry.getKey(), entry.getValue(), header.right);
		}
	}

	@Override
	public ListIterator<Entry<K, V>> iterator() {
		return new NodeIterator(header, nil, false);
	}

	@Override
	public Set<K> keySet() {
		return keySet(false);
	}

	@Override
	public Set<K> keySet(boolean reverse) {
		return new KeySet(new KeyIterator(header, nil, reverse));
	}

	@Override
	public Collection<V> values() {
		return values(false);
	}

	@Override
	public Collection<V> values(boolean reverse) {
		return new ValueSet(new valueIterator(header, nil, reverse));
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return entrySet(false);
	}

	@Override
	public Set<Entry<K, V>> entrySet(boolean reverse) {
		return new EntrySet(new NodeIterator(header, nil, reverse));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<K> keySet(K from, boolean fromInclusive, K to, boolean toInclusive, boolean reverse) {
		return new KeySet(getIterator(from, fromInclusive, to, toInclusive, 2, reverse));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<V> values(K from, boolean fromInclusive, K to, boolean toInclusive, boolean reverse) {
		return new ValueSet(getIterator(from, fromInclusive, to, toInclusive, 3, reverse));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<Entry<K, V>> entrySet(K from, boolean fromInclusive, K to, boolean toInclusive, boolean reverse) {
		return new EntrySet(getIterator(from, fromInclusive, to, toInclusive, 1, reverse));
	}

	private class KeySet extends AbstractSet<K> {
		public KeySet(ListIterator<K> iterator) {
			super(iterator);
		}
	}

	private class ValueSet extends AbstractSet<V> {
		public ValueSet(ListIterator<V> iterator) {
			super(iterator);
		}
	}

	private class EntrySet extends AbstractSet<Entry<K, V>> {
		public EntrySet(ListIterator<Entry<K, V>> iterator) {
			super(iterator);
		}
	}

	private static class AbstractSet<E> implements Set<E> {

		protected int setSize;

		protected ListIterator<E> iterator;

		public AbstractSet(ListIterator<E> iterator) {
			this.iterator = iterator;
		}

		@Override
		public int size() {
			if (iterator == null) {
				return 0;
			}
			while (iterator.hasNext()) {
				setSize++;
			}
			return setSize;
		}

		@Override
		public boolean isEmpty() {
			return iterator == null;
		}

		@Override
		public Iterator<E> iterator() {
			return iterator;
		}

		@Override
		public boolean contains(Object o) {
			E e;
			while (iterator.hasNext()) {
				e = iterator.next();
				if (e.equals(o)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean containsAll(Collection<?> collection) {
			for (Object o : collection) {
				if (!contains(o)) {
					return false;
				}
			}
			return true;
		}

		@Deprecated
		@Override
		public boolean remove(Object o) {
			E e;
			boolean removed = false;
			while (iterator.hasNext()) {
				e = iterator.next();
				if (e.equals(o)) {
					iterator.remove();
					removed = true;
				}
			}
			return removed;
		}

		@Deprecated
		@Override
		public boolean removeAll(Collection<?> collection) {
			boolean removed = false;
			for (Object o : collection) {
				if (remove(o)) {
					removed = true;
				}
			}
			return removed;
		}

		@Deprecated
		@Override
		public void clear() {
			while (iterator.hasNext()) {
				iterator.next();
				iterator.remove();
			}
		}

		@Override
		public Object[] toArray() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> T[] toArray(T[] a) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean add(E e) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(Collection<? extends E> c) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException();
		}
	}

	private class valueIterator extends AbstractIterator<V> {

		public valueIterator(Node<K, V> head, Node<K, V> tail, boolean reverse) {
			super(head, tail, reverse);
		}

		@Override
		protected boolean hasHead() {
			if (current == null) {
				current = head;
			}
			return current.next != tail;
		}

		@Override
		protected boolean hasTail() {
			if (current == null) {
				current = tail;
			}
			return current.prev != head;
		}

		@SuppressWarnings("unchecked")
		@Override
		public V next() {
			if (initSize != size) {
				throw new ConcurrentModificationException();
			}
			if (reverse != asc) {
				current = current.next;
			} else {
				current = current.prev;
			}
			return (V) current.getValue();
		}

		@SuppressWarnings("unchecked")
		@Override
		public V previous() {
			if (initSize != size) {
				throw new ConcurrentModificationException();
			}
			if (reverse != asc) {
				current = current.prev;
			} else {
				current = current.next;
			}
			return (V) current.getValue();
		}

		@SuppressWarnings("unchecked")
		@Override
		public void remove() {
			if (current == null || current == head || current == tail) {
				return;
			}
			linkOut(current);
			if (!current.isRepeat) {
				BinaryTreeMultiMap.this.remove(current);
			}
			initSize = size;
		}
	}

	private class KeyIterator extends AbstractIterator<K> {

		public KeyIterator(Node<K, V> head, Node<K, V> tail, boolean reverse) {
			super(head, tail, reverse);
		}

		@Override
		protected boolean hasHead() {
			if (current == null) {
				current = head;
			}
			return current.next != tail;
		}

		@Override
		protected boolean hasTail() {
			if (current == null) {
				current = tail;
			}
			return current.prev != head;
		}

		@SuppressWarnings("unchecked")
		@Override
		public K next() {
			if (initSize != size) {
				throw new ConcurrentModificationException();
			}
			if (reverse != asc) {
				current = current.next;
			} else {
				current = current.prev;
			}
			return (K) current.getKey();
		}

		@SuppressWarnings("unchecked")
		@Override
		public K previous() {
			if (initSize != size) {
				throw new ConcurrentModificationException();
			}
			if (reverse != asc) {
				current = current.prev;
			} else {
				current = current.next;
			}
			return (K) current.getKey();
		}

		@SuppressWarnings("unchecked")
		@Override
		public void remove() {
			if (current == null || current == head || current == tail) {
				return;
			}
			linkOut(current);
			if (!current.isRepeat) {
				BinaryTreeMultiMap.this.remove(current);
			}
			initSize = size;
		}
	}

	private class NodeIterator extends AbstractIterator<Entry<K, V>> {

		public NodeIterator(Node<K, V> head, Node<K, V> tail, boolean reverse) {
			super(head, tail, reverse);
		}

		@Override
		protected boolean hasHead() {
			if (current == null) {
				current = head;
			}
			return current.next != tail;
		}

		@Override
		protected boolean hasTail() {
			if (current == null) {
				current = tail;
			}
			return current.prev != head;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Entry<K, V> next() {
			if (initSize != size) {
				throw new ConcurrentModificationException();
			}
			if (reverse != asc) {
				return current = current.next;
			} else {
				return current = current.prev;
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public Entry<K, V> previous() {
			if (initSize != size) {
				throw new ConcurrentModificationException();
			}
			if (reverse != asc) {
				return current = current.prev;
			} else {
				return current = current.next;
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public void remove() {
			if (current == null || current == head || current == tail) {
				return;
			}
			linkOut(current);
			if (!current.isRepeat) {
				BinaryTreeMultiMap.this.remove(current);
			}
			initSize = size;
		}
	}

	private abstract class AbstractIterator<E> implements ListIterator<E> {

		// 这里简单处理下, 用size代替modCount来判断迭代期间是否发生修改，检测不到ABA问题
		protected int initSize;

		protected boolean reverse;

		@SuppressWarnings("rawtypes")
		protected Node head;

		@SuppressWarnings("rawtypes")
		protected Node tail;

		@SuppressWarnings("rawtypes")
		protected Node current;

		@SuppressWarnings("rawtypes")
		public AbstractIterator(Node head, Node tail, boolean reverse) {
			initSize = size;
			this.head = head;
			this.tail = tail;
			this.reverse = reverse;
		}

		@Override
		public boolean hasNext() {
			if (reverse != asc) { // !reverse == asc
				return hasHead();
			} else {
				return hasTail();
			}
		}

		@Override
		public boolean hasPrevious() {
			if (reverse != asc) {
				return hasTail();
			} else {
				return hasHead();
			}
		}

		protected abstract boolean hasHead();

		protected abstract boolean hasTail();

		@Override
		public int nextIndex() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException();
		}

		@Override
		public int previousIndex() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException();
		}

		@Override
		public void set(E e) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException();
		}

		@Override
		public void add(E e) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException();
		}
	}

	static class Node<K, V> implements Entry<K, V> {

		protected K key;

		protected V value;

		protected boolean isRepeat;

		protected Node<K, V> left;

		protected Node<K, V> right;

		protected Node<K, V> parent;

		protected Node<K, V> prev;

		protected Node<K, V> next;

		public Node(K key, V value) {
			this.key = key;
			this.value = value;
		}

		public Node(K key, V value, boolean isRepeat, Node<K, V> left, Node<K, V> right) {
			this.key = key;
			this.value = value;
			this.isRepeat = isRepeat;
			this.left = left;
			this.right = right;
		}

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public V setValue(V value) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			return "{" + key + " , " + value + "}";
		}
	}
}
