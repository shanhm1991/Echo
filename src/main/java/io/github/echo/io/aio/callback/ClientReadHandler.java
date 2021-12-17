package io.github.echo.io.aio.callback;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.apache.log4j.Logger;

import io.github.echo.io.Util;

public class ClientReadHandler implements CompletionHandler<Integer, ByteBuffer> {
	
	private static final Logger LOG = Logger.getLogger(ClientReadHandler.class);
	
	private AsynchronousSocketChannel channel;
	
	public ClientReadHandler(AsynchronousSocketChannel clientChannel) {
		this.channel = clientChannel;
	}

	@Override
	public void completed(Integer result, ByteBuffer buffer) {
		buffer.flip();
		final byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		try {
			LOG.info("客户端收到响应:" + new String(bytes, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			System.err.println(this.getClass().getName() + "：" +  e.getMessage());
		}
	}

	@Override
	public void failed(final Throwable e, ByteBuffer attachment) {
		LOG.error("客户端获取响应失败," + e.getMessage());
		//结束客户端
		Util.close(channel);
	}

}
