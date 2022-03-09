package io.github.shanhm1991.echo.lang.io.aio.future;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * 
 * @author shanhm1991
 *
 */
@Slf4j
public class AioClient extends Thread {

	private final AsynchronousSocketChannel socketChannel;

	private final String host;

	private final int port;

	private int msg_index = 0;

	public AioClient(String host, int port) throws IOException { 
		this.host = host;
		this.port = port;
		this.socketChannel = AsynchronousSocketChannel.open();
		this.setName("client");
	}

	@Override
	public void run(){
		Void isConnect;
		try {
			isConnect = socketChannel.connect(new InetSocketAddress(host, port)).get();//阻塞
		} catch (Exception e) {
			log.error("连接异常，" + e.getMessage());
			IOUtils.closeQuietly(socketChannel); 
			return;
		} 
		if(!(isConnect == null)){
			IOUtils.closeQuietly(socketChannel);
			log.error("连接失败");
		}

		log.info("连接成功"); // 返回null表示连接成功
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		try {
			while (!interrupted()) {
				String msg = "msg" + ++msg_index;
				byte[] bytes = msg.getBytes();
				buffer.clear();
				buffer.put(bytes);
				buffer.flip();
				log.info(">> send " + msg);
				socketChannel.write(buffer).get();

				buffer.clear();
				socketChannel.read(buffer).get();
				buffer.flip();
				bytes = new byte[buffer.remaining()];
				buffer.get(bytes);
				String resp = new String(bytes);
				log.info("<< " + resp);
			}
		}catch (Exception e) {
			log.error("client stopped, " + e.getMessage()); 
			IOUtils.closeQuietly(socketChannel); 
		}
	}
	
	public void shutdown() {
		log.info("关闭客户端，停止发送...");
		interrupt(); 
		
		log.info("断开连接...");
		IOUtils.closeQuietly(socketChannel);
	}

	public static void main(String[] args) throws InterruptedException, IOException {
		AioClient client = new AioClient("127.0.0.1", 7070);
		client.start();
		
		Thread.sleep(5000);
		client.shutdown();
	}
}
