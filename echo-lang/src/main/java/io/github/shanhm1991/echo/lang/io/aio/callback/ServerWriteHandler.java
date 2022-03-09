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
public class ServerWriteHandler implements CompletionHandler<Integer, ByteBuffer> {
	
	private final AsynchronousSocketChannel serverSocketChannel; 
	
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
		log.error("write failed, " + e.getMessage());
		IOUtils.closeQuietly(serverSocketChannel); 
	}  
}
