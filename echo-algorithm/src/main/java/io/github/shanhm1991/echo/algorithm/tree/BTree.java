package io.github.shanhm1991.echo.algorithm.tree;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * B树
 *
 * @author shanhm1991@163.com
 * @param <E>
 *
 */
public class BTree<E extends Comparable<? super E>> implements Tree<E> {

	protected final int m;

	protected final int middleIndex;

	protected int size;

	protected int height;

	private Node<E> root;

	public BTree(int m) {
		if (m < 3) {
			throw new IllegalArgumentException();
		}
		this.m = m;
		middleIndex = (m - 1) / 2 - 1;
		root = new Node<>(m, null, null);
	}

	public static class Node<E> {

		int m;

		Entry<E> leftEntry;

		Entry<E> rightEntry;

		Entry<E>[] entrys;

		Node(int m, Entry<E> leftEntry, Entry<E> rightEntry) {
			this.m = m;
			this.leftEntry = leftEntry;
			this.rightEntry = rightEntry;
			initEntrys(this, m);
		}

		@Override
		public String toString() {
			ArrayList<Entry<E>> list = new ArrayList<>(m);
			for (Entry<E> entry : entrys) {
				if (entry == null) {
					break;
				}
				list.add(entry);
			}
			return list.toString();
		}
	}

	static class Entry<E> {

		int index;

		E element;

		Node<E> node;

		Node<E> leftNode;

		Node<E> rightNode;

		Entry(E e, int index, Node<E> node, Node<E> leftNode, Node<E> rightNode) {
			this.index = index;
			element = e;
			this.node = node;
			this.leftNode = leftNode;
			this.rightNode = rightNode;
		}

		@Override
		public String toString() {
			return element.toString();
		}
	}

	@SuppressWarnings({ "unchecked" })
	static <E> void initEntrys(Node<E> node, int m) {
		node.entrys = new Entry[m];
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
		root = new Node<>(m, null, null);
		size = 0;
	}

	@Override
	public boolean contains(E e) {
		return contains(e, root, height);
	}

	private boolean contains(E e, Node<E> node, int ht) {
		if (ht == 0) {
			// 与叶子节点的关键字自左向右依次比较,如果关键字为空或e小于关键字则可以提前结束
			for (int i = 0; i < m - 1; i++) {
				Entry<E> entry = node.entrys[i];
				if (entry == null) {
					return false;
				}

				int compare = e.compareTo(entry.element);
				if (compare == 0) {
					return true;
				} else if (compare < 0) {
					return false;
				}
			}
		} else {
			for (int i = 0; i < m - 1; i++) {
				Entry<E> entry = node.entrys[i];
				int compare = e.compareTo(entry.element);
				if (compare == 0) {
					return true;
				} else if (compare < 0) {
					return contains(e, entry.leftNode, ht - 1);
				} else if (i == m - 2 || node.entrys[i + 1] == null || e.compareTo(node.entrys[i + 1].element) < 0) {
					return contains(e, entry.rightNode, ht - 1);
				}
			}
		}
		return false;
	}

	@Override
	public E getMin() {
		return findMin(root, height).element;
	}

	protected Entry<E> findMin(Node<E> node, int ht) {
		if (ht == 0) {
			return node.entrys[0];
		} else {
			return findMin(node.entrys[0].leftNode, ht - 1);
		}
	}

	@Override
	public E getMax() {
		return findMax(root, height).element;
	}

	private Entry<E> findMax(Node<E> node, int ht) {
		if (ht == 0) {
			for (int i = 0; i < m - 1; i++) {
				if (node.entrys[i + 1] == null) {
					return node.entrys[i];
				}
			}
		} else {
			for (int i = 0; i < m - 1; i++) {
				if (node.entrys[i + 1] == null) {
					return findMax(node.entrys[i].rightNode, ht - 1);
				}
			}
		}
		return null;
	}

	@Override
	public void add(E e) {
		add(e, root, height);
	}

