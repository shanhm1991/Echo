package io.github.echo.algorithms;

import java.util.Comparator;
import java.util.List;

/**
 * 
 * @author shanhm1991@163.com
 * 
 */
public class LimitPriorityQueue<T extends Comparable<? super T>> {

	private final Comparator<? super T> comp; // 比较器

	private final Object[] array; // 元素数组

	private int head; // head 左边为无效数据，take操作会导致head右移一位
	
	private int size; // 元素个数

	public LimitPriorityQueue(int capacity, Comparator<? super T> comp){
		this.comp = comp;
		this.array = new Object[capacity + 1]; 
	}

	public LimitPriorityQueue(int capacity, List<T> list, Comparator<? super T> comp){
		this.comp = comp;
		this.array = new Object[capacity + 1];

		Object[] arr = list.toArray(); 
		reset(arr, arr.length, 0);
		size = capacity > list.size() ? list.size() : capacity;
		System.arraycopy(arr, 0, array, 0, size);
	}

	private void reset(Object[] array, int length, int head) {
		for (int i = length / 2; i >= 0; i--) { // 从倒数第二层开始想上，逐个与子节点进行比较
			fix(array, i, length, head);
		}
	}

	private void fix(Object[] array, int parent, int length, int head) {
		int child; 
		while ((child = 2 * parent + 1)  < length) {
			if (child + 1 < length && comp(array, child + head, child + head + 1, comp) < 0) {
				child++;    // 从左右子节点中选取一个较大值
			}
			if (comp(array, parent + head, child + head, comp) > 0){ 
				break;      // 如果父结点已经不小于左右子节点，则直接结束
			}

			swap(array, parent + head, child + head); // 父节点下沉，子节点上浮
			parent = child;             // 继续向下比较  
		}
	}

	@SuppressWarnings("unchecked")
	public T get(){
		return (T)(array[0]);
	}
	
	public int size(){
		return size;
	}
	
	@SuppressWarnings("unchecked")
	public T take(){
		if(size == 0){
			return null;
		}

		T t = (T)(array[head]);
		head++;
		size--;
		if(size == 0){
			head = 0;
		}else{
			reset(array, size, head);
		}
		return t;
	}

	public void put(T t){
		if(head == 0){
			array[size] = t;
			reset(array, size + 1, head);
			if(size < array.length - 1){
				size++;
			}
		}else{
			array[--head] = t;
			size++;
			reset(array, size, head);
		}
	}
	
	public void print(){
		System.out.println();
		for(int i = head; i < head + size; i++){
			System.out.print(array[i] + ", "); 
		}
		System.out.println();
	}

	private void swap(Object[] array, int a, int b) {
		Object t = array[a];
		array[a] = array[b];
		array[b] = t;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private int comp(Object[] array, int a, int b, Comparator comp) {
		if(comp != null){
			return comp.compare(array[a], array[b]);
		}else{
			return ((Comparable)array[a]).compareTo(array[b]);
		}
	}
}
