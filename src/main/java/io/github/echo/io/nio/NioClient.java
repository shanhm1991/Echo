package io.github.echo.io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

/**
 * 
 * @author shanhm1991
 *
 */
public class NioClient {

	private static final Logger LOG = Logger.getLogger(NioClient.class);
	
	private final SecureRandom random = new SecureRandom();

	private final AtomicInteger msg_index = new AtomicInteger(0);

	private final Selector selector;

	private final int port;

	private final String host;

	public NioClient(String host, int port, Selector selector) throws IOException{ 
		this.host = host;
		this.port = port;
		this.selector = selector;
	}

	public void start() throws IOException{
		for(int i = 1; i <= 10; i++){
			new Sender(i).start();
		}
		new Accpeter().start();
	}

	private class Sender extends Thread {

		private SocketChannel socketChannel; 

		public Sender(int index){
			this.setName("client-sender-" + index); 
		}

		@Override
		public void run() {
			try{
				// 这里先同步进行连接，当然也可以通过SelectionKey.OP_CONNECT去监听异步连接
				this.socketChannel = SocketChannel.open();
				this.socketChannel.configureBlocking(true);
				this.socketChannel.connect(new InetSocketAddress(host, port));

				// 然后再设置为异步，进行消息发送
				this.socketChannel.configureBlocking(false); 
				this.socketChannel.register(selector, SelectionKey.OP_WRITE);

				while (!interrupted()) {
					String msg = "msg" + msg_index.incrementAndGet();
					byte[] bytes = msg.getBytes();
					ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
					buffer.put(bytes);
					buffer.flip();

					LOG.info(">> send " + msg);
					socketChannel.write(buffer); // 写入通道，并注册读事件
					socketChannel.register(selector, SelectionKey.OP_READ);
					
					sleep(random.nextInt(1000));
				}
			}catch(Exception e){
				LOG.error("sender stopped, " + e.getMessage()); 
			}
		}
	}

	private class Accpeter extends Thread {

		public Accpeter(){
			this.setName("client-accpeter"); 
		}

		@Override
		public void run() {
			try{
				while (!interrupted()) {
					selector.select();
					for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext();) {
						SelectionKey key = it.next();
						if (!key.isValid()) {
							it.remove();
							continue;
						}

						SocketChannel channel = (SocketChannel) key.channel();
						if(key.isReadable()){
							it.remove();
							ByteBuffer buffer = ByteBuffer.allocate(1024);
							if(channel.read(buffer) > 0){
								buffer.flip();
								byte[] bytes = new byte[buffer.remaining()];
								buffer.get(bytes);

								String resp = new String(bytes);
								LOG.info("<< " + resp);
							}
						}
					}
				}
			}catch(Exception e){
				LOG.error("accpeter stopped, " + e.getMessage()); 
			}
		}
	}

	public static void main(final String[] args) throws IOException {
		Selector selector = Selector.open();
		new NioClient("127.0.0.1", 8080, selector).start();
	}
}
