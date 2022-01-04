package io.github.echo.io.bio.handler;

import java.util.concurrent.FutureTask;

/**
 * 
 * @author shanhm1991
 *
 */
public class CancellableFuture<T> extends FutureTask<T> {

	private Handler handler;
	
	private String msg;

	public CancellableFuture(Runnable runnable, T result) {
		super(runnable, result);
		if(runnable instanceof Handler){
			this.handler = (Handler)runnable;
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
