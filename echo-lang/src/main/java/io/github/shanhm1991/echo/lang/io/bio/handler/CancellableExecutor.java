package io.github.shanhm1991.echo.lang.io.bio.handler;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * 扩展下ThreadPoolExecutor，自定义下newTaskFor()的封装，以便自定义任务的取消处理
 * 
 * @author shanhm1991
 *
 */
public class CancellableExecutor extends ThreadPoolExecutor {
	
	public CancellableExecutor(int corePoolSize, int maximumPoolSize, long keepAliveSeconds) {
		super(corePoolSize, maximumPoolSize, keepAliveSeconds, TimeUnit.SECONDS,
				new LinkedBlockingDeque<>(100), new HandleThreadFactory());
	}

	@Override
	protected <T> RunnableFuture<T> newTaskFor(Runnable runnable,T value) {
		if (runnable instanceof CancellableHandler) {
			return new CancellableFuture<>(runnable, value);
		} else {
			return super.newTaskFor(runnable,value);
		}
	}

	private static class HandleThreadFactory implements ThreadFactory {
		private final AtomicInteger index = new AtomicInteger(0);
		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "server-handler-" + index.incrementAndGet());
		}
	}
}
