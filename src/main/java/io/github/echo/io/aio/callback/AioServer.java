package io.github.echo.io.aio.callback;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import io.github.echo.io.Util;


public class AioServer extends Thread {

	private static final Logger LOG = Logger.getLogger(AioServer.class);

	AsynchronousServerSocketChannel serverChannel;
	
	private ServerAcceptHandler acceptHandler = new ServerAcceptHandler();

	public AioServer() {
		this.setName("server[8080]"); 
	}

	@Override
	public void run() {
		ExecutorService threadPool = Executors.newFixedThreadPool(20);
		try {
			AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(threadPool);
			
			serverChannel = AsynchronousServerSocketChannel.open(group);
			serverChannel.bind(new InetSocketAddress(8080));
			serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			LOG.info("服务启动...");
		} catch (IOException e) {
			LOG.error("服务启动异常," + e.getMessage());
		}
		
		serverChannel.accept(this, acceptHandler);
	}

	public void shutDown(){
		LOG.info("断开连接，关闭服务"); 
		Util.close(serverChannel); 
	}

	/**
	 * @测试
	 */
	public static void main(String[] args) throws InterruptedException {
		AioServer server = new AioServer();
		server.start();

		Thread.sleep(20000);
		server.shutDown();
	}
}