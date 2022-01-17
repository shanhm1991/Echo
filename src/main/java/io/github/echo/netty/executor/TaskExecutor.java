package io.github.echo.netty.executor;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.util.concurrent.SingleThreadEventExecutor;

/**
 * 
 * @author shanhm1991
 * 
 */
public class TaskExecutor extends SingleThreadEventExecutor {
	
	private static AtomicInteger index = new AtomicInteger();

	protected TaskExecutor() { 
		super(null, new ThreadFactory(){
			@Override
			public Thread newThread(Runnable r) {
				return new Thread("task-executor-" + index.incrementAndGet()){
					@Override
					public void run() {
						r.run();
					}
				};
			}
		}, true);
	}
	
	@Override
	protected void run() {
		Runnable r;
		while((r = takeTask()) != null){
			r.run();
		}
		confirmShutdown(); // 退出之前执行 confirmShutdown()
	}
}
