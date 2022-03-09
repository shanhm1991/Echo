package io.github.shanhm1991.echo.netty.executor;

import io.netty.util.concurrent.SingleThreadEventExecutor;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * @author shanhm1991
 * 
 */
public class NettyExecutor extends SingleThreadEventExecutor {
	
	private static final AtomicInteger index = new AtomicInteger();

	protected NettyExecutor() { 
		super(null, r -> {
			return new Thread("task-executor-" + index.incrementAndGet()){
				@Override
				public void run() {
					r.run();
				}
			};
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
