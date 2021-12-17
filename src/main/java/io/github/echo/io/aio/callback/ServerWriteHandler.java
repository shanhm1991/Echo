package io.github.echo.io.aio.callback;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.apache.log4j.Logger;

import io.github.echo.io.Util;

public class ServerWriteHandler implements CompletionHandler<Integer, ByteBuffer> {
	
	private static final Logger LOG = Logger.getLogger(ServerWriteHandler.class);

	private AsynchronousSocketChannel channel; 
	
	public ServerWriteHandler(AsynchronousSocketChannel channel) {  
		this.channel = channel;  
	}  

	@Override
	public void completed(Integer result, ByteBuffer buffer) {  
		if (buffer.hasRemaining())  
			//如果没有发送完，就继续发送直到完成  
			channel.write(buffer, buffer, this);  
		else{  
			ByteBuffer readBuffer = ByteBuffer.allocate(1024);  
			channel.read(readBuffer, readBuffer, new ServerReadHandler(channel));  
		}  
	}  

	@Override  
	public void failed(Throwable e, ByteBuffer attachment) {  
		LOG.error(e.getMessage());
		Util.close(channel);
	}  
}
