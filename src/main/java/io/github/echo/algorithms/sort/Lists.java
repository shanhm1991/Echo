package io.github.echo.algorithms.sort;

import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* 
* @author shanhm1991@163.com
* 
*/
public class Lists {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Lists.class);
	
	private static int times_compare;
	
	private static int times_swap;
	
	public static <T extends Comparable<? super T>> void sort(List<T> list) {
		doSort(list, null);
    }
	
	public static <T extends Comparable<? super T>> void sort(List<T> list, Comparator<? super T> comp) {
		doSort(list, comp);
    }
	
	@SuppressWarnings("unchecked")
	private static <E> void doSort(List<E> list, Comparator<? super E> comp) {
        Object[] array = list.toArray();
        
        sort_bubble(array, comp);
        
        LOGGER.info("共经过{}次比较，{}次交换", times_compare, times_swap); 
        ListIterator<E> i = list.listIterator();
        for (Object e : array) {
            i.next();
            i.set((E) e);
        }
    }
	
	private static void swap(Object[] x, int a, int b) {
		LOGGER.info("swap[{}]：{}[{}] <> {}[{}]", ++times_swap, x[a], a, x[b], b); 
        Object t = x[a];
        x[a] = x[b];
        x[b] = t;
    }
	
	/**
	 * 冒泡排序
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void sort_bubble(Object[] array, Comparator comp) {
		for(int i = 0; i < array.length; i++){
			for(int j = i + 1; j < array.length; j++){
				LOGGER.info("compare[{}]：{}[{}] <> {}[{}]", ++times_compare, array[i], i, array[j], j); 
				if(((Comparable)array[i]).compareTo(array[j]) > 0){
					swap(array, i ,j);
				}
			}
		}
	}
}
