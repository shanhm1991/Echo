package io.github.echo.io.bio.handler;

import java.util.concurrent.RunnableFuture;

/**
 * 
 * 扩展Runnable，增加自定义的取消方法cancel()和构造RunnableFuture的工厂方法newTask()
 * 
 * @author shanhm1991
 *
 */
public interface CancellableHandler extends Runnable{
	
	void cancel();

	<T> RunnableFuture<T> newTask();
}