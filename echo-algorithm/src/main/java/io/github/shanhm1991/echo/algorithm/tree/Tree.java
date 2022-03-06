package io.github.shanhm1991.echo.algorithm.tree;

import java.util.List;

/**
 *
 * Tree接口
 *
 * @author shanhm1991@163.com
 * @param <E>
 *
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
