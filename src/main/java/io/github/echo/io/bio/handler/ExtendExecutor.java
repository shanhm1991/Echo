package io.github.echo.io.bio.handler;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * 扩展ThreadPoolExecutor，对于CancellableRunnable，在构造RunnableFuture的时候调用它自己的newTask()方法
 * 
 * @author shanhm1991
 *
 */
public class ExtendExecutor extends ThreadPoolExecutor {
	
	public ExtendExecutor(int corePoolSize, int maximumPoolSize, long keepAliveSeconds, int port) {
		super(corePoolSize, maximumPoolSize, keepAliveSeconds, TimeUnit.SECONDS,
				new LinkedBlockingDeque<Runnable>(100), new HandleThreadFactory());
	}

	@Override
	protected <T> RunnableFuture<T> newTaskFor(Runnable runnable,T value) {
		if (runnable instanceof CancellableHandler) {
			return ((CancellableHandler) runnable).newTask();
		} else {
			return super.newTaskFor(runnable,value);
		}
	}

	private static class HandleThreadFactory implements ThreadFactory {
		private AtomicInteger index = new AtomicInteger(0);

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "handler-" + index.incrementAndGet());
		}
	}
}
