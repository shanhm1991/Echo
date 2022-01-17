package io.github.echo.io.aio.callback;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author shanhm1991
 *
 */
public class ServerReadHandler implements CompletionHandler<Integer, ByteBuffer> {

	private static final Logger LOG = LoggerFactory.getLogger(ServerReadHandler.class);

	private AsynchronousSocketChannel serverSocketChannel; 

	public ServerReadHandler(AsynchronousSocketChannel serverSocketChannel) {  
		this.serverSocketChannel = serverSocketChannel;  
	}  
	@Override  
	public void completed(Integer result, ByteBuffer buffer) {  
		buffer.flip();
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		String msg = new String(bytes);
		LOG.info(">> accept " + msg);

		long beginTime = System.currentTimeMillis();
		String response = "resp for " + msg;
		bytes = response.getBytes();

		buffer.clear();
		buffer.put(bytes);
		buffer.flip();
		try {
			Thread.sleep(100); // 模拟处理耗时
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		LOG.info("<< " + response + ", cost=" + (System.currentTimeMillis() - beginTime) + "ms"); 
		serverSocketChannel.write(buffer, buffer, new ServerWriteHandler(serverSocketChannel)); 
	}  

	@Override  
	public void failed(Throwable e, ByteBuffer attachment) {  
		LOG.error("read failed, " + e.getMessage());
		IOUtils.closeQuietly(serverSocketChannel); 
	}  
}
