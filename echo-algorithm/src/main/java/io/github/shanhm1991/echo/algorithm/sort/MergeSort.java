package io.github.shanhm1991.echo.algorithm.sort;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Comparator;

/**
 *
 * 归并排序 NlogN
 *
 * @author shanhm1991@163.com
 *
 */
@Slf4j
public class MergeSort extends Sort {

	@Override
	@SuppressWarnings("all")
	protected void sort(Object[] array, Comparator comp) {
		Object[] temp = array.clone();
		recursion_mid(array, temp, 0, array.length - 1, comp);
	}

	@SuppressWarnings("all")
	private void recursion_mid(Object[] array, Object[] temp, int low, int high, Comparator comp) {
		if (low >= high) {
			return;
		}

		int mid = (low + high) / 2;
		log.debug("mid={}", mid);

		recursion_mid(array, temp, low, mid, comp);
		recursion_mid(array, temp, mid + 1, high, comp);
		merge(array, temp, low, mid, high, comp);
	}

	@SuppressWarnings("all")
	private void merge(Object[] array, Object[] temp, int low, int mid, int high, Comparator comp) {
		if (comp(array, mid + 1, mid, comp) >= 0) {
			log.debug("mid={} merge skipped...", mid);
			return;
		}

		int i = low, j = mid + 1;
		System.arraycopy(array, low, temp, low, high - low + 1);
		for (int k = low; k <= high; k++) {
			if (i > mid) {
				array[k] = temp[j++];
			} else if (j > high) {
				array[k] = temp[i++];
			} else if (comp(temp, i, j, comp) > 0) {
				array[k] = temp[j++];
			} else {
				array[k] = temp[i++];
			}
		}
		log.debug("mid={} merge {}", mid, Arrays.toString(array));
	}
}
