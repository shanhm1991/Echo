package io.github.echo.issue.issue2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 *  LinkedList的add()方法在插入中间节点时，其实寻找了两次元素的下标，get(i)和add(i,e)，
 *  如果在比较得出元素应该插入的index时就直接将元素插入是不是可以更节省时间呢。
 *  由于LinkedList的一些方法和元素不可继承，就模拟写了个SortLongLinkedList，
 *  添加了一个linkLongWithSort(long e)方法，对于待插入的元素，当找到位置时直接插入，
 *  而不是先记下找到的位置index，然后再调用add把它插入到index位置
 * 
 * @author shanhm1991
 *
 */
public class Case2 {
	 
    private static SortLongLinkedList list = new SortLongLinkedList();
 
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
                    add(Long.parseLong(line.trim()));
                    //最多只插入100，减少遍历次数
                    if(list.size() > 100){
                        list.unlinkLast();
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
            list.linkFirst(e);
            return;
        }
        //大于头节点，直接插入到头节点
        if(e >= list.getFirst()){
            list.linkFirst(e);
            return;
        }
        //小于尾节点，直接插入尾节点
        if(e < list.getLast()){
            if(list.size() < 100){
                list.linkLast(e);
            }
            return;
        }
        //插入到中间节点
        list.linkLongWithSort(e);
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
                list.unlinkLast();  
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
