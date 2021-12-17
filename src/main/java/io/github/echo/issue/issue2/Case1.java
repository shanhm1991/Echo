package io.github.echo.issue.issue2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 简单的思路是：从输入流中按行读取所有数字放到一个集合中，然后排序，并取前100个的和，只是这样会比较慢。
 * 读取流的时间应该是省不了，但是可以改进下排序求和的过程：在插入的时候直接比较排序
 * 1.1 定义一个LinkedList，第一个元素直接插入，然后
 * 1.2 如果待插入的数值大于第一个元素，就将其插到列表首节点。
 * 1.3. 否则如果待插入的元素小于末尾元素，就将其插到列表末尾节点。
 * 1.4. 否则就挨个比较，放入比前一个节点小比后一个节点大的位置。
 * 1.5. 放完之后如果列表长度大于100，就去除最后一个节点（index=100）。
 * 这样可以预估一下最坏的情况下需要的比较次数： 0 + 1 + 2 + 3 + ... + 99 + (400000 - 100) * 100 次
 * 
 * 因为这里主要的操作是插入删除，所以定义LinkedList实例。
 * 指给LinkedList的引用，是为了可以直接调用addFirst(E e)，addLast(E,e)和removeLast(E,e)方法，
 * 如果指给List引用的话，调用List的add(int index ,E e)和remove(int index)其实也一样，
 * LinkedList是链表实现的，本身不具有index下标属性，
 * 为了实现List的方法，它通过判断index是靠前还是靠后，决定从前向后还是从后向前通过引用计数。
 * 
 * @author shanhm1991
 *
 */
public class Case1 {
	 
    private static LinkedList<Long> list = new LinkedList<Long>();
 
    public static long p() throws IOException{
        String url = "http://localhost:8888/numbers.txt";
        URLConnection urlconn = new URL(url).openConnection(); 
        urlconn.connect();
        HttpURLConnection httpconn =(HttpURLConnection)urlconn;
        int resp  = httpconn.getResponseCode();
        if(httpconn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("连接失败["+ resp +"],url:" + url);
        } 
        BufferedReader br = null;
        try{
            InputStreamReader input = new InputStreamReader(urlconn.getInputStream(),"UTF-8");
            br = new BufferedReader(input, 4 * 1024 *1024);  //4M缓存
            String line = "";
            while((line = br.readLine()) != null){
                try{
                    list.add(Long.parseLong(line.trim()));
                    //最多只插入100，减少遍历次数
                    if(list.size() > 100){
                        list.removeLast();
                    }
                }catch(Exception e){
                    System.out.println("非法数据：" + line); 
                }
            }
        }finally{
            if(br != null){
                br.close();
            }
        }
        long result = 0;
        for(long i : list){
            result = result + i;
        }
        return result;
    }
 
    private static void add(long e) {
        //空集合，直接插入元素
        if(list.isEmpty()){
            list.add(e);
            return;
        }
        //大于头节点，直接插入到头节点
        if(e >= list.getFirst()){
            list.addFirst(e);
            return;
        }
        //小于尾节点，直接插入尾节点
        if(e < list.getLast()){
            if(list.size() < 100){
                list.addLast(e);
            }
            return;
        }
        //插入到中间节点
        for (int i = 0; i < list.size(); i++) {
            if (e >= list.get(i + 1)) {
                list.add(i + 1, e);
                return;
            }
        }
    }
    
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
            if(list.size() > 100){
                list.removeLast();
            }
        }
        long result = 0;
        for(long i : list){
            result = result + i;
        }
        System.out.println("result:" + result);
        System.out.println("总耗时:" + (System.currentTimeMillis() - t) + "ms");
        System.out.println(list);
    }
}
