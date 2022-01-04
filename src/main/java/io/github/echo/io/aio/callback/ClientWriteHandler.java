package io.github.echo.io.aio.callback;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * 
 * @author shanhm1991
 *
 */
public class ClientWriteHandler implements CompletionHandler<Integer, ByteBuffer> {

	private static final Logger LOG = Logger.getLogger(ClientWriteHandler.class);

	private AsynchronousSocketChannel socketchannel;

	public ClientWriteHandler(AsynchronousSocketChannel clientChannel) {
		this.socketchannel = clientChannel;
	}

	@Override
	public void completed(final Integer result, ByteBuffer buffer) {
		if (buffer.hasRemaining()) {
			socketchannel.write(buffer, buffer, this);
		}else {
			buffer.clear();
			socketchannel.read(buffer, buffer, new ClientReadHandler(socketchannel)); // 异步读响应
		}
	}

	@Override
	public void failed(Throwable e, ByteBuffer attachment) {
		LOG.error("write failed, " +  e.getMessage());
		IOUtils.closeQuietly(socketchannel); 
	}
}
