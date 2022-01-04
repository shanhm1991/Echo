package io.github.echo.io.aio.callback;

import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

/**
 * 
 * @author shanhm1991
 *
 */
public class ClientConnectHandler implements CompletionHandler<Void, CountDownLatch> {
	
	private static final Logger LOG = Logger.getLogger(ClientConnectHandler.class);
	
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
