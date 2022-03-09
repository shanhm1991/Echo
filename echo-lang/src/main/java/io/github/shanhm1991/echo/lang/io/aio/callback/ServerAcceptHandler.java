package io.github.shanhm1991.echo.lang.io.aio.callback;

import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * 
 * @author shanhm1991
 *
 */
@Slf4j
public class ServerAcceptHandler implements CompletionHandler<AsynchronousSocketChannel, AioServer> {
	
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
		log.error("accept failed, " + e.getMessage());
	}
}
