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
public class ClientReadHandler implements CompletionHandler<Integer, ByteBuffer> {
	
	private final AsynchronousSocketChannel socketchannel;
	
	public ClientReadHandler(AsynchronousSocketChannel clientChannel) {
		this.socketchannel = clientChannel;
	}

	@Override
	public void completed(Integer result, ByteBuffer buffer) {
		buffer.flip();
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		
		String resp = new String(bytes);
		log.info("<< " + resp);
	}

	@Override
	public void failed(final Throwable e, ByteBuffer attachment) {
		log.error("read failed, " + e.getMessage());
		IOUtils.closeQuietly(socketchannel); 
	}
}
