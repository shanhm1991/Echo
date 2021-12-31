package io.github.echo.issue.issue1;

/**
 * 
 * <p>问题：
 * <br>设计一个符合生产者和消费者问题的程序，对一个对象（枪膛）进行操作，其最大容量是12颗子弹。
 * <br>生产者线程是一个压入线程，它不断向枪膛中压入子弹；消费者线程是一个射出线程，它不断从枪膛中射出子弹。
 * 
 * <p>要求：
 * <br>1. 为了防止两个线程访问一个资源时出现忙等待，要使用的wait-notify函数，是两个线程交替执行
 * <br>2. 程序输出，要模拟体现对枪膛的压入和射出操作
 * 
 * <p>实现：
 * <br>通过内置条件队列wait-notify将普通list构建成一个可以阻塞等待的容器，类似于LinkedBlockingQueue
 * <br>并且通过isEmpty()和isFull()判断来减少不必要的条件唤醒，因为只有在空变为非空，或满变为非满时才需要进行唤醒
 * 
 * <p>待改进：
 * <br>如果使用Condition可以将等待条件分开，进一步降低竞争程度
 * <br>另外，由于这里没有考虑关闭处理，所以将Consumer放在Producer之后关闭，以便将没有消费完的任务消费干净
 * 
 * @author shanhm1991@163.com
 *
 */
public class _Main {

	public static void main(String[] args) throws InterruptedException {

		TaskQueue<Task> taskList = new TaskQueue<Task>();

		Producer producer1 = new Producer(taskList);
		Producer producer2 = new Producer(taskList);
		Producer producer3 = new Producer(taskList);

		Consumer customer1 = new Consumer(taskList);
		Consumer customer2 = new Consumer(taskList);
		Consumer customer3 = new Consumer(taskList);

		producer1.start();
		producer2.start();
		producer3.start();

		customer1.start();
		customer2.start();
		customer3.start();

		Thread.sleep(15);
		producer1.interrupt();
		producer2.interrupt();
		producer3.interrupt();

		Thread.sleep(20);
		customer1.interrupt();
		customer2.interrupt();
		customer3.interrupt();
	}
}
