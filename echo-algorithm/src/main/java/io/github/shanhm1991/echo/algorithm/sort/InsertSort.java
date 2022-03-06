package io.github.shanhm1991.echo.algorithm.sort;

import java.util.Comparator;

/**
 *
 * 插入排序 N ~ N2
 *
 * @author shanhm1991@163.com
 *
 */
public class InsertSort extends Sort {

	@Override
	@SuppressWarnings("all")
	protected void sort(Object[] array, Comparator comp) {
		for (int i = 1; i < array.length; i++) {
			for (int j = i; j > 0 && comp(array, j, j - 1, comp) < 0; j--) {
				swap(array, j - 1, j);
			}
		}
	}
}
