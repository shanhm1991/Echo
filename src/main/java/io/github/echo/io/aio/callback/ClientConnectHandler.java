package io.github.echo.io.aio.callback;

import java.nio.channels.CompletionHandler;

import org.apache.log4j.Logger;

public class ClientConnectHandler implements CompletionHandler<Void, AioClient> {
	
	private static final Logger LOG = Logger.getLogger(ClientConnectHandler.class);
	
	@Override
	public void completed (Void result, AioClient attachment) {
		LOG.info("连接服务器成功...");
	}

	@Override
	public void failed(Throwable e, AioClient client) {
//		Util.close(channel);
		LOG.error("连接服务器失败," +  e.getMessage());
	}

}
