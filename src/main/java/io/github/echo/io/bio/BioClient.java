package io.github.echo.io.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import io.github.echo.io.Util;
import io.github.echo.io.bio.msg.Request;
import io.github.echo.io.bio.msg.Request1;
import io.github.echo.io.bio.msg.Request2;

/**
 * 
 * @author shanhm1991
 *
 */
public class BioClient extends Thread {

	private static final Logger LOG = Logger.getLogger(BioClient.class);
	
	private static SecureRandom random = new SecureRandom();

	/**
	 * 所有运行着的client的计数
	 */
	private static volatile AtomicInteger clientRunningNum = new AtomicInteger(0);

	/**
	 * 到达阻塞点准备好发请求的client的计数
	 */
	private static volatile AtomicInteger clientReadyNum = new AtomicInteger(0);

	private static volatile AtomicInteger client_index = new AtomicInteger(0);
	
	private int index = client_index.incrementAndGet();

	private static Object lock = new Object();

	private Socket socket;

	private String host;

	private int port;

	public BioClient(String host, int port) {
		this.host = host;
		this.port = port;
		this.setName("client-" + index);
	}

	@Override
	public void run() {
		clientRunningNum.incrementAndGet();
		while (true) {
			if (!isConnectionAlive()) {
				try {
					socket = new Socket(host, port);
				} catch (IOException e) {
					LOG.error(" 连接服务异常:" + e.getMessage());
					shutdown();
					return;
				}
			}

			BufferedReader in = null;
			OutputStream out = null;
			//为了体现不同消息的不同处理
			Request requst = null;
			if(index % 2 == 0){
				requst = new Request1();
				requst.init(this.getName());
			}else{
				requst = new Request2();
				requst.init(this.getName());
			}
			
			try {
				out = socket.getOutputStream();
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				//模拟并发场景，设置阻塞点，每次等所有客户端都到达后一起发送消息。
				clientReadyNum.incrementAndGet();
				synchronized (lock) {
					while (clientReadyNum.get() % clientRunningNum.get() != 0) {
						lock.wait();
					}
					clientReadyNum.set(0);
					lock.notifyAll();
				}
				LOG.info("发送请求：" + requst.getMsg());
				out.write(requst.getBytes());

				String response = in.readLine();
				LOG.info("收到响应：" + response);
			} catch (InterruptedException e) {
				shutdown(); // 在等待锁lock的时候可以响应中断 
				return;
			} catch (IOException e) {
				LOG.error("异常中断：" + e.getMessage());
				shutdown();  
				return;
			} catch (Exception e) {
				LOG.error("消息编码异常：", e);
			} finally {
				Util.close(socket);
			}

			try {
				sleep(random.nextInt(10));
			} catch (InterruptedException e) {
				shutdown(); 
				break;
			}
		}
	}

	/**
	 * 客户端关闭，注意要重设阻塞条件，且通知其他client不需要再等待自己了
	 */
	public void shutdown() {
		Util.close(socket);
		clientRunningNum.decrementAndGet();

		LOG.warn("关闭自己 " + "Ready/Running：" + clientReadyNum.get() + "/" + clientRunningNum.get());
		synchronized (lock) {
			if (clientRunningNum.get() == 0 || clientReadyNum.get() % clientRunningNum.get() == 0) {
				lock.notifyAll();
			}
		}
	}

	/**
	 * 检测socket连接
	 */
	private boolean isConnectionAlive() {
		if (socket == null) {
			return false;
		}
		try {
			socket.sendUrgentData(0xFF);
			return true;
		} catch (Exception e) {
			Util.close(socket);
			return false;
		}
	}

	/**
	 * @测试 启动20个client，并发请求
	 */
	public static void main(String[] args) {
		for (int i = 1; i <= 20; i++) {
			new BioClient("127.0.0.1", 4040).start();
		}
	}
}