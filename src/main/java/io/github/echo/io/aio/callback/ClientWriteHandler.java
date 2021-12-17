package io.github.echo.io.aio.callback;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.ReadPendingException;
import java.nio.channels.ShutdownChannelGroupException;

import org.apache.log4j.Logger;

import io.github.echo.io.Util;

public class ClientWriteHandler implements CompletionHandler<Integer, ByteBuffer> {

	private static final Logger LOG = Logger.getLogger(ClientWriteHandler.class);

	private AsynchronousSocketChannel channel;

	public ClientWriteHandler(AsynchronousSocketChannel clientChannel) {
		this.channel = clientChannel;
	}

	@Override
	public void completed(final Integer result, ByteBuffer buffer) {
		if (buffer.hasRemaining()) {
			channel.write(buffer, buffer, this);
		}else {
			ByteBuffer readBuffer = ByteBuffer.allocate(184);
			try{
				channel.read(readBuffer, readBuffer, new ClientReadHandler(channel));
			}catch(ReadPendingException e){
				//channel的上一次read结束前,再次read会出现ReadPendingException,但是又不想阻塞write线程
			}catch(ShutdownChannelGroupException e){
				LOG.warn("channel group has shutdown"); 
			}
		}
	}

	@Override
	public void failed(Throwable e, ByteBuffer attachment) {
		LOG.error("发送失败," +  e.getMessage());

		//结束客户端
		Util.close(channel);
	}

}
