package io.github.shanhm1991.echo.lang.io.aio.callback;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * 
 * @author shanhm1991
 *
 */
@Slf4j
public class ClientWriteHandler implements CompletionHandler<Integer, ByteBuffer> {

	private final AsynchronousSocketChannel socketchannel;

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
		log.error("write failed, " +  e.getMessage());
		IOUtils.closeQuietly(socketchannel); 
	}
}
