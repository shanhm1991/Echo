package io.github.shanhm1991.echo.algorithm.sort;

import java.util.Comparator;

/**
 *
 * 希尔排序
 *
 * @author shanhm1991@163.com
 *
 */
public class InsertXierSort extends Sort {

	@SuppressWarnings("all")
	@Override
	protected void sort(Object[] array, Comparator comp) {
		int h = 1;
		while (h < array.length / 3) {
			h = h * 3 + 1;
		}

		while (h >= 1) {
			LOGGER.debug("compare interval: {}", h);
			for (int i = h; i < array.length; i++) {
				for (int j = i; j >= h && comp(array, j, j - h, comp) < 0; j -= h) {
					swap(array, j - h, j);
				}
			}
			h = h / 3;
		}
	}

}
