package io.github.echo.io.aio.callback;

import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author shanhm1991
 *
 */
public class ClientConnectHandler implements CompletionHandler<Void, CountDownLatch> {
	
	private static final Logger LOG = LoggerFactory.getLogger(ClientConnectHandler.class);
	
	@Override
	public void completed (Void result, CountDownLatch connectLatch) {
		LOG.info("连接成功...");
		connectLatch.countDown();
	}

	@Override
	public void failed(Throwable e, CountDownLatch connectLatch) {
		LOG.error("连接失败," +  e.getMessage());
		connectLatch.countDown();
	}
}