	protected void add(E e, Node<E> node, int ht) {
		if (ht == 0) {
			for (int i = 0; i < m; i++) {
				Entry<E> entry = node.entrys[i];
				if (entry == null) {
					node.entrys[i] = new Entry<>(e, i, node, null, null);
					size++;
					fixAdd(node, ht);
					return;
				}

				int compare = e.compareTo(entry.element);
				if (compare <= 0) { // <=0往左放，>0往右放
					System.arraycopy(node.entrys, i, node.entrys, i + 1, m - i - 1);
					node.entrys[i] = new Entry<>(e, i, node, null, null);
					size++;
					fixIndex(i, node);
					fixAdd(node, ht);
					return;
				}
			}
		} else {
			for (int i = 0; i < m - 1; i++) {
				Entry<E> entry = node.entrys[i];
				int compare = e.compareTo(entry.element);
				if (compare <= 0) {
					add(e, entry.leftNode, ht - 1);
					return;
				} else if (i == m - 2 || node.entrys[i + 1] == null || e.compareTo(node.entrys[i + 1].element) <= 0) {
					add(e, entry.rightNode, ht - 1);
					return;
				}
			}
		}
	}

	protected void fixAdd(Node<E> node, int ht) {
		if (node.entrys[m - 1] == null) {
			return;
		}

		// 拆分node，并重新建立相互关联
		Entry<E> middle = node.entrys[middleIndex + 1];
		Node<E> oldLeft = middle.leftNode;
		Node<E> oldRight = middle.rightNode;
		if (oldLeft != null) { // left与right一定同时为空，或同时不为空
			oldLeft.rightEntry = null;
			oldRight.leftEntry = null;
		}

		Node<E> newLeft = new Node<>(m, null, middle);
		Node<E> newRight = new Node<>(m, middle, null);
		middle.leftNode = newLeft;
		middle.rightNode = newRight;

		System.arraycopy(node.entrys, 0, newLeft.entrys, 0, middleIndex + 1);
		fixEntryNode(newLeft.entrys, newLeft);
		System.arraycopy(node.entrys, middleIndex + 2, newRight.entrys, 0, middleIndex + 1);
		fixEntryNode(newRight.entrys, newRight);
		fixIndex(0, newRight);

		// middle插入到上层Node, 并确定它的前后Entry, 修正相互关联
		if (node.leftEntry != null) {
			Entry<E> preEntry = node.leftEntry;
			Node<E> parent = preEntry.node;

			preEntry.rightNode = newLeft;
			int index = preEntry.index;
			Entry<E> afterEntry = parent.entrys[index + 1];
			if (afterEntry != null) {
				afterEntry.leftNode = newRight;
			}
			newLeft.leftEntry = preEntry;
			newRight.rightEntry = afterEntry;

			System.arraycopy(parent.entrys, index + 1, parent.entrys, index + 2, m - index - 2);
			parent.entrys[index + 1] = middle;
			middle.node = parent;
			fixIndex(index, parent);
			fixAdd(parent, ht + 1);
		} else if (node.rightEntry != null) {
			Entry<E> afterEntry = node.rightEntry;
			Node<E> parent = afterEntry.node;

			afterEntry.leftNode = newRight;
			int index = afterEntry.index;
			Entry<E> preEntry = null;
			if (index >= 1) {
				preEntry = parent.entrys[index - 1];
				preEntry.rightNode = newLeft;
			}
			newLeft.leftEntry = preEntry;
			newRight.rightEntry = afterEntry;

			System.arraycopy(parent.entrys, index, parent.entrys, index + 1, m - index - 1);
			parent.entrys[index] = middle;
			middle.node = parent;
			fixIndex(index, parent);
			fixAdd(parent, ht + 1);
		} else {
			// root = node
			// 上面left和right重新创建而没有用分裂,
			// 就是为了这里可以直接对root进行修改，这样方便子类复用
			height++;
			initEntrys(node, m);
			node.entrys[0] = middle;
			fixIndex(0, node);
		}
	}

