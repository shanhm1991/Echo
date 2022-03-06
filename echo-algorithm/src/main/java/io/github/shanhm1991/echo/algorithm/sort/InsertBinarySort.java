package io.github.shanhm1991.echo.algorithm.sort;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Comparator;

/**
 *
 * 插入排序：用二分法改进下
 *
 * @author shanhm1991@163.com
 *
 */
@Slf4j
public class InsertBinarySort extends Sort {

	@Override
	@SuppressWarnings("all")
	protected void sort(Object[] array, Comparator comp) {
		if (array.length < 2) {
			return;
		} else if (comp(array, 0, 1, comp) > 0) {
			swap(array, 0, 1);
		}

		if (array.length > 2) {
			for (int i = 2; i < array.length; i++) {
				int mid = mid(array, i, comp);
				if (comp(array, i, mid, comp) > 0) {
					move(array, mid + 1, i);
				} else {
					move(array, mid, i);
				}
			}
		}
	}

	@SuppressWarnings("all")
	private int mid(Object[] array, int target, Comparator comp) {
		int left = 0;
		int right = target;
		int mid = (left + right) / 2;
		while (mid > left && mid < right) {
			if (comp(array, target, mid, comp) < 0) {
				right = mid;
			} else if (comp(array, target, mid, comp) > 0) {
				left = mid;
			} else {
				return mid; // 等于位置直接可以原地插入
			}
			mid = (left + right) / 2;
		}
		return mid;
	}

	private void move(Object[] array, int mid, int target) {
		Object t = array[target];
		System.arraycopy(array, mid, array, mid + 1, target - mid);
		array[mid] = t;
		log.debug(String.format("%2s move： %s([%s]->[%s]) = %s", ++swapTimes, t, target, mid, Arrays.toString(array)));
	}
}
