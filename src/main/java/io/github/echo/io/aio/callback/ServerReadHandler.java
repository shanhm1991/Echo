package io.github.echo.io.aio.callback;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.apache.log4j.Logger;

import io.github.echo.io.Calculator;
import io.github.echo.io.Util;
import io.github.echo.io.bio.msg.Request;

public class ServerReadHandler implements CompletionHandler<Integer, ByteBuffer> {

	private static final Logger LOG = Logger.getLogger(ServerReadHandler.class);

	private AsynchronousSocketChannel channel; 

	public ServerReadHandler(AsynchronousSocketChannel channel) {  
		this.channel = channel;  
	}  
	@Override  
	public void completed(Integer result, ByteBuffer buffer) {  
//		long begin = System.currentTimeMillis();
		buffer.flip();
		byte[] head = new byte[56];  
		buffer.get(head);  
		Request request = null;
		try{
			//解码消息头信息
			int bodyLnegth = Util.decodeInt(head, 2);
			String requestClzz = Util.decodeString(head, 6, 50);
			@SuppressWarnings("unchecked")
			Class<? extends Request> requestClass = (Class<? extends Request>) Class.forName(requestClzz);
			request = requestClass.newInstance();
			
			//读取消息体
			byte[] body = new byte[bodyLnegth]; //其实就是buffer.remaining()
			buffer.get(body);
			
			//解密，解码消息体
			request.decode(Util.RSADecode(body));
			
			//返回响应
			String response = String.valueOf(Calculator.conversion(request.getMsg()));
			
			System.out.println(response); 
			
//			//读取消息体
//			buffer.flip();
//			byte[] body = new byte[buffer.remaining()]; 
//			buffer.get(body);
//
//			//解密，解码消息体
//			request.decode(Util.RSADecode(body));
//
//			String response = String.valueOf(Calculator.conversion(request.getMsg()));
//
//			byte[] bytes = response.getBytes(); 
//			byte[] ss = new byte[10];
//			System.arraycopy(bytes, 0, ss, 0, bytes.length);
//
//			ByteBuffer writeBuffer = ByteBuffer.allocate(10);  
//			writeBuffer.put(ss);  
//			writeBuffer.flip();  
//
//
//			LOG.info("返回响应: " + response + "，处理耗时：" + (System.currentTimeMillis() - begin)); 
//			channel.write(writeBuffer, writeBuffer,new ServerWriteHandler(channel)); 
		}catch(Exception e){
			e.printStackTrace();
		}


	}  

	@Override  
	public void failed(Throwable e, ByteBuffer attachment) {  
		LOG.error(e.getMessage());
		Util.close(channel);
	}  
}
