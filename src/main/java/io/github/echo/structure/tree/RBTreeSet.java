package io.github.echo.structure.tree;

/**
 * 
 * @author shanhm1991@163.com
 *
 * @param <E>
 */
public class RBTreeSet<E extends Comparable<? super E>> extends BinaryTreeSet<E> {

	private RBNode<E> header;

	private RBNode<E> nil;

	public RBTreeSet() {
		nil = new RBNode<>(null);
		nil.left = nil;
		nil.right = nil;
		header = new RBNode<>(null, nil, nil);
	}

	@Override
	protected Node<E> getRoot() {
		return header.right;
	}

	@Override
	protected boolean isEmptyNode(Node<E> node) {
		return nil == node;
	}
	
	@Override
	public void clear() {
		header.right = nil;
		size = 0;
	}

	@Override
	public boolean add(E e) {
		RBNode<E> current = header;
		RBNode<E> parent = header;
		//先将nil的值设为e
		nil.element = e;
		//然后从header开始自上而下寻找值为e的节点a
		while(compare(e, current) != 0){
			parent = current;
			if(compare(e, current) < 0){
				current = getLeft(current);
			}else{
				current = getRight(current);
			}
			//如果左右子节点都为红色，则进行颜色翻转，避免插入时出现父亲的兄弟节点为红色的情况
			if(getLeft(current).red && getRight(current).red){
				current = handleReorient(current);
			}
		}

		//如果发现了值相同的重复节点，直接覆盖
		if(current != nil){
			current.element = e;
			return true;
		}

		//新建一个数据节点代替nil，并将其左右子树指向nil
		current = new RBNode<>(e, nil, nil);
		size++;

		//当while结束时，必然可以明确待插入节点的parent节点，这里使parent节点链接到新节点
		current.parent = parent;
		if(compare(e, parent) < 0){
			parent.left = current;
		}else{
			parent.right = current;
		}

		//将新数据节点设成红色，如果父亲节点为红色则需要旋转调整
		handleReorient(current);
		return true;
	}

	private RBNode<E> getLeft(Node<E> node){
		return (RBNode<E>)node.left;
	}

	private RBNode<E> getRight(Node<E> node){
		return (RBNode<E>)node.right;
	}

	private int compare(E e, Node<E> node) {
		if(node == header){
			return 1;
		}else{
			return e.compareTo(node.element);
		}
	}

	private RBNode<E> handleReorient(RBNode<E> current) {
		getLeft(current).red = false;  
		getRight(current).red = false; 
		current.red = true;

		RBNode<E> subRoot = current;
		//翻转之后发现parent也是红色，进行换色和旋转
		RBNode<E> parent = current.parent;
		if(parent.red){
			RBNode<E> grand = parent.parent;
			grand.red = true;
			//旋转parent, 向外旋转到同一侧 
			if(compare(current.element, grand) != compare(current.element, parent)) {
				if(compare(current.element, parent) > 0){
					rotateLeft(parent);
				}else{
					rotateRight(parent);
				}
			}
			//旋转grand
			if(compare(current.element, grand) < 0){
				subRoot = getLeft(grand);
				subRoot.red = false;
				rotateRight(grand);
			}else{
				subRoot = getRight(grand);
				subRoot.red = false;
				rotateLeft(grand);
			}
		}
		//直接将根节点置为黑色 
		getRight(header).red = false;
		return subRoot;
	}

