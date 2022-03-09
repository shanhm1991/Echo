package io.github.shanhm1991.echo.netty;

import io.github.shanhm1991.echo.netty.executor.NettyExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 *
 * @author shanhm1991@163.com
 *
 */
@Slf4j
public class ExecutorTest {
    
    @Test
    public void testExec() {
        NettyExecutorGroup exec = new NettyExecutorGroup(4);
        for(int i = 0; i < 20; i++){
            exec.submit(() -> log.info(Thread.currentThread().getName()));
        }
        exec.shutdownGracefully();
    }
}
