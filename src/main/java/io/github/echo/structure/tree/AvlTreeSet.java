package io.github.echo.structure.tree;

/**
 * 
 * @author shanhm1991@163.com
 *
 * @param <E>
 */
public class AvlTreeSet<E extends Comparable<? super E>> extends BinaryTreeSet<E> {

	private static final int ALLOWED_IMBALANCE = 1;

	private AvlNode<E> root;
	
	@Override
	protected AvlNode<E> getRoot() {
		return root;
	}
	
	@Override
	public void clear() {
		root = null;
		size = 0;
	}
	
	@Override
	public boolean add(E o){
		root = add(o, root);
		return true;
	}

	private AvlNode<E> add(E e, AvlNode<E> node) {
		if(node == null){
			size++;
			return new AvlNode<>(e);
		}
		int compare = e.compareTo(node.element);
		if(compare < 0){
			node.left = add(e, (AvlNode<E>)node.left);
		}else if(compare > 0){
			node.right = add(e, (AvlNode<E>)node.right);
		}else{
			node.element = e;
		}
		return balance(node);
	}
	
	private AvlNode<E> balance(AvlNode<E> node) {
		if(node == null){
			return node;
		}
		
		if(getHeight(node.left) - getHeight(node.right) > ALLOWED_IMBALANCE){
			if(getHeight(node.left.left) >= getHeight(node.left.right)){
				node = rotateRight(node);
			}else{
				node = rotateLeftRight(node);
			}
		}else if(getHeight(node.right) - getHeight(node.left) > ALLOWED_IMBALANCE){
			if(getHeight(node.right.right) >= getHeight(node.right.left)){
				node = rotateLeft(node);
			}else{
				node = rotateRightLeft(node);
			}
		}
		node.height = Math.max(getHeight(node.left), getHeight(node.right)) + 1;
		return node;
	} 

	private AvlNode<E> rotateRight(AvlNode<E> node) {
		AvlNode<E> leftNode = (AvlNode<E>)node.left;
		node.left = leftNode.right;
		leftNode.right = node;
		
		node.height = Math.max(getHeight(node.left), getHeight(node.right)) + 1;
		leftNode.height = Math.max(getHeight(leftNode.left), node.height) + 1;
		return leftNode;
	}
	
	private AvlNode<E> rotateLeft(AvlNode<E> node) {
		AvlNode<E> rightNode = (AvlNode<E>)node.right;
		node.right = rightNode.left;
		rightNode.left = node;
		
		node.height = Math.max(getHeight(node.left), getHeight(node.right)) + 1;
		rightNode.height = Math.max(getHeight(rightNode.right), node.height) + 1;
		return rightNode;
	}

	private AvlNode<E> rotateLeftRight(AvlNode<E> node) {
		node.left = rotateLeft((AvlNode<E>)node.left); 
		return rotateRight(node);
	}
	
	private AvlNode<E> rotateRightLeft(AvlNode<E> node) {
		node.right = rotateRight((AvlNode<E>)node.right);
		return rotateLeft(node);
	}

	private int getHeight(Node<E> node) {
		AvlNode<E> avlNode = (AvlNode<E>)node;
		return avlNode == null ? -1 : avlNode.height;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o){
		root = remove((E)o, root);
		return true;
	}
	
	private AvlNode<E> remove(E e, AvlNode<E> node) {
		if(node == null) {
			return node;
		}
		int compare = e.compareTo(node.element);
		if(compare < 0){
			node.left = remove(e, (AvlNode<E>)node.left);
		}else if(compare > 0){
			node.right = remove(e, (AvlNode<E>)node.right);
		}else if(node.left != null && node.right != null){
			node = (AvlNode<E>)removeLeftMax(node);
			size--;
		}else{
			node = (node.left != null) ? (AvlNode<E>)node.left : (AvlNode<E>)node.right;
			size--;
		}
		return balance(node);
	}

	static class AvlNode<E> extends Node<E> {

		int height = 0;

		AvlNode(E e) {
			super(e);
		}
		
		@Override
		public String toString() {
			return super.toString();
		}
	}
}
