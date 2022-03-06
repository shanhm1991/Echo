package io.github.shanhm1991.echo.algorithm.sort;

import java.util.Comparator;

/**
 *
 * 选择排序/冒泡排序 N2
 *
 * @author shanhm1991@163.com
 *
 */
public class SelectSort extends Sort {

	@Override
	@SuppressWarnings("all")
	protected void sort(Object[] array, Comparator comp) {
		for (int i = 0; i < array.length; i++) {
			for (int j = i + 1; j < array.length; j++) {
				if (comp(array, i, j, comp) > 0) {
					swap(array, i, j);
				}
			}
		}
	}
}
