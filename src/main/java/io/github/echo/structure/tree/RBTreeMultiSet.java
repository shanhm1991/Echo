package io.github.echo.structure.tree;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * 
 * @author shanhm1991@163.com
 *
 * @param <E>
 */
public class RBTreeMultiSet<E extends Comparable<? super E>> implements Set<E>{

	private static final Object V = new Object();

	private RBTreeMultiMap<E, Object> map;

	public RBTreeMultiSet(){
		map = new RBTreeMultiMap<>();
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return map.containsKey(o);
	}

	@Override
	public Iterator<E> iterator() {
		return map.keySet().iterator();
	}
	
	public Iterator<E> iterator(E from, boolean fromInclusive, E to, boolean toInclusive) {
		return map.keySet(from, fromInclusive, to, toInclusive, false).iterator();
	}

	@Override
	public boolean add(E e) {
		map.put(e, V);
		return true;
	}

	@Override
	public boolean remove(Object o) {
		map.removeList(o);
		return true;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return map.keySet().containsAll(c);
	}
	
	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public boolean addAll(Collection<? extends E> collection) {
		for(E e : collection){
			map.put(e, V);
		}
		return true;
	}
	
	@Override
	public boolean removeAll(Collection<?> collection) {
		for(Object o : collection){
			map.removeList(o);
		}
		return true;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
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
}
