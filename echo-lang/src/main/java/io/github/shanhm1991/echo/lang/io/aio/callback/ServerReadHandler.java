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
public class ServerReadHandler implements CompletionHandler<Integer, ByteBuffer> {

	private final AsynchronousSocketChannel serverSocketChannel; 

	public ServerReadHandler(AsynchronousSocketChannel serverSocketChannel) {  
		this.serverSocketChannel = serverSocketChannel;  
	}  
	@Override  
	public void completed(Integer result, ByteBuffer buffer) {  
		buffer.flip();
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		String msg = new String(bytes);
		log.info(">> accept " + msg);

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
		log.info("<< " + response + ", cost=" + (System.currentTimeMillis() - beginTime) + "ms"); 
		serverSocketChannel.write(buffer, buffer, new ServerWriteHandler(serverSocketChannel)); 
	}  

	@Override  
	public void failed(Throwable e, ByteBuffer attachment) {  
		log.error("read failed, " + e.getMessage());
		IOUtils.closeQuietly(serverSocketChannel); 
	}  
}
