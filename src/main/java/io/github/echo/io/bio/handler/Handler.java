package io.github.echo.io.bio.handler;

import java.io.PrintWriter;
import java.net.Socket;
import java.security.SecureRandom;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * 
 * @author shanhm1991
 *
 */
public class Handler implements Runnable {

	private static final Logger LOG = Logger.getLogger(Handler.class);

	private static SecureRandom random = new SecureRandom();

	private final Socket socket;

	private final String msg;

	public Handler(Socket socket, String msg){
		this.socket = socket;
		this.msg = msg;
	}

	@Override
	public void run() {
		long beginTime = System.currentTimeMillis();
		try(PrintWriter out = new PrintWriter(socket.getOutputStream(), true)){
			LOG.info(">>：" + msg);
			Thread.sleep(random.nextInt(1000)); // 模拟处理耗时

			String response = "resp for " + msg;
			out.println(response);
			LOG.info("<<：" + response 
					+ " , cost=" + (System.currentTimeMillis() - beginTime) + "ms");
		} catch (InterruptedException e) {
			LOG.warn("中断处理，保存或丢弃消息：" + cancel()); 
		} catch (Exception e) {
			LOG.error("处理异常，保存或丢弃消息：" + cancel()); 
		} finally {
			IOUtils.closeQuietly(socket);  // 对于短连接， 比如Htpp，直接在响应后关闭连接 
		}
	}

	public String cancel() {
		IOUtils.closeQuietly(socket); 
		return msg;
	}
}