package io.github.echo.io.aio.future;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import io.github.echo.io.Calculator;

/**
 * 
 * @author shanhm1991
 *
 */
public class AioServer extends Thread{ 

	private static final Logger LOG = Logger.getLogger(AioServer.class);

	private int port;

	private AsynchronousServerSocketChannel serverSocketChannel;
	
	private static ThreadPoolExecutor exec = new ThreadPoolExecutor(10,10,10,TimeUnit.SECONDS,
			new LinkedBlockingDeque<Runnable>(100),new HandlerFactory());

	public AioServer(int port){
		this.port = port;
	}

	@Override
	public void run(){
		try {
			serverSocketChannel = AsynchronousServerSocketChannel.open();
		} catch (IOException e) {
			LOG.error(e);
			return;
		}
		if (!serverSocketChannel.isOpen()) {
			LOG.error("服务启动失败");
			return;
		} 
		try {
			serverSocketChannel.bind(new InetSocketAddress(port));
		} catch (IOException e1) {
			LOG.error("服务启动失败",e1);
			return;
		}

		LOG.info("启动服务端口：" + port);
		while (true) {
			//返回的是future，不能重复调用
			Future<AsynchronousSocketChannel> socketChannelFuture = serverSocketChannel.accept();
			try {
				AsynchronousSocketChannel socketChannel = socketChannelFuture.get();

				exec.submit(new Handler(socketChannel));
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} 
		}
	}


	private class Handler extends Thread {

		private AsynchronousSocketChannel socketChannel;

		public Handler(AsynchronousSocketChannel socketChannel){
			this.socketChannel = socketChannel;
		}

		@Override
		public void run(){
				ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
				try {
					socketChannel.read(buffer).get();
				} catch (InterruptedException e) {
					e.printStackTrace();
					return;
				} catch (ExecutionException e) {
					e.printStackTrace();
					return;
				}
				buffer.flip();
				String request = Charset.defaultCharset().decode(buffer).toString();
				LOG.info("收到请求：" + request);

				String response = String.valueOf(Calculator.conversion(request));
				byte[] bytes = response.getBytes();  
				ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);  
				writeBuffer.put(bytes);  
				writeBuffer.flip();  
				LOG.info("返回响应：" + response);
				socketChannel.write(writeBuffer);

		}

	}
	
	private static class HandlerFactory implements ThreadFactory {
		private AtomicInteger index = new AtomicInteger(0);

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "handler-" + index.incrementAndGet());
		}
	}


	public static void main(String[] args) {
		AioServer server = new AioServer(7070);
		server.start();
	}
}
