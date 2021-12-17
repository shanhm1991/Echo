package io.github.echo.io.bio;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;

import org.apache.log4j.Logger;

import io.github.echo.io.Util;
import io.github.echo.io.bio.handler.ExtendExecutor;
import io.github.echo.io.bio.handler.Handler;
import io.github.echo.io.bio.msg.Request;
import io.github.echo.io.bio.msg.Request1;

/**
 * 
 * @author shanhm1991
 *
 */
public class BioServer extends Thread {

	private static final Logger LOG = Logger.getLogger(BioServer.class);
	
	private final ExtendExecutor exec;

	private ServerSocket serverSocket;

	private Integer port;

	public BioServer(int port) {
		super("accepter");
		this.port = port;
		exec = new ExtendExecutor(5, 5, 1, port);
	}

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			LOG.error("服务启动异常：" + e.getMessage());
			e.printStackTrace();
		}
		LOG.info("服务启动端口：" + port);
		Request request = null;
		while (!interrupted()) {
			try {
				Socket socket = serverSocket.accept();
				InputStream input = socket.getInputStream();
				
				//读取消息头，长度固定56,0消息代号，1消息版本，2-5消息体长度,6-55消息class类型
				byte[] headArray = new byte[56];
				input.read(headArray);
				
				//解码消息头信息
				int bodyLnegth = Util.decodeInt(headArray, 2);
				String requestClzz = Util.decodeString(headArray, 6, 50);
				@SuppressWarnings("unchecked")
				Class<? extends Request> requestClass = (Class<? extends Request>) Class.forName(requestClzz);
				request = requestClass.newInstance();
				
				//读取消息体
				byte[] bodyArray = new byte[bodyLnegth];
				input.read(bodyArray);
				
				//解密，解码消息体
				request.decode(Util.RSADecode(bodyArray));
				
				//获取对应处理器进行处理
				Handler<? extends Request> handler = Handler.getHandler(request,socket);
				exec.submit(handler);
			} catch (IOException e) {
				if (serverSocket.isClosed()) {
					LOG.warn("socket已经关闭，开始关闭服务");
				} else {
					LOG.error("服务异常关闭！" + e.getMessage());
				}
				shutdown();
				break;
			} catch (RejectedExecutionException e){
				// 可以选择调用者执行的饱和策略r.run(); 但这里本就想关闭服务了，所以直接丢弃 
				LOG.warn("服务已关闭，丢弃"+ request.getName() + "的请求[" 
						+ request.getClass().getSimpleName() + "]:" + request.getMsg());
			} catch (Exception e) {
				LOG.error("消息处理异常,", e); 
			} 
		}
	}

	@SuppressWarnings("unchecked")
	public void shutdown() {
		LOG.info("关闭服务，停止接收请求...");
		interrupt();// 中断accepter

		LOG.info("关闭任务线程池，中断正在处理和取消等待处理的任务");
		List<Runnable> taskList = exec.shutdownNow();
		if (!taskList.isEmpty()) {
			for (Runnable task : taskList) {
				FutureTask<Request1> future = (FutureTask<Request1>) task;
				future.cancel(true);
			}
		}
		
		/**
		 * 关闭的时候将连接放在最后关闭，是为了刚好在这时结束的任务或者不能很好响应中断的任务能够在最后返回响应
		 * 可以在这边在这里也设置一个闭锁阻塞main线程，
		 * 等所有需要返回响应的任务线程都结束之后，再放开闭锁，关闭连接。
		 * 否则任务现在在处理结束后想返回响应时会发现连接已经关闭，无法返回响应
		 */
		LOG.info("断开连接...");
		Util.close(serverSocket);
	}

	/**
	 * @测试：启动服务7秒后关闭
	 */
	public static void main(String[] args) throws InterruptedException {
		BioServer server = new BioServer(4040);
		server.start();

		Thread.sleep(15000);
		server.shutdown();
	}
}