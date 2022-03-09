package io.github.shanhm1991.echo.lang.io.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * @author shanhm1991
 *
 */
@Slf4j
public class NioClient {

	private final SecureRandom random = new SecureRandom();

	private final AtomicInteger msg_index = new AtomicInteger(0);

	private final Selector selector;

	private final int port;

	private final String host;

	public NioClient(String host, int port) throws IOException{ 
		this.host = host;
		this.port = port;
		this.selector = Selector.open();
	}

	public void start() {
		for(int i = 1; i <= 10; i++){
			new Sender(i).start();
		}
		new Accpeter().start();
	}

	private class Sender extends Thread {

		public Sender(int index){
			this.setName("client-sender-" + index); 
		}

		@Override
		public void run() {
			try{
				while (!interrupted()) {
					// 这里先同步进行连接，当然也可以通过SelectionKey.OP_CONNECT去监听异步连接
					SocketChannel socketChannel = SocketChannel.open();
					socketChannel.configureBlocking(true);
					socketChannel.connect(new InetSocketAddress(host, port));
					// 然后再设置为异步，进行消息发送
					socketChannel.configureBlocking(false); 

					String msg = "msg" + msg_index.incrementAndGet();
					byte[] bytes = msg.getBytes();
					ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
					buffer.put(bytes);
					buffer.flip();

					log.info(">> send " + msg);
					socketChannel.write(buffer); // 写入通道，并注册读事件
					socketChannel.register(selector, SelectionKey.OP_READ);
					
					sleep(random.nextInt(1000));
				}
			}catch(Exception e){
				log.error("sender stopped, " + e.getMessage()); 
			}
		}
	}

	private class Accpeter extends Thread {

		public Accpeter(){
			this.setName("client-accpeter");  
			this.setDaemon(true);
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
								log.info("<< " + resp);
							}
						}
					}
				}
			}catch(Exception e){
				log.error("accpeter stopped, " + e.getMessage()); 
			}
		}
	}

	public static void main(final String[] args) throws IOException {
		new NioClient("127.0.0.1", 8080).start();
	}
}