	protected void fixIndex(int index, Node<E> node) {
		for (int i = index; i < m; i++) {
			if (node.entrys[i] == null) {
				return;
			}
			node.entrys[i].index = i;
		}
	}

	protected void fixEntryNode(Entry<E>[] entrys, Node<E> node) {
		for (Entry<E> entry : entrys) {
			if (entry == null) {
				return;
			}
			entry.node = node;
		}
	}

	@Override
	public void remove(E e) {
		Entry<E> entry = findRemoveEntry(e, root, height);
		if (entry == null) {
			return;
		}
		remove(entry);
	}

	protected void remove(Entry<E> entry) {
		Node<E> node = entry.node;
		Entry<E>[] entrys = node.entrys;
		Node<E> oldLeft = entrys[0].leftNode;
		Node<E> oldRight = getMaxEntry(node).rightNode;

		// 删除entry, 如果删除之后middleIndex处依然有值，则直接结束
		// 即便node不是树叶，它也在之前的递归删除中将下层Node处理结束了
		System.arraycopy(entrys, entry.index + 1, entrys, entry.index, m - entry.index - 1);
		if (entrys[middleIndex] != null) {
			return;
		}

		// 确定node关联的上层Entry，以及与它的左右关系
		if (node.rightEntry != null) {
			Entry<E> parentEntry = node.rightEntry;
			Node<E> parent = parentEntry.node;
			Node<E> brother = parentEntry.rightNode;

			// 在middleIndex处创建一个Entry, 并建立相互关联
			Entry<E> brotherMinEntry = brother.entrys[0];
			Entry<E> middle = new Entry<>(parentEntry.element, middleIndex, node, oldRight, brotherMinEntry.leftNode);
			entrys[middleIndex] = middle;
			if (oldRight != null) { // 同一层的节点必然同时存在
				oldRight.rightEntry = middle;
				brotherMinEntry.leftNode.leftEntry = middle;
			}

			if (brother.entrys[middleIndex + 1] != null) { // brother是富足的
				// 删除brother.entrys[0], 并将parentEntry的值设为brother.entrys[0]的值
				System.arraycopy(brother.entrys, 1, brother.entrys, 0, m - 1);
				parentEntry.element = brotherMinEntry.element;
				fixIndex(0, node);
				fixIndex(0, brother);
			} else {
				System.arraycopy(brother.entrys, 0, entrys, middleIndex + 1, middleIndex + 1);
				fixEntryNode(brother.entrys, node);
				fixIndex(0, node);
				fixIndex(0, parent);

				parentEntry.rightNode = node;
				node.rightEntry = null;
				if (parentEntry.index < m - 2 && parent.entrys[parentEntry.index + 1] != null) {
					node.rightEntry = parent.entrys[parentEntry.index + 1];
					parent.entrys[parentEntry.index + 1].leftNode = node;
				}
				// 递归删除父节点中的parentEntry
				remove(parentEntry);
			}
		} else if (node.leftEntry != null) { // 对称情形
			Entry<E> parentEntry = node.leftEntry;
			Node<E> parent = parentEntry.node;
			Node<E> brother = parentEntry.leftNode;

			Entry<E> brotherMaxEntry = getMaxEntry(brother);
			if (brother.entrys[middleIndex + 1] != null) {
				System.arraycopy(entrys, 0, entrys, 1, middleIndex + 1);
				Entry<E> first = new Entry<>(parentEntry.element, 0, node, brotherMaxEntry.rightNode, oldLeft);
				entrys[0] = first;
				if (oldLeft != null) {
					oldLeft.leftEntry = first;
					brotherMaxEntry.rightNode.rightEntry = first;
				}

				brother.entrys[brotherMaxEntry.index] = null;
				parentEntry.element = brotherMaxEntry.element;
				fixIndex(0, node);
			} else {
				Entry<E> last = new Entry<>(parentEntry.element, middleIndex + 1, brother, brotherMaxEntry.rightNode,
						oldLeft);
				if (oldLeft != null) {
					brotherMaxEntry.rightNode.rightEntry = last;
					oldLeft.leftEntry = last;
				}
				brother.entrys[middleIndex + 1] = last;

				System.arraycopy(entrys, 0, brother.entrys, middleIndex + 2, middleIndex + 1);
				fixEntryNode(entrys, brother);
				fixIndex(0, parent);
				fixIndex(0, brother);

				// 合并统一自右向左, 所以只需要修正rightEntry
				parentEntry.rightNode = brother;
				node.rightEntry = null;
				if (parentEntry.index < m - 2 && parent.entrys[parentEntry.index + 1] != null) {
					brother.rightEntry = parent.entrys[parentEntry.index + 1];
					parent.entrys[parentEntry.index + 1].leftNode = brother;
				}
				remove(parentEntry);
			}
		} else { // root
			if (entrys[0] == null) {
				height--;
				root = entry.leftNode;
				root.rightEntry = null;
			}
		}
	}

