package io.github.echo.io.aio.callback;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.apache.log4j.Logger;

public class ServerAcceptHandler implements CompletionHandler<AsynchronousSocketChannel, AioServer> {
	
	private static final Logger LOG = Logger.getLogger(ServerAcceptHandler.class);

	@Override
	public void completed(AsynchronousSocketChannel channel, AioServer server) {
		
		server.serverChannel.accept(server,this);
		
		//根据协议定义好的消息长度，否则连续发送消息，在读取时会出现ReadPendingException
		ByteBuffer buffer = ByteBuffer.allocate(184);
		channel.read(buffer, buffer, new ServerReadHandler(channel));
	}

	@Override
	public void failed(Throwable e, AioServer server) {
		LOG.error("接收异常," + e.getMessage());
	}
}
