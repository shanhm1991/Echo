package io.github.echo.issue.issue1;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 通过内置锁以及wait-notify将普通list构建一个可以阻塞等待的容器，类似于LinkedBlockingQueue
 * 
 * 并通过isEmpty()和isFull()判断来减少不必要的条件唤醒，只有在空变为非空，或满变为非满时才需要进行唤醒
 * 
 * 不过这里使用的是内置条件队列，如果通过Condition可以将等待条件分开，进一步降低竞争程度
 * 
 * @author shanhm1991@163.com
 *
 */
public class TaskQueue<T extends Runnable> {
	 
	private static final Logger LOGGER = LoggerFactory.getLogger(Task.class);
 
    private final int capacity = 12;
 
    private int size = 0;
 
    private List<T> taskList = new LinkedList<T>();
 
    /**
     * 
     * @param task
     * @throws InterruptedException
     */
    public synchronized void offer(T task) throws InterruptedException{ 
        while(isFull()){
            wait();
        }
        
        boolean isEmpty = isEmpty();
        taskList.add(task);
        size++;
       
        if(isEmpty){
        	LOGGER.info("唤醒消费者..."); 
            notifyAll();
        }
    }
 
    /**
     * 
     * @return
     * @throws InterruptedException
     */
    public synchronized T take() throws InterruptedException{
        while(isEmpty()){
            wait();
        }
        
        boolean isFull = isFull();
        T task = taskList.remove(0);
        size--;
        
        if(isFull){
        	LOGGER.info("唤醒生产者..."); 
            notifyAll();
        }
        return task;
    }
 
    private boolean isFull(){
        return size == capacity;
    }
 
    private boolean isEmpty(){
        return size == 0;
    }
}