	private void rotateRight(RBNode<E> node) {
		RBNode<E> parent = node.parent;
		RBNode<E> left = getLeft(node); 
		RBNode<E> leftRight = getRight(left);
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

	private void rotateLeft(RBNode<E> node) {
		RBNode<E> parent = node.parent;
		RBNode<E> right = getRight(node); 
		RBNode<E> rightLeft = getLeft(right);
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

	@Override
	public boolean remove(Object o) {
		RBNode<E> current = header;
		RBNode<E> parent = header;
		
		@SuppressWarnings("unchecked")
		E e = (E)o;
		nil.element = e;
		while(compare(e, current) != 0){
			parent = current;
			if(compare(e, current) < 0){
				current = getLeft(current);
			}else{
				current = getRight(current);
			}
		}

		if(current == nil){ //没有找到值为e的数据节点
			return true;
		}

		size--;
		RBNode<E> node = current;
		if(current.right != nil){ //替换右子树最小节点
			parent = current;
			current = getRight(current);
			while(current.left != nil){
				parent = current;
				current = getLeft(current);
			}
			node.element = current.element;
			//右侧最小节点不是叶子，则继续向下遍历一层，这时可以断定树叶是红色
			if(current.right != nil){
				parent = current;
				current = getRight(current);
				parent.element = current.element;
				parent.right = nil;
				return true;
			}
		}else if(current.left != nil){ //替换左子树最大节点
			parent = current;
			current = getLeft(node);
			while(current.right != nil){
				parent = current;
				current = getRight(current);
			}
			node.element = current.element;
			//左侧最大节点不是叶子，则继续向下遍历一层，这时可以断定树叶是红色
			if(current.left != nil){
				parent = current;
				current = getLeft(current);
				parent.element = current.element;
				parent.left = nil;
				return true;
			}
		}

		//先调整再删除，因为后面还需要依赖current与parent的位置关系判断
		if(!current.red){
			fixRemove(current);
		}
		
		//删除树叶leaf
		if(current == parent.left){
			parent.left = nil;
		}else{
			parent.right = nil;
		}
		return true;
	}

	private void fixRemove(RBNode<E> current){
		RBNode<E> parent = current.parent;
		if(parent == header){
			return;
		}

		if(current == parent.left){
			RBNode<E> brother = getRight(parent);
			if(parent.red){ // 1.parent为红色
				if(getLeft(brother).red){ 
					parent.red = false;
					rotateRight(brother);
				}
			}else if(brother.red){ // 2.brother为红色
				if(!getLeft(brother.left).red && !getRight(brother.left).red){
					brother.red = false;
					getLeft(brother).red = true;
				}else if(getRight(brother.left).red){
					getRight(brother.left).red = false;
					rotateRight(brother);
				}else{
					getLeft(brother.left).red = false;
					rotateRight(getLeft(brother));
					rotateRight(brother);
				}
			}else{ // 3. parent和brother都为黑色
				if(!getLeft(brother).red && !getRight(brother).red){ 
					brother.red = true;
					fixRemove(parent);
					return;
				}else if(getRight(brother).red){
					getRight(brother).red = false;
				}else{
					getLeft(brother).red = false;
					rotateRight(brother);
				}
			}
			//最后一步的调整都是parent左旋
			rotateLeft(parent);
		}else{ // 对称情形
			RBNode<E> brother = getLeft(parent);   
			if(parent.red){ 
				if(getRight(brother).red){ 
					parent.red = false;
					rotateLeft(brother);
				}
			}else if(brother.red){    
				if(!getLeft(brother.right).red && !getRight(brother.right).red){
					brother.red = false;
					getRight(brother).red = true;
				}else if(getLeft(brother.right).red){
					getLeft(brother.right).red = false;
					rotateLeft(brother);
				}else{
					getRight(brother.right).red = false;
					rotateLeft(getRight(brother));
					rotateLeft(brother);
				}
			}else{ 
				if(!getLeft(brother).red && !getRight(brother).red){
					brother.red = true;
					fixRemove(parent);
					return;
				}else if(getLeft(brother).red){
					getLeft(brother).red = false;
				}else{
					getRight(brother).red = false;
					rotateLeft(brother);
				}
			}
			rotateRight(parent);
		}
	}

	static class RBNode<E> extends Node<E> {

		RBNode<E> parent;

		boolean red;

		RBNode(E e) {
			super(e);
		}

		RBNode(E e, RBNode<E> left, RBNode<E> right) {
			super(e);
			this.left = left;
			this.right = right;
		}

		@Override
		public String toString() {
			return red ? element.toString() + "(red)" : element.toString();
		}
	}
}
