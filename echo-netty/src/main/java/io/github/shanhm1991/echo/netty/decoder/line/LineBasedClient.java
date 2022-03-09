package io.github.shanhm1991.echo.netty.decoder.line;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author shanhm1991
 *
 */
@Slf4j
public class LineBasedClient {
	
	private final String host;

	private final int port;

	public LineBasedClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void start() throws InterruptedException {
		Bootstrap clientBoot = new Bootstrap();
		EventLoopGroup handleGroup = new NioEventLoopGroup();
		clientBoot.group(handleGroup).channel(NioSocketChannel.class);
		clientBoot.option(ChannelOption.TCP_NODELAY, true);
		clientBoot.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel channel) {
				channel.pipeline().addLast(new LineBasedFrameDecoder(1024));
				channel.pipeline().addLast(new StringDecoder());
				channel.pipeline().addLast(new SocketChannelHandler());
			}
		});
		
		try {
			ChannelFuture channelFuture = clientBoot.connect(host, port).sync();
			channelFuture.channel().closeFuture().sync();
		} finally {
			handleGroup.shutdownGracefully();
		}
	}
	
	private static class SocketChannelHandler extends ChannelInboundHandlerAdapter {
		
		@Override
		public void channelActive(ChannelHandlerContext ctx) {
			for(int i = 1; i <= 100; i++){
				String msg = "msg" + i;
				byte[] bytes = (msg + "\n").getBytes(); // 注意消息要添加换行符作为结束
				ByteBuf buf = Unpooled.buffer(bytes.length);
				buf.writeBytes(bytes);
				ctx.writeAndFlush(buf);
				log.info(">> send " + msg);
			}
		}
		
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) {
			String resp = (String)msg;
			log.info("<< " + resp);
		}
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
			log.error("", e); 
			ctx.close();
		}
	} 
	
	public static void main(String[] args) throws InterruptedException {
		LineBasedClient client = new LineBasedClient("127.0.0.1", 8080);
		client.start();
	}
}
