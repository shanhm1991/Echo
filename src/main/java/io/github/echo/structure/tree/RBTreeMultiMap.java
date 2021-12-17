package io.github.echo.structure.tree;

/**
 * 
 * @author shanhm1991@163.com
 *
 * @param <K>
 * @param <V>
 */
public class RBTreeMultiMap<K extends Comparable<? super K>, V> extends BinaryTreeMultiMap<K, V> {
	
	public RBTreeMultiMap() {
		
	}
	
	public RBTreeMultiMap(boolean deduplication, boolean asc){
		super(deduplication, asc);
	}

	@Override
	protected void init() {
		nil = new RBNode<K, V>(null, null, false, null, null);
		nil.left = nil;
		nil.right = nil;
		header = new RBNode<K, V>(null, null, false, nil, nil);
		header.next = nil;
		nil.prev = header;
	}

	public V put(K key, V value){
		nil.key = key;
		RBNode<K, V> current = (RBNode<K, V>)header;
		RBNode<K, V> parent = (RBNode<K, V>)header; 
		while(compare(key, current) != 0){
			parent = current;
			if(compare(key, current) < 0){
				current = getLeft(current);
			}else{
				current = getRight(current);
			}
			if(getLeft(current).isRed && getRight(current).isRed){
				current = handleReorient(current);
			}
		}

		if(current != nil){
			if(deduplication){
				V old = current.value;
				current.key = key;
				current.value = value;
				return old;
			}else{
				while((current = getNext(current)).isRepeat); 
				V old = current.prev.value;
				linkIn(new RBNode<>(key, value, true, nil, nil), current, false);
				return old;
			}
		}

		current = new RBNode<>(key, value, false, nil, nil);
		current.parent = parent;
		if(compare(key, parent) < 0){
			parent.left = current;
			linkIn(current, parent, false);
		}else{
			parent.right = current;
			while((parent = getNext(parent)).isRepeat);
			linkIn(current, parent, false);
		}
		//将新数据节点设成红色，如果父亲节点为红色则需要旋转调整
		handleReorient(current);
		return null;
	}

	private RBNode<K, V> handleReorient(RBNode<K, V> current) {
		getLeft(current).isRed = false;  
		getRight(current).isRed = false; 
		current.isRed = true;

		RBNode<K, V> subRoot = current; 
		RBNode<K, V> parent = getParent(current);
		if(parent.isRed){
			RBNode<K, V> grand = getParent(parent);
			grand.isRed = true;
			//旋转parent, 向外旋转到同一侧 
			if(compare(current.getKey(), grand) != compare(current.getKey(), parent)) {
				if(compare(current.getKey(), parent) > 0){
					rotateLeft(parent);
				}else{
					rotateRight(parent);
				}
			}
			//旋转grand
			if(compare(current.getKey(), grand) < 0){
				subRoot = getLeft(grand);
				subRoot.isRed = false;
				rotateRight(grand);
			}else{
				subRoot = getRight(grand);
				subRoot.isRed = false;
				rotateLeft(grand);
			}
		}
		//直接将根节点置为黑色 
		getRight(header).isRed = false;
		return subRoot;
	}

	private void rotateRight(Node<K, V> node) {
		Node<K, V> parent = node.parent;
		Node<K, V> left = node.left;
		Node<K, V> leftRight = left.right;
		left.right = node;
		node.parent = left;
		node.left = leftRight;
		leftRight.parent = node;
		left.parent = parent;
		if(parent.right == node){
			parent.right = left;
		}else{
			parent.left = left;
		}
	}

	private void rotateLeft(Node<K, V> node) {
		Node<K, V> parent = node.parent;
		Node<K, V> right = node.right;
		Node<K, V> rightLeft = right.left;
		right.left = node;
		node.parent = right;
		node.right = rightLeft;
		rightLeft.parent = node;
		right.parent = parent;
		if(parent.right == node){
			parent.right = right;
		}else{
			parent.left = right;
		}
	}

	private int compare(K key, Entry<K, V> node) {
		if(node == header){
			return 1;
		}else{
			return key.compareTo(node.getKey());
		}
	}

	private RBNode<K, V> getParent(Node<K, V> node){
		return (RBNode<K, V>)node.parent;
	}

	private RBNode<K, V> getNext(Node<K, V> node){
		return (RBNode<K, V>)node.next;
	}

	private RBNode<K, V> getLeft(Node<K, V> node){
		return (RBNode<K, V>)node.left;
	}

	private RBNode<K, V> getRight(Node<K, V> node){
		return (RBNode<K, V>)node.right;
	}

