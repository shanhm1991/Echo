package org.eto.essay.questions.question3;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.apache.log4j.Logger;

/**
 * 
 * @author shanhm1991
 *
 */
public class Printer extends Thread{
	 
    private static final Logger LOG = Logger.getLogger(Printer.class);
 
    private static final int INDEX_MAX = 75;
    
    private static int index = 0;
    
    private int count;
    
    private Lock lock;
 
    private Condition condition;
    
    private Condition nextCondition;
 
    public Printer(String name,Lock lock,Condition condition,Condition nextCondition,int count){
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
                    LOG.info("已达到最大值，停止计数"); 
                    nextCondition.signal();//停止自己之前将下一个线程唤醒
                    return;
                }
                
                for(int i = 0;i < count;i++){
                    LOG.info(String.valueOf(++index));
                }
                nextCondition.signal();
            } catch (InterruptedException e) {
                //忽略
            }finally{
                lock.unlock();
            }
        }
    }
 
}
