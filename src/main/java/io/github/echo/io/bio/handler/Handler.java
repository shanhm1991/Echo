package io.github.echo.io.bio.handler;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import org.apache.log4j.Logger;

import io.github.echo.io.Util;
import io.github.echo.io.bio.msg.Request;
import io.github.echo.io.bio.msg.Request1;
import io.github.echo.io.bio.msg.Request2;

/**
 * 
 * Handler实现CancellableRunnable，自定义了Future.cancel(),在cancel()中关闭socket
 * 
 * @author shanhm1991
 *
 * @param <T>
 */
public abstract class Handler<T extends Request> implements CancellableHandler {

	private static final Logger LOG = Logger.getLogger(Handler.class);

	@SuppressWarnings("rawtypes")
	public static Map<Class<? extends Request>,Class<? extends Handler>> handlerMap = new HashMap<>();

	protected Socket socket;

	protected T request;

	static{
		handlerMap.put(Request1.class , Request1Handler.class);
		handlerMap.put(Request2.class , Request2Handler.class);
		//...
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <E extends Request> Handler<? extends Request> getHandler(E request,Socket socket){
		Class<? extends Handler> clzz = handlerMap.get(request.getClass());
		if(clzz == null){
			return null;
		}
		Handler<E> handler = null;
		try {
			handler = (Handler<E>)clzz.newInstance();
		} catch (Exception e) {
			return null;
		} 

		handler.socket = socket;
		handler.request = request;
		return handler;
	}

	public abstract void handler(Socket socket,T v);

	@Override
	public void run() {
		handler(socket,request);
	}

	@Override
	public void cancel() {
		LOG.warn(" 取消处理并保存" + request.getName() + "的请求[" 
				+ request.getClass().getSimpleName() + "]:" + request.getMsg());
		Util.close(socket);
	}

	/**
	 * cancel()能关闭socket的关键就在于这个方法，
	 * 它返回一个匿名的RunnableFuture实例，但是重写了实例的cancel()方法，
	 * 在cancel()之前调用了外部HandleTask实例的cancel()方法，将socket关闭。
	 */
	@SuppressWarnings("unchecked")
	@Override
	public RunnableFuture<T> newTask() {
		return new FutureTask<T>(this,request) {
			@SuppressWarnings("finally")
			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				try {
					Handler.this.cancel();
				} finally {
					return super.cancel(mayInterruptIfRunning);
				}
			}
		};
	}
}