	private Entry<E> findRemoveEntry(E e, Node<E> node, int ht) {
		if (ht == 0) {
			for (int i = 0; i < m - 1; i++) {
				Entry<E> entry = node.entrys[i];
				if (entry == null) {
					return null;
				}

				int compare = e.compareTo(entry.element);
				if (compare < 0) {
					return null;
				} else if (compare == 0) {
					return entry;
				}
			}
		} else {
			for (int i = 0; i < m - 1; i++) {
				Entry<E> entry = node.entrys[i];
				int compare = e.compareTo(entry.element);
				if (compare == 0) {
					Entry<E> leaf = findMax(entry.leftNode, ht - 1);
					// findMin(entry.right, ht - 1);
					entry.element = leaf.element;
					return leaf;
				} else if (compare < 0) {
					return findRemoveEntry(e, entry.leftNode, ht - 1);
				} else if (i == m - 2 || node.entrys[i + 1] == null || e.compareTo(node.entrys[i + 1].element) < 0) {
					return findRemoveEntry(e, entry.rightNode, ht - 1);
				}
			}
		}
		return null;
	}

	private Entry<E> getMaxEntry(Node<E> node) {
		for (int i = m - 1; i >= 0; i--) {
			if (node.entrys[i] != null) {
				return node.entrys[i];
			}
		}
		return null;
	}

	@Override
	public List<E> toList() {
		List<E> list = new ArrayList<>(size);
		if (!isEmpty()) {
			link(list, root);
		}

		return list;
	}

	@Override
	public String toString() {
		return toList().toString();
	}

	private void link(List<E> list, Node<E> node) {
		if (node != null) {
			Entry<E>[] entrys = node.entrys;
			for (int i = 0; i < m - 1; i++) {
				Entry<E> entry = entrys[i];

				if (entry == null) {
					return;
				}
				if (i == 0) {
					if (entry.leftNode != null) {
						link(list, entry.leftNode);
					}
					list.add(entry.element);
					link(list, entry.rightNode);
				} else {
					list.add(entry.element);
					link(list, entry.rightNode);
				}
			}
		}
	}

	protected Node<E> getRoot() {
		return root;
	}

	@Override
	public void printTree() {
		Map<Integer, List<Node<E>>> map = new LinkedHashMap<>();
		build(getRoot(), height, map);
		for (List<Node<E>> list : map.values()) {
			System.out.println(list);
		}
	}

	private void build(Node<E> node, int ht, Map<Integer, List<Node<E>>> map) {
		if (node == null) {
			return;
		}

		List<Node<E>> list = map.get(ht);
		if (list == null) {
			list = new ArrayList<>();
			map.put(ht, list);
		}
		list.add(node);

		Entry<E>[] entrys = node.entrys;
		for (int i = 0; i < m - 1; i++) {
			Entry<E> entry = entrys[i];
			if (entry == null) {
				return;
			}
			if (i == 0) {
				build(entry.leftNode, ht - 1, map);
				build(entry.rightNode, ht - 1, map);
			} else {
				build(entry.rightNode, ht - 1, map);
			}
		}
	}
}
