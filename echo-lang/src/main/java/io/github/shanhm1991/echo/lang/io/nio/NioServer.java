package io.github.shanhm1991.echo.lang.io.nio;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.SecureRandom;
import java.util.Iterator;

/**
 * 
 * @author shanhm1991
 *
 */
@Slf4j
public class NioServer extends Thread {

	private static final SecureRandom random = new SecureRandom();

	private final Selector selector;

	public NioServer(int port) throws IOException {
		this.selector = Selector.open();
		
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);
		serverChannel.socket().bind(new InetSocketAddress(port));
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		this.setName("server-accepter");
	}

	@Override
	public void run() {
		log.info("启动监听...");
		try {
			while (!interrupted()) {
				selector.select();
				for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext();) {
					SelectionKey key = it.next();
					it.remove();
					if (!key.isValid()) {
						continue;
					}

					if (key.isAcceptable()) {
						ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
						SocketChannel channel = serverChannel.accept();
						channel.configureBlocking(false);
						channel.register(selector, SelectionKey.OP_READ); 
					} 

					if (key.isReadable()) {
						SocketChannel channel = (SocketChannel) key.channel();
						ByteBuffer buffer = ByteBuffer.allocate(1024);
						if(channel.read(buffer) > 0){
							buffer.flip();
							byte[] bytes = new byte[buffer.remaining()];
							buffer.get(bytes);

							String msg = new String(bytes);
							new Handler(channel, msg).start();
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("接收异常，",e); 
		}
	}

	private static class Handler extends Thread {

		private final SocketChannel channel;

		private final String msg;

		public Handler(SocketChannel channel, String msg){
			this.channel = channel;
			this.msg = msg;
		}

		@Override
		public void run(){
			log.info(">> recv " + msg); 
			
			long beginTime = System.currentTimeMillis();
			try {
				String response = "resp for " + msg;
				byte[] bytes = response.getBytes();
				ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
				buffer.put(bytes);
				buffer.flip();

				Thread.sleep(random.nextInt(100));
				channel.write(buffer); // 这里假设是短消息一次写完
				log.info("<< " + response + ", cost=" + (System.currentTimeMillis() - beginTime) + "ms");
			} catch (Exception e){
				log.error("处理异常", e); 
			}finally{
				IOUtils.closeQuietly(channel); 
			}
		}
	}

	public void shutdown(){
		log.info("关闭服务，停止接收请求...");
		interrupt(); //中断accepter
		
		log.info("断开连接...");
		IOUtils.closeQuietly(selector);
	}

	public static void main(String[] args) throws InterruptedException, IOException { 
		NioServer server = new NioServer(8080);
		server.start();

		Thread.sleep(5000);
		server.shutdown();
	}
}