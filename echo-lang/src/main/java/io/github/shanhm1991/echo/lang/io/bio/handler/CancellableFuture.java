package io.github.shanhm1991.echo.lang.io.bio.handler;

import java.util.concurrent.FutureTask;

/**
 * 
 * @author shanhm1991
 *
 */
public class CancellableFuture<T> extends FutureTask<T> {

	private CancellableHandler handler;
	
	private String msg;

	public CancellableFuture(Runnable runnable, T result) {
		super(runnable, result);
		if(runnable instanceof CancellableHandler){
			this.handler = (CancellableHandler)runnable;
		}
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		if(handler != null){
			this.msg = handler.cancel();
			return true;
		}else{
			return super.cancel(mayInterruptIfRunning);
		}
	}

	public String getMsg(){
		return msg;
	}
}
