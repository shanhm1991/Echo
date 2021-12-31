package io.github.echo.issue.issue1;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author shanhm1991@163.com
 *
 */
public class Producer extends Thread{
    
	private static final Logger LOGGER = LoggerFactory.getLogger(Consumer.class);
    
    private static AtomicInteger index = new AtomicInteger(0);
    
    private TaskQueue<Task> taskList;
    
    public Producer(TaskQueue<Task> taskList){
        this.setName("producer-" + index.incrementAndGet()); 
        this.taskList = taskList;
    }
    
    @Override
    public void run(){
        while(true){
            Task task = new Task();
            try {
            	LOGGER.info("压入子弹" + task.getIndex()); 
                taskList.offer(task);
            } catch (InterruptedException e) {
            	LOGGER.info("停止生产"); 
                return;
            }
        }
    }
}
