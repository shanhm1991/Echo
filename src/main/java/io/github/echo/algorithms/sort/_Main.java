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
		list.add(5);
		list.add(2);
		list.add(10);
		list.add(9);
		list.add(3);
		list.add(6);
		list.add(1);
		
		Lists.sort(list);
		System.out.println(list); 
	}
}