	@Override
	protected void remove(Node<K, V> node){
		RBNode<K, V> rbnode = (RBNode<K, V>)node;
		RBNode<K, V> current = rbnode;
		RBNode<K, V> parent = getParent(current);
		if(current.right != nil){
			current = getRight(current);
			while(current.left != nil){
				parent = current;
				current = getLeft(current);
			}
			locationExchange(rbnode, current);
			if(rbnode.right != nil){//node.right is red
				parent = rbnode;
				rbnode = getRight(rbnode);
				locationExchange(parent, rbnode);
				rbnode.right = nil; //交换过位置
				return;
			}
		}else if(current.left != nil){
			current = getLeft(current);
			while(current.right != nil){
				parent = current;
				current = getRight(current);
			}
			locationExchange(rbnode, current);
			if(rbnode.left != nil){//node.left is red
				parent = rbnode;
				rbnode = getLeft(rbnode);
				locationExchange(parent, rbnode);
				rbnode.left = nil;
				return;
			}
		}

		if(!rbnode.isRed){
			fixRemove(rbnode);
		}else if(rbnode == parent.left){
			parent.left = nil;
		}else{
			parent.right = nil;
		}
	}
	
	//这里不能简单的交换节点的值，因为要保证链表和树中删除的是同一个节点实例即node
	private void locationExchange(RBNode<K, V> node, RBNode<K, V> current){
		RBNode<K, V> parent = getParent(node);
		RBNode<K, V> left = getLeft(node);
		RBNode<K, V> right = getRight(node);
		boolean isRed = node.isRed;
		replaceChild(current.parent, current, node);
		node.left = current.left;
		node.left.parent = node;
		node.right = current.right;
		node.right.parent = node;
		node.isRed = current.isRed;
		replaceChild(parent, node, current);
		current.left = left;
		left.parent = current;
		current.right = right;
		right.parent = current;
		current.isRed = isRed;
	}

	private void fixRemove(RBNode<K, V> node){
		RBNode<K, V> parent = getParent(node);
		if(parent == header){
			return;
		}

		if(node == parent.left){
			parent.left = nil;
			RBNode<K, V> brother = getRight(parent);
			if(parent.isRed){ // 1.parent为红色
				if(getLeft(brother).isRed){ 
					parent.isRed = false;
					rotateRight(brother);
				}
			}else if(brother.isRed){ // 2.brother为红色
				if(!getLeft(brother.left).isRed && !getRight(brother.left).isRed){
					brother.isRed = false;
					getLeft(brother).isRed = true;
				}else if(getRight(brother.left).isRed){
					getRight(brother.left).isRed = false;
					rotateRight(brother);
				}else{
					getLeft(brother.left).isRed = false;
					rotateRight(brother.left);
					rotateRight(brother);
				}
			}else{ // 3. parent和brother都为黑色
				if(!getLeft(brother).isRed && !getRight(brother).isRed){ 
					brother.isRed = true;
					fixRemove(parent);
					return;
				}else if(getRight(brother).isRed){
					getRight(brother).isRed = false;
				}else{
					getLeft(brother).isRed = false;
					rotateRight(brother);
				}
			}
			//最后一步的调整都是parent左旋
			rotateLeft(parent);
		}else{ // 对称情形
			parent.right = nil;
			RBNode<K, V> brother = getLeft(parent);   
			if(parent.isRed){ 
				if(getRight(brother).isRed){ 
					parent.isRed = false;
					rotateLeft(brother);
				}
			}else if(brother.isRed){    
				if(!getLeft(brother.right).isRed && !getRight(brother.right).isRed){
					brother.isRed = false;
					getRight(brother).isRed = true;
				}else if(getLeft(brother.right).isRed){
					getLeft(brother.right).isRed = false;
					rotateLeft(brother);
				}else{
					getRight(brother.right).isRed = false;
					rotateLeft(brother.right);
					rotateLeft(brother);
				}
			}else{ 
				if(!getLeft(brother).isRed && !getRight(brother).isRed){
					brother.isRed = true;
					fixRemove(parent);
					return;
				}else if(getLeft(brother).isRed){
					getLeft(brother).isRed = false;
				}else{
					getRight(brother).isRed = false;
					rotateLeft(brother);
				}
			}
			rotateRight(parent);
		}
	}

	static class RBNode<K, V> extends Node<K, V> {

		boolean isRed;

		public RBNode(K key, V value) {
			super(key, value);
		}

		RBNode(K key, V value, boolean isRepeat, Node<K, V> left, Node<K, V> right) {
			super(key, value, isRepeat, left, right);
		}

		@Override
		public String toString() {
			String str = super.toString();
			return isRed ? str + "(red)" : str;
		}
	}
}
