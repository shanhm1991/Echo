package io.github.echo.algorithms.sort;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author shanhm1991@163.com
 * 
 */
public class Sorts {

	public static final String ALGORITHM_SELECT = "SELECT";

	public static final String ALGORITHM_INSERT_SWAP = "INSERT_SWAP";

	public static final String ALGORITHM_INSERT_MOVE = "INSERT_MOVE";

	public static final String ALGORITHM_INSERT_XIER = "INSERT_XIER";

	public static final String ALGORITHM_MERGE = "MERGE";

	public static final String ALGORITHM_QUICK = "QUICK";

	private static final Logger LOGGER = LoggerFactory.getLogger(Sorts.class);

	private static int times_compare;

	private static int times_swap;

	public static <T extends Comparable<? super T>> void sort(List<T> list, String algorithm) {
		doSort(list, algorithm, null);
	}

	public static <T extends Comparable<? super T>> void sort(List<T> list, String algorithm, Comparator<? super T> comp) {
		doSort(list, algorithm, comp);
	}

	@SuppressWarnings("unchecked")
	private static <E> void doSort(List<E> list, String algorithm, Comparator<? super E> comp) {
		LOGGER.debug("before sort: {}", list); 

		Object[] array = list.toArray();
		switch(algorithm){
		case ALGORITHM_SELECT: sort_select(array, comp); break;
		case ALGORITHM_INSERT_SWAP: sort_insert_swap(array, comp); break;
		case ALGORITHM_INSERT_MOVE: sort_insert_move(array, comp); break;
		case ALGORITHM_INSERT_XIER: sort_xier(array, comp); break;
		case ALGORITHM_MERGE: sort_merge(array, comp); break;
		case ALGORITHM_QUICK: sort_quick(array, comp); break;
		}

		ListIterator<E> i = list.listIterator();
		for (Object e : array) {
			i.next();
			i.set((E) e);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static boolean comp(Object[] array, int a, int b, Comparator comp) {
		LOGGER.debug(String.format("%60s %2s comp：%2s<>%2s ([%2s]<>[%2s])", " ", ++times_compare, array[a], array[b], a, b)); 
		return (comp != null && comp.compare(array[a], array[b]) > 0)
				|| (comp == null && ((Comparable)array[a]).compareTo(array[b]) > 0);
	}

	private static void swap(Object[] array, int a, int b) {
		Object t = array[a];
		array[a] = array[b];
		array[b] = t;
		LOGGER.debug(String.format("%2s swap： %2s<>%2s ([%2s]<>[%2s]) = %s", ++times_swap, array[b], array[a], a, b, Arrays.toString(array))); 
	}

	private static void move(Object[] array, int a, int b) {
		Object t = array[b];
		System.arraycopy(array, a, array, a + 1, b - a);
		array[a] = t;
		LOGGER.debug(String.format("%2s move： %s([%s]->[%s]) = %s", ++times_swap, t, b, a, Arrays.toString(array)));
	}

	/**
	 * 冒泡排序/选择排序 N2
	 * 思路是一样的，都是将当前位置与其之后的所有位置进行比较，找到一个最小值
	 * @param array
	 * @param comp
	 */
	@SuppressWarnings("rawtypes")
	private static void sort_select(Object[] array, Comparator comp) {
		for(int i = 0; i < array.length; i++){
			for(int j = i + 1; j < array.length; j++){
				if(comp(array, i, j, comp)){
					swap(array, i ,j);
				}
			}
		}
	}

	/**
	 * 插入排序 N ~ N2
	 * @param array
	 * @param comp
	 */
	@SuppressWarnings("rawtypes")
	private static void sort_insert_swap(Object[] array, Comparator comp) {
		for(int i = 1; i < array.length; i++){
			for(int j = i; j > 0 && !comp(array, j, j - 1, comp); j--){
				swap(array, j-1 ,j);
			}
		}
	}

	/**
	 * 插入排序：通过move代替swap，可以降低操作次数
	 * @param array
	 * @param comp
	 */
	@SuppressWarnings("rawtypes")
	private static void sort_insert_move(Object[] array, Comparator comp) {
		for(int i = 1; i < array.length; i++){
			int index = -1;
			for(int j = i; j > 0 && comp(array, j - 1, i, comp); j--){
				index = j - 1;
			}
			if(index != -1){
				move(array, index ,i);
			}
		}
	}

	/**
	 * 希尔排序
	 * @param array
	 * @param comp
	 */
	@SuppressWarnings("rawtypes")
	private static void sort_xier(Object[] array, Comparator comp) {
		int h = 1;
		while(h < array.length / 3){
			h = h * 3 + 1;
		}

		while(h >= 1){
			LOGGER.debug("compare interval: {}", h); 
			for(int i = h; i < array.length; i++){
				for(int j = i; j >= h && comp(array, j - h, j, comp); j -= h){
					swap(array, j-h ,j);
				}
			}
			h = h / 3;
		}
	}

	/**
	 * 归并排序 NlogN
	 * @param array
	 * @param comp
	 */
	@SuppressWarnings("rawtypes")
	private static void sort_merge(Object[] array, Comparator comp) {
		Object[] temp = array.clone();
		recursion_mid(array, temp, 0, array.length - 1, comp);
	}

	@SuppressWarnings({ "rawtypes" })
	private static void recursion_mid(Object[] array, Object[] temp, int low, int high, Comparator comp) {
		if(low >= high){
			return;
		}

		int mid = (low + high) / 2;
		LOGGER.debug("mid={}", mid); 

		recursion_mid(array, temp, low, mid, comp);
		recursion_mid(array, temp, mid + 1, high, comp);
		merge(array, temp, low, mid, high, comp);
	}

	@SuppressWarnings("rawtypes")
	private static void merge(Object[] array, Object[] temp, int low, int mid, int high, Comparator comp) {
		if(comp(array, mid + 1, mid, comp)){
			LOGGER.debug("mid={} merge skipped...", mid); 
			return;
		}

		int i = low, j = mid + 1;
		System.arraycopy(array, low, temp, low, high - low + 1);
		for(int k = low; k <= high; k++){
			if(i > mid){
				array[k] = temp[j++];
			}else if(j > high){
				array[k] = temp[i++];
			}else if(comp(temp, i, j, comp)){
				array[k] = temp[j++];
			}else{
				array[k] = temp[i++];
			}
		}
		LOGGER.debug("mid={} merge {}", mid, Arrays.toString(array)); 
	}

	@SuppressWarnings("rawtypes")
	private static void sort_quick(Object[] array, Comparator comp) {
		recursion_partition(array, 0, array.length - 1, comp);
	}

	@SuppressWarnings("rawtypes")
	private static void recursion_partition(Object[] array, int low, int high, Comparator comp) {
		if(low >= high){
			return; 
		}
		
		int partition = partition(array, low, high, comp);
		LOGGER.debug(" partition={}", partition);
		
		recursion_partition(array, low, partition - 1, comp);
		recursion_partition(array, partition + 1, high, comp);
	}

	@SuppressWarnings("rawtypes")
	private static int partition(Object[] array, int low, int high, Comparator comp) {
		int i = low, j = high + 1; // 左右扫描指针
		while(true){ 
			while(++i < high && comp(array, low, i, comp)){}
			while(--j > low && !comp(array, low, j, comp)){}
			if(i >= j){
				break;
			}

			swap(array, i, j);
		}

		if(low != j){
			swap(array, low, j); // 将切分元素放入正确的位置 
		}
		return j;
	}
}
