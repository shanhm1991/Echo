package io.github.echo.algorithms;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 
 * @author shanhm1991@163.com
 * 
 */
public class _Main {

	public static void main(String[] args) {
		test1();
	}

	public static void test1(){
		List<Integer> list = new ArrayList<>(1100);
		list.add(5);
		list.add(2);
		list.add(3);
		list.add(10);
		list.add(7);
		list.add(5);
		list.add(6);
		list.add(3);
		list.add(5);
		list.add(8);
		list.add(3);
		list.add(4);
		
		
		SecureRandom random = new SecureRandom();  
		for(int i = 0;i < 1000;i++){
			list.add(random.nextInt(988888));
		}
		
		long stime1 = System.currentTimeMillis();
		List<Integer> temp1 = new ArrayList<>();
		temp1.addAll(list);
		Sorts.sort(temp1, Sorts.ALGORITHM_SELECT); 
		System.out.println(Sorts.ALGORITHM_SELECT
				+ "  comp=" + Sorts.times_compare + "  swap=" + Sorts.times_swap + "  cost=" + (System.currentTimeMillis() - stime1) + "ms"); 

		long stime2 = System.currentTimeMillis();
		Sorts.times_compare = 0;
		Sorts.times_swap = 0;
		List<Integer> temp2 = new ArrayList<>();
		temp2.addAll(list);
		Sorts.sort(temp2, Sorts.ALGORITHM_INSERT);
		System.out.println(Sorts.ALGORITHM_INSERT
				+ "  comp=" + Sorts.times_compare + "  swap=" + Sorts.times_swap + "  cost=" + (System.currentTimeMillis() - stime2) + "ms"); 
		
		long stime3 = System.currentTimeMillis();
		Sorts.times_compare = 0;
		Sorts.times_swap = 0;
		List<Integer> temp7 = new ArrayList<>();
		temp7.addAll(list);
		Sorts.sort(temp7, Sorts.ALGORITHM_INSERT_BINARY);
		System.out.println(Sorts.ALGORITHM_INSERT_BINARY
				+ "  comp=" + Sorts.times_compare + "  move=" + Sorts.times_swap + "  cost=" + (System.currentTimeMillis() - stime3) + "ms"); 
		
		long stime4 = System.currentTimeMillis();
		Sorts.times_compare = 0;
		Sorts.times_swap = 0;
		List<Integer> temp3 = new ArrayList<>();
		temp3.addAll(list);
		Sorts.sort(temp3, Sorts.ALGORITHM_INSERT_XIER);
		System.out.println(Sorts.ALGORITHM_INSERT_XIER
				+ "  comp=" + Sorts.times_compare + "  swap=" + Sorts.times_swap + "  cost=" + (System.currentTimeMillis() - stime4) + "ms"); 
		
		long stime5 = System.currentTimeMillis();
		Sorts.times_compare = 0;
		Sorts.times_swap = 0;
		List<Integer> temp4 = new ArrayList<>();
		temp4.addAll(list);
		Sorts.sort(temp4, Sorts.ALGORITHM_MERGE);
		System.out.println(Sorts.ALGORITHM_MERGE
				+ "  comp=" + Sorts.times_compare + "  swap=" + Sorts.times_swap + "  cost=" + (System.currentTimeMillis() - stime5) + "ms"); 
		
		long stime6 = System.currentTimeMillis();
		Sorts.times_compare = 0;
		Sorts.times_swap = 0;
		List<Integer> temp5 = new ArrayList<>();
		temp5.addAll(list);
		Sorts.sort(temp5, Sorts.ALGORITHM_QUICK);
		System.out.println(Sorts.ALGORITHM_QUICK
				+ "  comp=" + Sorts.times_compare + "  swap=" + Sorts.times_swap + "  cost=" + (System.currentTimeMillis() - stime6) + "ms"); 
		
		long stime7 = System.currentTimeMillis();
		Sorts.times_compare = 0;
		Sorts.times_swap = 0;
		List<Integer> temp6 = new ArrayList<>();
		temp6.addAll(list);
		Sorts.sort(temp6, Sorts.ALGORITHM_QUICK2);
		System.out.println(Sorts.ALGORITHM_QUICK2
				+ "  comp=" + Sorts.times_compare + "  swap=" + Sorts.times_swap + "  cost=" + (System.currentTimeMillis() - stime7) + "ms"); 
	}

	public static void test2(){
		SecureRandom random = new SecureRandom();  
		List<Long> array = new ArrayList<>();
		for(int i = 0;i < 40;i++){
			array.add(random.nextLong() % 200000000000000L);  
		}

		TopHeap<Long> heap = new TopHeap<>(10, new Comparator<Long>(){
			@Override
			public int compare(Long o1, Long o2) {
				if(o2 > o1){
					return 1;
				}else if(o1 > o2){
					return -1;
				}else{
					return 0;
				}
			}
		});

		for(int i = 0; i < 40; i++){
			heap.put(array.get(i));
		}
		System.out.println(heap.getTop());
	}

}
