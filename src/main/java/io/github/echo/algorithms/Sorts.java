package io.github.echo.algorithms;

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

	public static final String ALGORITHM_INSERT = "INSERT";

	public static final String ALGORITHM_INSERT_BINARY = "INSERT_BINARY";

	public static final String ALGORITHM_INSERT_XIER = "INSERT_XIER";

	public static final String ALGORITHM_MERGE = "MERGE";

	public static final String ALGORITHM_QUICK = "QUICK";

	public static final String ALGORITHM_QUICK2 = "QUICK2";

	private static final Logger LOGGER = LoggerFactory.getLogger(Sorts.class);

	public static int times_compare;

	public static int times_swap;

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
		case ALGORITHM_SELECT: sort_select(array, comp); break;               // 选择排序（冒泡）
		case ALGORITHM_INSERT: sort_insert_swap(array, comp); break;          // 插入排序
		case ALGORITHM_INSERT_BINARY: sort_insert_binary(array, comp); break; // 插入排序（二分法改进）
		case ALGORITHM_INSERT_XIER: sort_xier(array, comp); break;            // 希尔排序
		case ALGORITHM_MERGE: sort_merge(array, comp); break;                 // 归并排序
		case ALGORITHM_QUICK: sort_quick(array, comp); break;                 // 快速排序
		case ALGORITHM_QUICK2: sort_quick2(array, comp); break;               // 快速排序（重复场景改进）
		}

		ListIterator<E> i = list.listIterator();
		for (Object e : array) {
			i.next();
			i.set((E) e);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static int comp(Object[] array, int a, int b, Comparator comp) {
		times_compare++;
		LOGGER.debug(String.format("%60s %2s comp：%2s<>%2s ([%2s]<>[%2s])", " ", times_compare, array[a], array[b], a, b)); 
		if(comp != null){
			return comp.compare(array[a], array[b]);
		}else{
			return ((Comparable)array[a]).compareTo(array[b]);
		}
	}

	private static void swap(Object[] array, int a, int b) {
		Object t = array[a];
		array[a] = array[b];
		array[b] = t;
		times_swap++;
		LOGGER.debug(String.format("%2s swap： %2s<>%2s ([%2s]<>[%2s]) = %s", times_swap, array[b], array[a], a, b, Arrays.toString(array))); 
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
				if(comp(array, i, j, comp) > 0){
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
			for(int j = i; j > 0 && comp(array, j, j - 1, comp) < 0; j--){
				swap(array, j-1 ,j);
			}
		}
	}

	/**
	 * 插入排序：通过二分法改进
	 * @param array
	 * @param comp
	 */
	@SuppressWarnings("rawtypes")
	private static void sort_insert_binary(Object[] array, Comparator comp) {
		if(array.length < 2){
			return;
		}else if(comp(array, 0, 1, comp) > 0){
			swap(array, 0 ,1);
		}
		
		if(array.length > 2){
			for(int i = 2; i < array.length; i++){
				int mid = mid(array, i, comp);
				if(comp(array, i, mid, comp) > 0){
					move(array, mid + 1, i);
				}else{
					move(array, mid, i);
				}
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	private static int mid(Object[] array, int target, Comparator comp){
		int left = 0;
		int right = target;
		int mid = (left + right) / 2;
		while(mid > left && mid < right){
			if(comp(array, target, mid, comp) < 0){ 
				right = mid; 
			}else if(comp(array, target, mid, comp) > 0){
				left = mid;
			}else{
				return mid; // 等于位置直接可以原地插入
			}
			mid = (left + right) / 2;
		}
		return mid;
	}
	
	private static void move(Object[] array, int mid, int target) {
		Object t = array[target];
		System.arraycopy(array, mid, array, mid + 1, target - mid);
		array[mid] = t;
		
		times_swap++;
		LOGGER.debug(String.format("%2s move： %s([%s]->[%s]) = %s", times_swap, t, target, mid, Arrays.toString(array)));
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
				for(int j = i; j >= h && comp(array, j, j - h, comp) < 0; j -= h){
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
		if(comp(array, mid + 1, mid, comp) >= 0){
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
			}else if(comp(temp, i, j, comp) > 0){
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

		recursion_partition(array, low, partition - 1, comp);
		recursion_partition(array, partition + 1, high, comp);
	}

	@SuppressWarnings("rawtypes")
	private static int partition(Object[] array, int low, int high, Comparator comp) {
		int i = low, j = high + 1; // 左右扫描指针
		while(true){ 
			while(++i < high && comp(array, low, i, comp) > 0){}
			while(--j > low && comp(array, low, j, comp) < 0){}
			if(i >= j){
				break;
			}

			swap(array, i, j);
		}

		if(low != j){
			swap(array, low, j); // 将切分元素放入正确的位置 
		}
		LOGGER.debug(" paitition by {}[{}]", array[j], j);
		return j;
	}

	@SuppressWarnings("rawtypes")
	private static void sort_quick2(Object[] array, Comparator comp) {
		recursion_partition2(array, 0, array.length - 1, comp);
	}

	@SuppressWarnings("rawtypes")
	private static void recursion_partition2(Object[] array, int low, int high, Comparator comp) {
		if(high <= low){
			return; 
		}

		int lt = low, i = low + 1, gt = high;
		while(i <= gt){
			int cmp = comp(array, i, lt, comp);
			if(cmp < 0){
				swap(array, lt++, i++);
			}else if(cmp > 0){
				swap(array, i, gt--);
			}else{
				i++;
			}
		}
		recursion_partition2(array, low, lt - 1, comp);
		recursion_partition2(array, gt + 1, high, comp);
	}
}
