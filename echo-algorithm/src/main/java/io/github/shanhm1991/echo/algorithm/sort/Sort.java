package io.github.shanhm1991.echo.algorithm.sort;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * 排序接口
 *
 * @author shanhm1991@163.com
 *
 */
public abstract class Sort {

	protected static final Logger LOGGER = LoggerFactory.getLogger(Sort.class);

	protected int compareTimes;

	protected int swapTimes;

	@SuppressWarnings("all")
	protected int comp(Object[] array, int a, int b, Comparator comp) {
		LOGGER.debug(
				String.format("%60s %2s comp: %2s<>%2s ([%2s]<>[%2s])", " ", ++compareTimes, array[a], array[b], a, b));
		if (comp != null) {
			return comp.compare(array[a], array[b]);
		} else {
			return ((Comparable) array[a]).compareTo(array[b]);
		}
	}

	protected void swap(Object[] array, int a, int b) {
		Object t = array[a];
		array[a] = array[b];
		array[b] = t;
		LOGGER.debug(String.format("%2s swap: %2s<>%2s ([%2s]<>[%2s]) = %s", ++swapTimes, array[b], array[a], a, b,
				Arrays.toString(array)));
	}

	@SuppressWarnings("all")
	protected abstract void sort(Object[] array, Comparator comp);

	/**
	 * 排序
	 *
	 * @param list
	 * @param <E>
	 */
	public <E extends Comparable<? super E>> void sort(List<E> list) {
		sort(list, null);
	}

	/**
	 * 排序，自定义比较器
	 *
	 * @param list
	 * @param comp
	 * @param <E>
	 */
	@SuppressWarnings("all")
	public <E extends Comparable<? super E>> void sort(List<E> list, Comparator<? super E> comp) {
		Object[] array = list.toArray();
		sort(array, comp);

		ListIterator<E> i = list.listIterator();
		for (Object e : array) {
			i.next();
			i.set((E) e);
		}
	}
}
