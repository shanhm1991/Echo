package io.github.echo.io.aio.future;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import io.github.echo.io.Util;

/**
 * 
 * @author shanhm1991
 *
 */
public class AioClient extends Thread{

	private static final Logger LOG = Logger.getLogger(AioClient.class);

	private static volatile AtomicInteger client_index = new AtomicInteger(0);

	private String host;

	private int port;

	private AsynchronousSocketChannel socketChannel;

	public AioClient(String host, int port) {
		this.host = host;
		this.port = port;
		this.setName("client-" + client_index.incrementAndGet());
	}

	//连接线程
	@Override
	public void run(){
		try{
			socketChannel = AsynchronousSocketChannel.open();
		} catch (IOException e) {
			LOG.error("启动异常，" + e.getMessage()); 
			return;
		}
		if (!socketChannel.isOpen()) {
			LOG.error("启动失败");
			return;
		}

		try {
			Void isConnect = socketChannel.connect(new InetSocketAddress(host, port)).get();//阻塞
			//返回null表示连接成功
			if(!(isConnect == null)){
				Util.close(socketChannel); 
				LOG.error("连接服务失败");
			}else{
				LOG.info("连接服务成功");
			}
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("连接服务异常，" + e.getMessage());
			Util.close(socketChannel); 
			return;
		}

		while(true){
			String request = Util.buildMsg();
			byte[] req = request.getBytes();
			ByteBuffer buffer = ByteBuffer.allocate(req.length);
			buffer.put(req);
			buffer.flip();
			try {
				int s = socketChannel.write(buffer).get();
				LOG.info(s + "发送请求：" + request);
			} catch (InterruptedException e) {
				LOG.warn("发送请求中断"); 
				Util.close(socketChannel); 
				return;
			} catch (ExecutionException e) {
				LOG.error("发送请求异常,可能是服务关闭了，" + e.getMessage()); 
				Util.close(socketChannel); 
				return;
			}

			buffer = ByteBuffer.allocateDirect(1024);
			try {
				
				socketChannel.read(buffer).get();
			} catch (InterruptedException e) {
				LOG.warn("接收响应中断"); 
				Util.close(socketChannel); 
				return;
			} catch (ExecutionException e) {
				LOG.warn("接收响应异常," + e.getMessage()); 
				Util.close(socketChannel); 
				return;
			}

			buffer.flip();
			CharBuffer decode = Charset.defaultCharset().decode(buffer);
			LOG.info("收到响应：" + decode.toString());
		}
	}

	public static void main(String[] args) throws InterruptedException {
		for(int i = 0;i < 1;i++){
			new AioClient("127.0.0.1", 7070).start();
		}
	}
}
