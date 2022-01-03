package io.github.echo.io.bio;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.concurrent.CyclicBarrier;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * 
 * @author shanhm1991
 *
 */
public class BioClient extends Thread {

	private static final Logger LOG = Logger.getLogger(BioClient.class);
	
	private static final int CONCURRENCY = 10; //并发数
	
	private static CyclicBarrier barrier = new CyclicBarrier(CONCURRENCY);
	
	private static SecureRandom random = new SecureRandom();
	
	private final Socket socket;
	
	private final String msg;

	public BioClient(String host, int port, int index) throws Exception { 
		this.socket = new Socket(host, port);
		this.setName("client-" + index);
		this.msg = "Hi, i'm client-" + index;
	}

	@Override
	public void run() {
		while (true) {
			try(OutputStream out = socket.getOutputStream();
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))){
				sleep(random.nextInt(2000));
				barrier.await();

				LOG.info(">>：" + msg);
				out.write(msg.getBytes());
				String resp = in.readLine();
				LOG.info("<<：" + resp);
			}catch(Exception e){ 
				LOG.error("发送失败：" + e.getMessage());
				return;
			}finally {
				barrier.reset(); // 使其它client发生BrokenBarrierException而结束
				IOUtils.closeQuietly(socket); 
			}
		}
	} 

	/**
	 * @throws Exception  
	 * @测试 
	 */
	public static void main(String[] args) throws Exception {
		for (int i = 1; i <= 30; i++) {
			new BioClient("127.0.0.1", 4040, i).start();
		}
	}
}