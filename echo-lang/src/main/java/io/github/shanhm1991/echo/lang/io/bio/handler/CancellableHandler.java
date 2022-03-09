package io.github.shanhm1991.echo.lang.io.bio.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.PrintWriter;
import java.net.Socket;
import java.security.SecureRandom;

/**
 * 
 * @author shanhm1991
 *
 */
@Slf4j
public class CancellableHandler implements Runnable {

	private static final SecureRandom random = new SecureRandom();

	private final Socket socket;

	private final String msg;

	public CancellableHandler(Socket socket, String msg){
		this.socket = socket;
		this.msg = msg;
	}

	@Override
	public void run() {
		long beginTime = System.currentTimeMillis();
		try(PrintWriter out = new PrintWriter(socket.getOutputStream(), true)){
			log.info(">>：" + msg);
			Thread.sleep(random.nextInt(1000)); // 模拟处理耗时

			String response = "resp for " + msg;
			out.println(response);
			log.info("<<：" + response 
					+ " , cost=" + (System.currentTimeMillis() - beginTime) + "ms");
		} catch (InterruptedException e) {
			log.warn("中断处理，保存或丢弃消息：" + cancel()); 
		} catch (Exception e) {
			log.error("处理异常，保存或丢弃消息：" + cancel()); 
		} finally {
			IOUtils.closeQuietly(socket);  // 对于短连接， 比如Htpp，直接在响应后关闭连接 
		}
	}

	public String cancel() {
		IOUtils.closeQuietly(socket); 
		return msg;
	}
}