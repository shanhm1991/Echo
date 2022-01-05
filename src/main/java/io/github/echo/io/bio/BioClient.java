package io.github.echo.io.bio;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * 
 * @author shanhm1991
 *
 */
public class BioClient extends Thread {

	private static final Logger LOG = Logger.getLogger(BioClient.class);
	
	private static AtomicInteger msg_index = new AtomicInteger(0);

	private static SecureRandom random = new SecureRandom();
	
	private final CyclicBarrier barrier;

	private final String host;

	private final int port;

	public BioClient(String host, int port, int index, CyclicBarrier barrier) throws Exception { 
		this.host = host;
		this.port = port;
		this.barrier = barrier;
		this.setName("client-" + index);
	}

	@Override
	public void run() {
		while (true) {
			Socket socket = null; // 短连接消息，每次消息都重新建立连接
			try {
				sleep(random.nextInt(1000));
				barrier.await(3000, TimeUnit.MILLISECONDS);
				socket = new Socket(host, port);
			} catch (Exception e) {
				LOG.error("连接失败：" + e.getMessage());
				return;
			}

			try(OutputStream out = socket.getOutputStream();
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))){
				String msg = "msg" + msg_index.incrementAndGet();
				LOG.info(">> send " + msg);
				out.write(msg.getBytes());
				String resp = in.readLine();
				LOG.info("<< " + resp);
			}catch(Exception e){ 
				LOG.error("发送失败：" + e.getMessage());
				return;
			}finally {
				IOUtils.closeQuietly(socket); 
			}
		}
	} 
	
	public static void main(String[] args) throws Exception { 
		CyclicBarrier barrier = new CyclicBarrier(10); // 模拟并发消息
		for (int i = 1; i <= 30; i++) {
			new BioClient("127.0.0.1", 4040, i, barrier).start();
		}
	}
}