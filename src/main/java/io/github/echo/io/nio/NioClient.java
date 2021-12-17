package io.github.echo.io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import io.github.echo.io.Util;
import io.github.echo.io.bio.msg.Request;
import io.github.echo.io.bio.msg.Request1;

/**
 * 
 * @author shanhm1991
 *
 */
public class NioClient {

	private static final Logger LOG = Logger.getLogger(NioClient.class);

	private static volatile AtomicInteger client_index = new AtomicInteger(0);
	
	private static SecureRandom random = new SecureRandom();

	private int port;

	private String host;

	private Selector selector;

	private SocketChannel socketChannel; 

	private CountDownLatch acceptLatch = new CountDownLatch(1);

	private CountDownLatch sendLatch = new CountDownLatch(1);

	private int index = client_index.incrementAndGet();
	
	private Connector connector = new Connector();
	
	private Accepter accepter = new Accepter();
	
	private List<Sender> senderList = new ArrayList<Sender>();

	public NioClient(String host, int port, int senderNum){
		this.host = host;
		this.port = port;
		
		connector.start();
		accepter.start();
		for(int i = 1;i <= senderNum;i++){
			Sender sender = new Sender(i);
			sender.start();
			senderList.add(sender);
		}
	}

	private class Connector extends Thread {

		public Connector(){
			this.setName("client[" + index + "]-connector");
		}

		@Override
		public void run(){
			try {
				selector = Selector.open();
				socketChannel = SocketChannel.open();
				socketChannel.configureBlocking(false);

				//异步连接服务，结果立即返回,
				if (socketChannel.connect(new InetSocketAddress(host, port))) {
					LOG.info("连接服务成功");
					acceptLatch.countDown(); //放开Accepter和Sender
					sendLatch.countDown();
				} else {
					/**
					 * 如果没有立即成功，就注册一个SelectionKey.OP_CONNECT事件，由Accepter去监听
					 * If the selector detects that the corresponding socket channel is ready to complete its connection sequence, 
					 * or has an error pending, then it will add OP_CONNECT to the key's ready set and add the key to its selected-key set. 
					 */
					socketChannel.register(selector, SelectionKey.OP_CONNECT);
					acceptLatch.countDown(); //只放开Accepter
				}
			} catch (IOException e) {
				LOG.error("连接异常失败:" + e.getMessage());
				accepter.interrupt();
				for(Sender sender : senderList){
					sender.interrupt();
				}
				Util.close(selector);
			}
		}
	}

	private class Accepter extends Thread {

		public Accepter(){
			this.setName("client[" + index + "]-accepter");
		}

		@Override
		public void run(){

			try {
				acceptLatch.await();
			} catch (InterruptedException e1) {
				LOG.warn("连接服务失败，中断消息接收");
				return;
			}

			while (!interrupted()) {
				Request response = null;
				try {
					selector.select();
					for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext();) {
						SelectionKey key = it.next();
						it.remove();
						if (!key.isValid()) {
							continue;
						}
						SocketChannel channel = (SocketChannel) key.channel();
						
						//Tests whether this key's channel has either finished, or failed to finish
						if (key.isConnectable()) {
							if (channel.finishConnect()) {
								LOG.info("连接服务成功");
								sendLatch.countDown();
							}else{
								LOG.error("连接服务失败"); 
								for(Sender sender : senderList){
									sender.interrupt();
								}
								Util.close(selector);
								return;
							}
						}

						//Tests whether this key's channel is ready for reading. 
						if(key.isReadable()){
							//首先尝试读取消息头
							ByteBuffer headBuffer = ByteBuffer.allocate(56);
							if(channel.read(headBuffer) > 0){
								headBuffer.flip();
								byte[] headArray = new byte[headBuffer.remaining()];
								headBuffer.get(headArray);

								//解码消息头信息
								int bodyLnegth = Util.decodeInt(headArray, 2);
								String requestClzz = Util.decodeString(headArray, 6, 50);
								@SuppressWarnings("unchecked")
								Class<? extends Request> requestClass = (Class<? extends Request>) Class.forName(requestClzz);
								response = requestClass.newInstance();

								//读取消息体
								ByteBuffer bodyBuffer = ByteBuffer.allocate(bodyLnegth);
								channel.read(bodyBuffer);//这里认为一定大于0

								bodyBuffer.flip();
								byte[] bodyArray = new byte[bodyBuffer.remaining()];
								bodyBuffer.get(bodyArray);
								//解密，解码消息体
								response.decode(Util.RSADecode(bodyArray));
								
								//根据信息将响应返回给当初发消息的业务模块，这里直接打印就当收到了
								LOG.info("收到" + response.getName() + "的响应：" + response.getMsg()); 
							}
						}
					}
				} catch (IOException e) {
					// 这里简单的认为是服务关闭导致连接异常了，实际情况需要对异常做细分处理
					LOG.error("接收异常（可能是已服务关闭）," + e.getMessage());
					for(Sender sender : senderList){
						sender.interrupt();
					}
					Util.close(selector);
					return;
				} catch ( Exception e){
					LOG.error("响应解析异常," + e.getMessage());
				}
			}
		}
	}

	private class Sender extends Thread {

		public Sender(int i){
			this.setName("client[" + index + "]-sender[" + i + "]");
		}

		@Override
		public void run(){
			try {
				sendLatch.await();
			} catch (InterruptedException e1) {
				LOG.warn("连接服务失败，中断消息发送");
				return;
			}

			while(true){
				Request request = new Request1();
				request.init(this.getName()); 
				byte[] bytes = request.getBytes();
				ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
				buffer.put(bytes);
				buffer.flip();
				LOG.info("发送请求：" + request.getMsg());
				try {
					socketChannel.write(buffer);
					socketChannel.register(selector, SelectionKey.OP_READ);
				} catch (IOException e) {
					LOG.error("发送异常停止（可能是连接断开），" + e.getMessage()); 
					accepter.interrupt(); //当接受的异常断开小于10个时，会出问题
					return;
				} 
				try {
					sleep(random.nextInt(500));
				} catch (InterruptedException e1) {
					LOG.warn("收到中断信号，结束消息发送");
					return;
				}
			}
		}
	}

	/**
	 * @测试 启10个客户端，每个客户端中有多个线程发送请求
	 */
	public static void main(final String[] args) throws IOException {
		for(int i = 1;i <= 10;i++){
			new NioClient("127.0.0.1", 8080, i);
		}
	}
}
