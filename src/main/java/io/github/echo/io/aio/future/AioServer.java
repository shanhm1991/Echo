package io.github.echo.io.aio.future;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.security.SecureRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * 
 * @author shanhm1991
 *
 */
public class AioServer extends Thread{ 

	private static final Logger LOG = Logger.getLogger(AioServer.class);
	
	private static SecureRandom random = new SecureRandom();

	private final AsynchronousServerSocketChannel serverSocketChannel;

	public AioServer(int port) throws IOException{ 
		ExecutorService threadPool = new ThreadPoolExecutor(
				0, 10, 5000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(threadPool);
		this.serverSocketChannel = AsynchronousServerSocketChannel.open(group);
		this.serverSocketChannel.bind(new InetSocketAddress(port));
		this.setName("server"); 
	}

	@Override
	public void run(){
		LOG.info("启动监听...");
		try{
			while (!interrupted()) {
				AsynchronousSocketChannel socketChannel = serverSocketChannel.accept().get(); // 同步等待消息
				new Handler(socketChannel).start(); // 这里只有一个连接，所以没有用线程池，不过这里是长链接
			}
		}catch(Exception e){
			LOG.error("server stopped, " + e.getMessage()); 
			return;
		}
	}
	
	private class Handler extends Thread {
		
		private final AsynchronousSocketChannel socketChannel;
		
		public Handler(AsynchronousSocketChannel socketChannel){
			this.socketChannel = socketChannel;
			this.setName("handler"); 
		}
		
		@Override
		public void run() {
			ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
			try{
				while (!interrupted()) {
					buffer.clear();
					socketChannel.read(buffer).get(); // 同步读取
					buffer.flip();
					byte[] bytes = new byte[buffer.remaining()];
					buffer.get(bytes);
					String msg = new String(bytes);
					LOG.info(">> accept " + msg); 
					
					long beginTime = System.currentTimeMillis();
					Thread.sleep(random.nextInt(100));
					
					String response = "resp for " + msg;
					bytes = response.getBytes();
					buffer.clear();
					buffer.put(bytes);
					buffer.flip(); 
					socketChannel.write(buffer).get(); // 这里没有同步等待
					LOG.info("<< " + response + ", cost=" + (System.currentTimeMillis() - beginTime) + "ms");
				}
			}catch(Exception e){
				LOG.error("handle error, " + e.getMessage());
				shutdown();
			}
		}
	}
	
	public void shutdown(){
		LOG.info("关闭服务，停止接收请求...");
		interrupt(); //中断accepter

		LOG.info("断开连接...");
		IOUtils.closeQuietly(serverSocketChannel);
	}

	public static void main(String[] args) throws IOException, InterruptedException { 
		AioServer server = new AioServer(7070);
		server.start();
	}
}
