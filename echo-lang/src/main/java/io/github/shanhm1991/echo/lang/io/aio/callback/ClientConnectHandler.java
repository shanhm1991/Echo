package io.github.shanhm1991.echo.lang.io.aio.callback;

import lombok.extern.slf4j.Slf4j;

import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

/**
 * 
 * @author shanhm1991
 *
 */
@Slf4j
public class ClientConnectHandler implements CompletionHandler<Void, CountDownLatch> {
	
	@Override
	public void completed (Void result, CountDownLatch connectLatch) {
		log.info("连接成功...");
		connectLatch.countDown();
	}

	@Override
	public void failed(Throwable e, CountDownLatch connectLatch) {
		log.error("连接失败," +  e.getMessage());
		connectLatch.countDown();
	}
}
