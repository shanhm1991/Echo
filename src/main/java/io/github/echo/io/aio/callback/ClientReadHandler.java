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
public class ClientReadHandler implements CompletionHandler<Integer, ByteBuffer> {
	
	private static final Logger LOG = Logger.getLogger(ClientReadHandler.class);
	
	private AsynchronousSocketChannel socketchannel;
	
	public ClientReadHandler(AsynchronousSocketChannel clientChannel) {
		this.socketchannel = clientChannel;
	}

	@Override
	public void completed(Integer result, ByteBuffer buffer) {
		buffer.flip();
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		
		String resp = new String(bytes);
		LOG.info("<< " + resp);
	}

	@Override
	public void failed(final Throwable e, ByteBuffer attachment) {
		LOG.error("read failed, " + e.getMessage());
		IOUtils.closeQuietly(socketchannel); 
	}
}
