package io.github.shanhm1991.echo.netty.executor;

import java.util.concurrent.Executor;

import io.netty.util.concurrent.DefaultEventExecutorChooserFactory;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.MultithreadEventExecutorGroup;

/**
 * 
 * @author shanhm1991
 * 
 */
public class NettyExecutorGroup extends MultithreadEventExecutorGroup {

	public NettyExecutorGroup(int n) { // 使用默认的单线程执行，以及默认选择器 
		super(n, null, DefaultEventExecutorChooserFactory.INSTANCE, new Object[3]); 
	}

	@Override
	protected EventExecutor newChild(Executor executor, Object... args) {
		return new NettyExecutor();
	}
}
