package io.github.shanhm1991.echo.lang.io.bio;

import io.github.shanhm1991.echo.lang.io.bio.handler.CancellableExecutor;
import io.github.shanhm1991.echo.lang.io.bio.handler.CancellableFuture;
import io.github.shanhm1991.echo.lang.io.bio.handler.CancellableHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

/**
 * 
 * @author shanhm1991
 *
 */
@Slf4j
public class BioServer extends Thread {

	private final CancellableExecutor executor = new CancellableExecutor(5, 5, 1);

	private final ServerSocket serverSocket;

	public BioServer(int port) throws IOException {
		this.serverSocket = new ServerSocket(port);
		this.setName("server-accepter"); 
	}

	@Override
	public void run() {
		String msg = null;
		log.info("启动监听...");
		while (!interrupted()) {
			try {
				Socket socket = serverSocket.accept();
				InputStream input = socket.getInputStream();
				
				// 根据约定的消息协议和编解码，对读取的字节码进行解析，这里简单消息简单处理
				byte[] bytes = new byte[1024];
				input.read(bytes);
				msg = new String(bytes);
				
				// 根据协议选择对应的Handler进行处理，这里还是简单处理
				executor.submit(new CancellableHandler(socket, msg));
			} catch (RejectedExecutionException e){
				if(isInterrupted()){
					log.info("服务已经被关闭，保存或丢弃接收的消息：" + msg);
					return;
				}
				log.warn("请求超过服务负载，保存或丢弃接收的消息：" + msg); 
			} catch (Exception e) {
				log.error("消息接收异常，" + msg , e); 
			} 
		}
	}

	public void shutdownNow() {
		log.info("关闭服务，停止接收请求...");
		interrupt();// 中断accepter

		log.info("关闭任务线程池，中断正在处理事的任务，以及还在等待处理的任务");
		List<Runnable> taskList = executor.shutdownNow();
		if (!taskList.isEmpty()) {
			for (Runnable task : taskList) { 
				CancellableFuture<?> future = (CancellableFuture<?>) task;
				future.cancel(true); // 关闭socket，保存消息
				log.warn("保存或丢弃未处理的消息：" + future.getMsg()); 
			}
		}
		
		log.info("断开连接...");
		IOUtils.closeQuietly(serverSocket); 
	}
	
	public static void main(String[] args) throws Exception { 
		BioServer server = new BioServer(4040);
		server.start();

		Thread.sleep(5000);
		server.shutdownNow();
	}
}