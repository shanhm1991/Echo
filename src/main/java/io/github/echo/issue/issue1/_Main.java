package io.github.echo.issue.issue1;

/**
 * 
 * 由于实现没有考虑关闭处理，所以将Consumer放在Producer之后关闭，以便将没有消费完的任务消费干净
 * 
 * @author shanhm1991@163.com
 *
 */
public class _Main {

	public static void main(String[] args) throws InterruptedException {

		TaskList<Task> taskList = new TaskList<Task>();

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
