package org.eto.essay.questions.question1;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author shanhm1991@163.com
 *
 */
public class Consumer extends Thread{
    
	private static final Logger LOGGER = LoggerFactory.getLogger(Consumer.class);
    
    private static AtomicInteger index = new AtomicInteger(0);
    
    private TaskList<Task> taskList;
    
    public Consumer(TaskList<Task> taskList){
        this.setName("consumer-" + index.incrementAndGet()); 
        this.taskList = taskList;
    }
    
    @Override
    public void run(){
        while(true){
            Task task;
            try {
                task = taskList.take();
            } catch (InterruptedException e) {
            	LOGGER.info("停止消费"); 
                return;
            }
            LOGGER.info("射出子弹" + task.getIndex()); 
            task.run();
        }
    }
}

