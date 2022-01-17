package io.github.echo.netty.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author shanhm1991
 * 
 */
public class _Main {
	
	private static final Logger LOG = LoggerFactory.getLogger(_Main.class);

	public static void main(String[] args) throws InterruptedException {
		TaskExecutorGroup exec = new TaskExecutorGroup(4);
		for(int i = 0; i < 20; i++){
			exec.submit(new Runnable(){
				@Override
				public void run() { 
					LOG.info(Thread.currentThread().getName());
				}
			});
		}
		exec.shutdownGracefully();
	}
}
