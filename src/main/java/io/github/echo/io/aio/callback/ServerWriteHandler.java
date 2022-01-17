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
public class ServerWriteHandler implements CompletionHandler<Integer, ByteBuffer> {
	
	private static final Logger LOG = LoggerFactory.getLogger(ServerWriteHandler.class);

	private AsynchronousSocketChannel serverSocketChannel; 
	
	public ServerWriteHandler(AsynchronousSocketChannel serverSocketChannel) {  
		this.serverSocketChannel = serverSocketChannel;  
	}  

	@Override
	public void completed(Integer result, ByteBuffer buffer) {  
		if (buffer.hasRemaining())  
			//如果没有发送完，就继续发送直到完成  
			serverSocketChannel.write(buffer, buffer, this);  
		else{  
			ByteBuffer readBuffer = ByteBuffer.allocate(1024);  
			serverSocketChannel.read(readBuffer, readBuffer, new ServerReadHandler(serverSocketChannel));  
		}  
	}  

	@Override  
	public void failed(Throwable e, ByteBuffer attachment) {  
		LOG.error("write failed, " + e.getMessage());
		IOUtils.closeQuietly(serverSocketChannel); 
	}  
}
