package io.github.echo.algorithms;

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
		test2();
	}

	public static void test1(){
		List<Integer> list = new ArrayList<>();
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
		Sorts.sort(list, Sorts.ALGORITHM_QUICK2);
	}

	public static void test2(){
		List<Integer> list = new ArrayList<>();
		list.add(5);
		list.add(2);
		list.add(10);
		list.add(7);
		list.add(6);
		list.add(3);
		list.add(8);
		list.add(4);

		LimitPriorityQueue<Integer> heap = new LimitPriorityQueue<Integer>(10, list, new Comparator<Integer>(){
			@Override
			public int compare(Integer o1, Integer o2) {
				return o2 - o1;
			}
		});
		heap.print();

		System.out.println(heap.take());
		heap.print();

		heap.put(1);
		heap.print();

		heap.put(0);
		heap.print();

		heap.put(4);
		heap.print();
		
		heap.put(2);
		heap.print();
	}
}
