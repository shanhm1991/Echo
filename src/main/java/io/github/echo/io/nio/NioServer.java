package io.github.echo.io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import io.github.echo.io.Calculator;
import io.github.echo.io.Util;
import io.github.echo.io.bio.msg.Request;
import io.github.echo.io.bio.msg.Response1;

/**
 * 
 * @author shanhm1991
 *
 */
public class NioServer extends Thread {

	private static final Logger LOG = Logger.getLogger(NioServer.class);

	private static SecureRandom random = new SecureRandom();

	private static ThreadPoolExecutor exec = new ThreadPoolExecutor(10,10,10,TimeUnit.SECONDS,
			new LinkedBlockingDeque<Runnable>(100),new HandlerFactory());

	private int port;

	private Selector selector;

	private ServerSocketChannel serverChannel;

	public NioServer(int port) {
		this.port = port;
		this.setName("accepter");
	}

	@Override
	public void run() {
		try {
			selector = Selector.open();
			serverChannel = ServerSocketChannel.open();
			serverChannel.configureBlocking(false);
			serverChannel.socket().bind(new InetSocketAddress(port), 1024);
			/**
			 * If the selector detects that the corresponding server-socket channel 
			 * is ready to accept another connection,or has an error pending,
			 * then it will add OP_ACCEPT to the key's ready set and add the key to its selected-key set. 
			 */
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
			LOG.info("启动服务端口：" + port);
		} catch (IOException e) {
			LOG.error("启动服务失败",e);
			Util.close(selector);
			return;
		}

		/**
		 * Tests whether the current thread has been interrupted.
		 *  The interrupted status of the thread is cleared by this method. In other words, 
		 *  if this method were to be called twice in succession, the second call would return false 
		 *  (unless the current thread were interrupted again, 
		 *  after the first call had cleared its interrupted status 
		 *  and before the second call had examined it).
		 */
		while (!interrupted()) {
			try {
				selector.select();
			} catch (IOException e) {
				LOG.error("服务异常关闭",e);
				break;
			} catch (ClosedSelectorException e) { 
				LOG.warn("服务已经关闭");
				break;
			}
			for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext();) {
				SelectionKey key = it.next();
				it.remove();

				Request request = null;
				try {
					if (!key.isValid()) {
						continue;
					}

					//Tests whether this key's channel is ready to accept a new socket connection
					if (key.isAcceptable()) {
						ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
						SocketChannel channel = serverChannel.accept();
						channel.configureBlocking(false);
						/**
						 *  If the selector detects that the corresponding channel is ready for reading, 
						 *  has reached end-of-stream, has been remotely shut down for further reading, or has an error pending, 
						 *  then it will add OP_READ to the key's ready-operation set and add the key to its selected-key set
						 */
						channel.register(selector, SelectionKey.OP_READ); 
					} 

					//Tests whether this key's channel is ready for reading
					if (key.isReadable()) {
						SocketChannel channel = (SocketChannel) key.channel();
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
							request = requestClass.newInstance();

							//读取消息体
							ByteBuffer bodyBuffer = ByteBuffer.allocate(bodyLnegth);
							channel.read(bodyBuffer);//这里认为一定大于0

							bodyBuffer.flip();
							byte[] bodyArray = new byte[bodyBuffer.remaining()];
							bodyBuffer.get(bodyArray);
							//解密，解码消息体
							request.decode(Util.RSADecode(bodyArray));
							//交给对应的handler处理
							exec.submit(new Handler(channel,request));
						}
					}
				} catch(RejectedExecutionException e){
					LOG.error("丢弃" + request.getName() + "的请求:" + request.getMsg()); 
				} catch (Exception e) {
					LOG.error("接收异常，",e); 
				}
			}
		}
	}

	private class Handler extends Thread {

		private SocketChannel channel;

		private Request request;

		public Handler(SocketChannel channel, Request request){
			this.channel = channel;
			this.request = request;
		}

		@Override
		public void run(){
			long begin = System.currentTimeMillis();
			LOG.info("处理" + request.getName() + "的请求" + request.getMsg()); 
			
			try {
				String result = String.valueOf(Calculator.conversion(request.getMsg()));
				Response1 response = new Response1();
				response.init(request.getName());
				response.setMsg(result); 
				byte[] bytes = response.getBytes();
				ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
				buffer.put(bytes);
				buffer.flip();
				
				Thread.sleep(random.nextInt(10));
				
				channel.write(buffer);
				LOG.info("返回" + request.getName() + "的响应:" + result + ",处理耗时：" + (System.currentTimeMillis() - begin));
			} catch (IOException e) {
				LOG.error("任务返回失败，" + e.getMessage()); 
			} catch (InterruptedException e) {
				//忽略
			} catch (Exception e){
				LOG.error("任务异常结束", e); 
			}
		}
	}

	private static class HandlerFactory implements ThreadFactory {
		private AtomicInteger index = new AtomicInteger(0);

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "handler-" + index.incrementAndGet());
		}
	}

	@Override
	public void interrupt(){
		LOG.info("关闭服务，停止接收请求...");
		super.interrupt(); //中断accepter

		LOG.info("关闭任务线程池");
		exec.shutdown();

		// main线程等待200ms后关闭selector，给需要返回响应的任务一点时间
		try {
			Thread.sleep(200);
		} catch (InterruptedException e1) {
			//忽略
		}

		LOG.info("断开连接...");
		try {
			selector.close();
		} catch (IOException e){
			LOG.error(e);
		}
	}

	/**
	 * @测试 启动服务10秒后关闭
	 */
	public static void main(String[] args) throws InterruptedException {
		NioServer server = new NioServer(8080);
		server.start();

		Thread.sleep(10000);
		server.interrupt();
	}
}