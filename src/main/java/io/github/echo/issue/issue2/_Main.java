package io.github.echo.issue.issue2;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import io.github.echo.algorithms.TopHeap;

/**
 * 
 * <p>问题：
 * <br>有大约40万个数字(数字范围：0～200000000000000），数字没有重复，求这些数字中，最大的100个数字之和。
 * 
 * <p>实现：
 * <br>思路一：通过插入排序，不过可以使用二分法改进下；
 * <br>思路二：通过堆排序，具体通过小顶堆，这样如果后续插入的值就会将较小值挤出堆；
 * <br>思路三：通过快速排序，每次排队一个元素都会将数组分为左右两部分，左边小于，右边大于，于是递归对右边子数组进行排序，直至又子数组元素个数k小于等于100，
 * 如果小于，则同样的思路再从左子数组选100-k个最大值
 * 
 * <p>对比：如果数据是一次性给定，那么可以通过快排是比较快的；如果数据是持续性输入的，那么通过堆排序或者二分法插入排序比较合适
 * 
 * @author shanhm1991
 *
 */
public class _Main {

	private static int K = 100;

	public static void main(String[] args) {
		SecureRandom random = new SecureRandom();  
		Long[] array = new Long[400000];
		for(int i = 0;i < 400000;i++){
			array[i] = random.nextLong() % 200000000000000L;
		}

		top1(array);
		comp_times = 0;
		
		top2(array);
		comp_times = 0;
		
		top3(array);
	}

	private static int comp_times = 0;

	private static void top1(Long[] array){
		long stime = System.currentTimeMillis();
		long[] result = new long[K];
		int size = 0;
		for(int i = 0; i < array.length; i++){
			binaryInsert(result, array[i], size);
			if(++size >= K){
				size = K;
			}
		}

		System.out.println(System.currentTimeMillis() - stime + "ms,  " + comp_times);
		System.out.println(Arrays.toString(result)); 
	}

	private static void binaryInsert(long[] result, long e, int size){
		// 空数组，直接插入
		if(size == 0){
			result[0] = e;
			return;
		}

		//大于等于头节点，直接插入到头节点
		if(comp(e, result[0]) >= 0){  
			System.arraycopy(result, 0, result, 1, size - 1);
			result[0] = e;
			return;
		}

		//小于等于尾节点，如果数组还没满则直接插入队尾，否则直接丢弃  
		if(comp(e, result[size - 1]) <= 0){ 
			if(size < K){
				result[size] = e;
			}
			return;
		}

		// 二分法从中间插入
		int middle = 0, left = 0, right = size - 1;
		middle = (left + right) / 2;
		while(middle > left && middle < right){
			if(comp(e, result[middle]) < 0){ 
				left = middle; 
			}else if(comp(e, result[middle]) >= 0){
				right = middle;
			}
			middle = (left + right) / 2;
		}

		int move = K - (middle + 1) -1;
		if(move > 0){
			System.arraycopy(result, middle + 1, result, middle + 2, move);
			result[middle + 1] = e;
		}
	}

	private static long comp(long o1, long o2){
		comp_times++;
		return o1 - o2;
	}

	private static void top2(Long[] array){
		TopHeap<Long> queue = new TopHeap<>(100, new Comparator<Long>(){
			@Override
			public int compare(Long o1, Long o2) {
				return o2.compareTo(o1); // 这里不要用 o2 - o1，存在溢出问题
			}
		});

		long stime = System.currentTimeMillis();
		for(int i = 0; i < array.length; i++){
			queue.put(array[i]);
		}

		System.out.println(System.currentTimeMillis() - stime + "ms,  " + queue.getCompTimes());
		System.out.println(queue.getTop());
	}

	private static void top3(Long[] array){
		long stime = System.currentTimeMillis();
		recursion_partition(array, 0, array.length - 1, null);
		System.out.println(System.currentTimeMillis() - stime + "ms,  " + comp_times);

		List<Long> list = new ArrayList<>();
		for(int i = 0; i < K; i++){
			if(array[i] == null){
				break;
			}
			list.add(array[i]);
		}
		list.sort(new Comparator<Long>(){
			@Override
			public int compare(Long o1, Long o2) {
				return o2.compareTo(o1); 
			}
		}); 
		System.out.println(list);
	}

	@SuppressWarnings("rawtypes")
	private static void recursion_partition(Object[] array, int low, int high, Comparator comp) {
		int partition = partition(array, low, high, comp);
		if(K < partition){
			recursion_partition(array, low, partition - 1, comp);
		}else if(K  > partition){
			recursion_partition(array, partition + 1, high, comp);
		}else{
			return;
		}
	}

	@SuppressWarnings("rawtypes")
	private static int partition(Object[] array, int low, int high, Comparator comp) {
		int i = low, j = high + 1; // 左右扫描指针
		while(true){ 
			while(++i < high && comp(array, low, i, comp) <= 0){}
			while(--j > low && comp(array, low, j, comp) > 0){}
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

	@SuppressWarnings({ "unchecked", "rawtypes" }) 
	private static int comp(Object[] array, int a, int b, Comparator comp) {
		comp_times++;
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
	}
}
