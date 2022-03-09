package io.github.shanhm1991.echo.leetcode.issue3;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author shanhm1991@163.com
 *
 */
public class Printer extends Thread{
	 
	private static final Logger LOGGER = LoggerFactory.getLogger(Printer.class);
 
    private static final int INDEX_MAX = 75;
    
    private static int index = 0;
    
    private final int count;
    
    private final Lock lock;
 
    private final Condition condition;
    
    private final Condition nextCondition;
 
    public Printer(String name, Lock lock, Condition condition, Condition nextCondition, int count){
        this.setName(name);
        this.count = count;
        this.lock = lock;
        this.condition = condition;
        this.nextCondition = nextCondition;
    }
 
    @Override
    public void run(){
        while(true){
            lock.lock();
            try{
                condition.await();
                if(index >= INDEX_MAX){
                	LOGGER.info("已达到最大值，停止计数"); 
                    nextCondition.signal(); // 停止自己之前将下一个线程唤醒
                    return;
                }
                
                for(int i = 0; i < count; i++){
                	LOGGER.info("{}", ++index);
                }
                nextCondition.signal();
            } catch (InterruptedException e) {
                // ignore
            }finally{
                lock.unlock();
            }
        }
    }
 
}
