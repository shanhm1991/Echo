package io.github.echo.issue.issue3;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * <p>问题：
 * <br>启动三个线程打印递增的数字
 * <br>线程1先打印1,2,3,4,5，然后是线程2打印6,7,8,9,10，然后是线程3打印11,12,13,14,15，接着再由线程1打印16,17,18,19,20...由此类推，直到打印到75
 * 
 * <p>实现：
 * <br>通过三个条件Condition，轮流进行放开
 * 
 * @author shanhm1991@163.com
 *
 */
public class _Main {

	public static void main(String[] args) throws InterruptedException {

		Lock lock = new ReentrantLock();
		Condition condition1 = lock.newCondition();
		Condition condition2 = lock.newCondition();
		Condition condition3 = lock.newCondition();

		Printer printer1 = new Printer("printer-1", lock, condition1, condition2, 5);
		Printer printer2 = new Printer("printer-2", lock, condition2, condition3, 5);
		Printer printer3 = new Printer("printer-3", lock, condition3, condition1, 5);

		printer1.start();
		printer2.start();
		printer3.start();

		Thread.sleep(10); // 这里主线程睡一会，等待printer到达阻塞点，如果提前唤醒，printer将会永远阻塞下去

		lock.lock();
		try{
			condition1.signal(); // 放开第一个
		} finally{
			lock.unlock();
		}
	}
}
