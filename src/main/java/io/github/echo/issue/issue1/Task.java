package io.github.echo.issue.issue1;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author shanhm1991@163.com
 *
 */
public class Task implements Runnable{
	 
	private static final Logger LOGGER = LoggerFactory.getLogger(Task.class);
    
    private static AtomicInteger index = new AtomicInteger(0);
    
    private int currentIndex;
    
    public Task(){
        currentIndex = index.incrementAndGet();
    }
    
    @Override
    public void run() {
    	LOGGER.info("子弹[{}]执行飞行", currentIndex); 
    }
 
    public String getIndex(){
        return String.valueOf(currentIndex);
    }
}
