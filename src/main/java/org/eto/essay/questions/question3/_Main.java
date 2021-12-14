package org.eto.essay.questions.question3;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
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
