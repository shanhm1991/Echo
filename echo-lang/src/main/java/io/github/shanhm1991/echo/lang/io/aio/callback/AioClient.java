package io.github.shanhm1991.echo.lang.io.aio.callback;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.CountDownLatch;

/**
 * 
 * @author shanhm1991
 *
 */
@Slf4j
public class AioClient extends Thread {

	private final AsynchronousSocketChannel socketchannel;
	
	private final CountDownLatch connectLatch = new CountDownLatch(1);
	
	private final String host;

	private final int port;

	private int msg_index = 0;

	public AioClient(String host, int port) throws IOException {
		this.host = host;
		this.port = port;
		this.socketchannel = AsynchronousSocketChannel.open();
		this.socketchannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
		this.setName("client"); 
	}

	@Override
	public void run() {
		socketchannel.connect(new InetSocketAddress(host, port), connectLatch, new ClientConnectHandler()); // 异步连接
		try{
			connectLatch.await();
			while (!interrupted()) {
				String msg = "msg" + ++msg_index;
				byte[] bytes = msg.getBytes();
				ByteBuffer buffer = ByteBuffer.allocate(1024); // 简单消息，这里申请个固定的缓存
				buffer.put(bytes);
				buffer.flip();

				
				log.info(">> send " + msg);
				socketchannel.write(buffer, buffer, new ClientWriteHandler(socketchannel)); // 异步写消息，然后异步读响应
				Thread.sleep(1000);
			}
		} catch (Exception e) {
			log.error("client stopped, " + e.getMessage()); 
		}
	}
	
	public void shutdown() {
		log.info("关闭客户端，停止发送...");
		interrupt(); 
		
		log.info("断开连接...");
		IOUtils.closeQuietly(socketchannel);
	}

	public static void main(String[] args) throws IOException, InterruptedException { 
		AioClient client = new AioClient("127.0.0.1", 8080);
		client.start();
		
		Thread.sleep(8000);
		client.shutdown();
	}
}