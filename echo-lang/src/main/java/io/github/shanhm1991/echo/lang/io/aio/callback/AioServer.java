package io.github.shanhm1991.echo.lang.io.aio.callback;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author shanhm1991
 *
 */
@Slf4j
public class AioServer extends Thread {

	private final AsynchronousServerSocketChannel serverSocketChannel;

	private final ServerAcceptHandler acceptHandler = new ServerAcceptHandler();

	public AioServer() throws IOException {
		ExecutorService threadPool = new ThreadPoolExecutor(
				0, 10, 5000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
		AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(threadPool);
		serverSocketChannel = AsynchronousServerSocketChannel.open(group); // 指定任务线程组
		serverSocketChannel.bind(new InetSocketAddress(8080));
		serverSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		this.setName("server"); 
		log.info("启动监听..."); 
	}

	public AsynchronousServerSocketChannel getServerChannel() {
		return serverSocketChannel;
	}

	@Override
	public void run() {
		serverSocketChannel.accept(this, acceptHandler);
	}

	public void shutDown(){
		log.info("关闭服务，停止接收请求...");
		interrupt(); //中断accepter

		log.info("断开连接...");
		IOUtils.closeQuietly(serverSocketChannel);
	}

	public static void main(String[] args) throws InterruptedException, IOException {
		AioServer server = new AioServer();
		server.start();

		Thread.sleep(3000); // 只要客户端一直在发消息保证任务线程存活，服务就不会关闭
	}
}