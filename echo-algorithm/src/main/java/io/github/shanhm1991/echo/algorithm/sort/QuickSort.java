package io.github.shanhm1991.echo.algorithm.sort;

import java.util.Comparator;

/**
 *
 * 快速排序
 *
 * @author shanhm1991@163.com
 *
 */
public class QuickSort extends Sort {

	@Override
	@SuppressWarnings("all")
	protected void sort(Object[] array, Comparator comp) {
		recursion_partition(array, 0, array.length - 1, comp);
	}

	@SuppressWarnings("all")
	private void recursion_partition(Object[] array, int low, int high, Comparator comp) {
		if (low >= high) {
			return;
		}

		int partition = partition(array, low, high, comp);

		recursion_partition(array, low, partition - 1, comp);
		recursion_partition(array, partition + 1, high, comp);
	}

	@SuppressWarnings("all")
	private int partition(Object[] array, int low, int high, Comparator comp) {
		int i = low, j = high + 1; // 左右扫描指针
		while (true) {
			while (++i < high && comp(array, low, i, comp) > 0) {
			}
			while (--j > low && comp(array, low, j, comp) < 0) {
			}
			if (i >= j) {
				break;
			}

			swap(array, i, j);
		}

		if (low != j) {
			swap(array, low, j); // 将切分元素放入正确的位置
		}
		LOGGER.debug(" paitition by {}[{}]", array[j], j);
		return j;
	}
}
