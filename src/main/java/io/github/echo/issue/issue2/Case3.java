package io.github.echo.issue.issue2;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * 在插入中间节点时省去了一次获取下标为index的元素的动作，但是在比较的时候还是顺序比较的，
 * 如果将顺序比较换成二分法排序插入，应该是可以更快的。但是由于LinkedList不能直接通过下标获取元素，
 * 如果在LinkedList上对元素个数用二分法排序反而适得其反，因为获取中间下标元素本身就增加了寻找动作。
 * 所以直接用ArrayList反而比较合适，只是ArrayList中掺和了数组拷贝的成本，
 * 这里直接定义了一个固定长度的数组，本质上与使用ArrayList是相同的动作。
 * 
 * 这里提升除了二分法的原因，还省去了很多java对象构造，
 * 在使用List的时候，他其实会把每个元素构造成一个Node的节点实例对象，这个实例的new动作积少成多也是可观的，
 * 另外直接使用long，也省去了自动装箱的过程。
 * 
 * @author shanhm1991
 *
 */
public class Case3 {
	 
    static long[] array = new long[100];
 
    static int inserted = 0;
    
    static int modified = 0;
 
    public static void test(){
        SecureRandom random = new SecureRandom();  
        List<Long> alist = new ArrayList<Long>(400000);
        for(int i = 0;i < 400000;i++){  
            long a = random.nextLong() % 200000000000000L; 
            alist.add(a);
        }
 
        long t = System.currentTimeMillis();
        for(long l : alist){
            add(l);
        }
 
        long result = 0;
        for(long i : array){
            result = result + i;
        }
        System.out.println("result:" + result);
        System.out.println("总耗时:" + (System.currentTimeMillis() - t) + "ms");
        System.out.println(Arrays.asList(array));
    }
 
    private static void count(){
        if(inserted >= 100){
            return;
        }
        inserted++;
    }
 
    private static void add(long e){
        if(modified == 0){
        	modified++;
            array[0] = e;
            count();
            return;
        }
 
        //大于头节点，直接插入到头节点
        if(e >= array[0]){
            int move = inserted;
            if(inserted == 100){
                move = 99;
            }
            System.arraycopy(array, 0, array, 1, move);
            array[0] = e;
            count();
            return;
        }
        //小于尾节点，直接插入尾节点
        if(e < array[inserted - 1]){ 
            if(inserted < 100){
                array[inserted] = e;
                count();
            }
            return;
        }
 
        middleAdd(e);
    }
 
    // 二分法查找中间插入
    private static void middleAdd(long e){
        int left = 0;
        int right = inserted - 1;
        int middle = 0;
        while( right >= left){
            middle = ( left + right) / 2;
            if(e < array[middle]){
                left = middle + 1; 
            }else if(e > array[middle]){
                right = middle - 1;
            }
        }
        
        int index = middle;
        if(right == middle){//right < left && e <= array[middle]
            index = middle + 1; 
        }
        
        int move = inserted - index;
        if(move > 0){
            if(inserted == 100){//数组越界
                move = move - 1;
            }
            System.arraycopy(array, index, array, index + 1, move);
        }
        array[index] = e;
        count();
    }
}

