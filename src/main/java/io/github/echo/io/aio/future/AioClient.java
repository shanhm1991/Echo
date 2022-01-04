package io.github.echo.io.aio.future;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * 
 * @author shanhm1991
 *
 */
public class AioClient extends Thread {

	private static final Logger LOG = Logger.getLogger(AioClient.class);

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
		Void isConnect = null;
		try {
			isConnect = socketChannel.connect(new InetSocketAddress(host, port)).get();//阻塞
		} catch (Exception e) {
			LOG.error("连接异常，" + e.getMessage());
			IOUtils.closeQuietly(socketChannel); 
			return;
		} 
		if(!(isConnect == null)){
			IOUtils.closeQuietly(socketChannel);
			LOG.error("连接失败");
		}

		LOG.info("连接成功"); // 返回null表示连接成功
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		try {
			while (!interrupted()) {
				String msg = "msg" + ++msg_index;
				byte[] bytes = msg.getBytes();
				buffer.clear();
				buffer.put(bytes);
				buffer.flip();
				LOG.info(">> send " + msg);
				socketChannel.write(buffer).get();

				buffer.clear();
				socketChannel.read(buffer).get();
				buffer.flip();
				bytes = new byte[buffer.remaining()];
				buffer.get(bytes);
				String resp = new String(bytes);
				LOG.info("<< " + resp);
			}
		}catch (Exception e) {
			LOG.error("client stopped, " + e.getMessage()); 
			IOUtils.closeQuietly(socketChannel); 
		}
	}
	
	public void shutdown() {
		LOG.info("关闭客户端，停止发送...");
		interrupt(); 
		
		LOG.info("断开连接...");
		IOUtils.closeQuietly(socketChannel);
	}

	public static void main(String[] args) throws InterruptedException, IOException {
		AioClient client = new AioClient("127.0.0.1", 7070);
		client.start();
		
		Thread.sleep(5000);
		client.shutdown();
	}
}
