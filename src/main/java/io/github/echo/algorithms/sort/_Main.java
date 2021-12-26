package io.github.echo.algorithms.sort;

import java.util.ArrayList;
import java.util.List;

/**
* 
* @author shanhm1991@163.com
* 
*/
public class _Main {

	public static void main(String[] args) {
		List<Integer> list = new ArrayList<>();
		
		// 乱序
		list.add(5);
		list.add(2);
		list.add(10);
		list.add(7);
		list.add(6);
		list.add(3);
		list.add(8);
		list.add(4);
		
		// 正序
//		list.add(1);
//		list.add(2);
//		list.add(3);
//		list.add(5);
//		list.add(6);
//		list.add(9);
//		list.add(10);
		
		// 反序
//		list.add(10);
//		list.add(9);
//		list.add(6);
//		list.add(5);
//		list.add(3);
//		list.add(2);
//		list.add(1);
		
		Sorts.sort(list, Sorts.ALGORITHM_QUICK2);
	}
}
