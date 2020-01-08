package org.eto.essay.datastructure.tree.b;

import java.util.List;

/**
 * 
 * @author shanhm1991
 *
 * @param <E>
 */
public interface Tree<E> {

	boolean isEmpty();
	
	int size();
	
	void clear();

	E getMin();

	E getMax();

	boolean contains(E e);

	void add(E e);

	void remove(E e);
	
	List<E> toList();
	
	void printTree();
}
