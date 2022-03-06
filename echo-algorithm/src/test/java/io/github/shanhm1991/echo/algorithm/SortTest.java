package io.github.shanhm1991.echo.algorithm;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.shanhm1991.echo.algorithm.sort.InsertBinarySort;
import io.github.shanhm1991.echo.algorithm.sort.InsertSort;
import io.github.shanhm1991.echo.algorithm.sort.InsertXierSort;
import io.github.shanhm1991.echo.algorithm.sort.MergeSort;
import io.github.shanhm1991.echo.algorithm.sort.QuickRepeatSort;
import io.github.shanhm1991.echo.algorithm.sort.QuickSort;
import io.github.shanhm1991.echo.algorithm.sort.SelectSort;
import io.github.shanhm1991.echo.algorithm.sort.Sort;

/**
 *
 * @author shanhm1991@163.com
 *
 */
public class SortTest {

	private void sortTest(Sort sort) {
		List<Integer> list1 = new ArrayList<>(100);
		List<Integer> list2 = new ArrayList<>(100);

		SecureRandom random = new SecureRandom();
		for (int i = 0; i < 100; i++) {
			list1.add(random.nextInt(100));
		}
		list2.addAll(list1);

		Collections.sort(list1);
		sort.sort(list2);
		Assertions.assertArrayEquals(list1.toArray(), list2.toArray());
	}

	@Test
	public void testSelect() {
		sortTest(new SelectSort());
	}

	@Test
	public void testInsert() {
		sortTest(new InsertSort());
	}

	@Test
	public void testInsertBinary() {
		sortTest(new InsertBinarySort());
	}

	@Test
	public void testInsertXierSort() {
		sortTest(new InsertXierSort());
	}

	@Test
	public void testMergeSort() {
		sortTest(new MergeSort());
	}

	@Test
	public void testQuickSort() {
		sortTest(new QuickSort());
	}

	@Test
	public void testQuickRepeatSort() {
		sortTest(new QuickRepeatSort());
	}

	@Test
	public void testSortHeap() {
		SecureRandom random = new SecureRandom();
		List<Long> list = new ArrayList<>();
		for (int i = 0; i < 40; i++) {
			list.add(random.nextLong() % 200000000000000L);
		}

		Comparator<Long> comparator = Comparator.reverseOrder();

		SortHeap<Long> heap = new SortHeap<>(40, comparator);
		for (int i = 0; i < 40; i++) {
			heap.put(list.get(i));
		}

		Object[] array = new Object[40];
		for (int i = 0; i < 40; i++) {
			array[i] = heap.take();
		}

		list.sort(comparator);
		Assertions.assertArrayEquals(array, list.toArray());
	}
}
