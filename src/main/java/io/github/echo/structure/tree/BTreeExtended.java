package io.github.echo.structure.tree;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author shanhm1991@163.com
 *
 * @param <E>
 */
public class BTreeExtended<E extends Comparable<? super E>> extends BTree<E> {

	private ENode<E> root;

	public BTreeExtended(int m) {
		super(m);
		this.root = new ENode<>(m, null, null);
	}

	private static class ENode<E> extends BTree.Node<E> {

		ENode<E> next;

		ENode(int m, Entry<E> leftEntry, Entry<E> rightEntry) {
			super(m, leftEntry, rightEntry);
		}
	}

	@Override
	protected Node<E> getRoot() {
		return root;
	}

	@Override
	public void clear() {
		root = new ENode<>(m, null, null);
		size = 0;
	}

	@Override
	public void add(E e){
		add(e, root, height);
	}

	@Override
	protected void fixAdd(Node<E> node, int ht) {
		if(ht == 0){
			fixLeafAdd((ENode<E>)node, ht);
		}else{
			super.fixAdd(node, ht);
		}
	}

	private void fixLeafAdd(ENode<E> leaf, int ht){
		if(leaf.entrys[m - 1] == null){
			return;
		}

		Entry<E>[] entrys = leaf.entrys;
		Entry<E> middle = entrys[middleIndex + 1];  
		//实际中这里新建的关键字不需要包含卫星数据data，只要索引数据
		Entry<E> created = new Entry<>(middle.element, 0, null, null, null); 

		ENode<E> right = new ENode<>(m, created, null); 
		leaf.next = right;
		created.leftNode = leaf;
		created.rightNode = right;

		initEntrys(leaf, m);
		System.arraycopy(entrys, 0, leaf.entrys, 0, middleIndex + 1);
		System.arraycopy(entrys, middleIndex + 1, right.entrys, 0, middleIndex + 2);
		fixEntryNode(right.entrys, right);
		fixIndex(0, right);

		//created插入到上层Node, 并确定它的前后Entry, 修正相互关联
		if(leaf.leftEntry != null){
			Entry<E> preEntry = leaf.leftEntry; 
			Node<E> parent = preEntry.node;

			int index = preEntry.index;
			Entry<E> afterEntry = parent.entrys[index + 1]; 
			if(afterEntry != null){
				afterEntry.leftNode = right;
			}
			right.rightEntry = afterEntry;
			leaf.rightEntry = created;

			System.arraycopy(parent.entrys, index + 1, parent.entrys, index + 2, m - index - 2);
			parent.entrys[index + 1] = created;
			created.node = parent;
			fixIndex(index, parent);
			fixAdd(parent, ht + 1);
		}else if(leaf.rightEntry != null){ 
			Entry<E> afterEntry = leaf.rightEntry; 
			Node<E> parent = afterEntry.node;

			afterEntry.leftNode = right;
			right.rightEntry = afterEntry;
			leaf.rightEntry = created;

			int index = afterEntry.index;
			System.arraycopy(parent.entrys, index, parent.entrys, index + 1, m - index - 1);
			parent.entrys[index] = created;
			created.node = parent;
			fixIndex(index, parent);
			fixAdd(parent, ht + 1);
		}else{
			height++;
			root = new ENode<>(m, null, null);
			root.entrys[0] = created;
			created.node = root; 
		}
	}

	@Override
	public void remove(E e) { 
		//TODO 
	}

	@Override
	public String toString() {
		Entry<E> entry = findMin(root, height);
		ENode<E> node = (ENode<E>)entry.node;

		List<E> list = new LinkedList<>();
		build(list, node);
		while((node = node.next) != null){
			build(list, node);
		}
		return list.toString();
	}

	private void build(List<E> list, ENode<E> node){
		for(Entry<E> entry : node.entrys){
			if(entry == null){
				return;
			}
			list.add(entry.element);
		}
	}
}
