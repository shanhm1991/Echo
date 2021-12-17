package io.github.echo.io.aio.callback;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

import org.apache.log4j.Logger;

import io.github.echo.io.bio.msg.Request;
import io.github.echo.io.bio.msg.Request1;


public class AioClient extends Thread {

	private static final Logger LOG = Logger.getLogger(AioServer.class);

	private String host;

	private int port;

	private AsynchronousSocketChannel channel;

	public AioClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	@Override
	public void run() {
		try {
			channel = AsynchronousSocketChannel.open();
			channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
		} catch (IOException e) {
			LOG.error("客户端开启异常：" + e.getMessage());
			return;
		}

		//异步连接
		channel.connect(new InetSocketAddress(host, port), this, new ClientConnectHandler());

		//循环发送消息
		while(true){
			Request request = new Request1();
			request.init(this.getName()); 
			LOG.info("发送请求：" + request.getMsg());
			
			
			byte[] req = request.getBytes();
			ByteBuffer buffer = ByteBuffer.allocate(req.length);
			buffer.put(req);
			buffer.flip();
			
			channel.write(buffer, buffer, new ClientWriteHandler(channel));
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * @测试
	 */
	public static void main(String[] args) { 
		AioClient client = new AioClient("127.0.0.1", 8080);
		client.start();
	}
}