package io.github.echo.io.aio.callback;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author shanhm1991
 *
 */
public class ServerAcceptHandler implements CompletionHandler<AsynchronousSocketChannel, AioServer> {
	
	private static final Logger LOG = LoggerFactory.getLogger(ServerAcceptHandler.class);

	@Override
	public void completed(AsynchronousSocketChannel serverSocketChannel, AioServer server) {
		// 注册异步读事件
		ByteBuffer readBuffer = ByteBuffer.allocate(1024);  
		serverSocketChannel.read(readBuffer, readBuffer, new ServerReadHandler(serverSocketChannel));  
		// 继续监听下一次消息
		server.getServerChannel().accept(server, this);
	}

	@Override
	public void failed(Throwable e, AioServer server) {
		LOG.error("accept failed, " + e.getMessage());
	}
}
