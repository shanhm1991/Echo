package io.github.shanhm1991.echo.netty.decoder.delimiter;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author shanhm1991
 *
 */
@Slf4j
public class DelimiterBasedServer {

	private final int port;

	public DelimiterBasedServer(int port) {
		this.port = port;
	}

	public void start() throws InterruptedException {
		EventLoopGroup acceptGroup = new NioEventLoopGroup();
		EventLoopGroup handleGroup = new NioEventLoopGroup();
		ServerBootstrap serverBoot = new ServerBootstrap();
		serverBoot.group(acceptGroup, handleGroup).channel(NioServerSocketChannel.class);
		serverBoot.option(ChannelOption.SO_BACKLOG, 1024);
		serverBoot.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel channel) {
				ByteBuf delimiter = Unpooled.copiedBuffer("$".getBytes());
				channel.pipeline().addLast(new DelimiterBasedFrameDecoder(1024, delimiter));
				channel.pipeline().addLast(new StringDecoder());
				channel.pipeline().addLast(new SocketChannelHandler());
			}
		});

		try {
			ChannelFuture channelFuture = serverBoot.bind(port).sync();
			channelFuture.channel().closeFuture().sync();
		} finally {
			acceptGroup.shutdownGracefully();
			handleGroup.shutdownGracefully();
		}
	}

	private static class SocketChannelHandler extends ChannelInboundHandlerAdapter {

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) {
			String req = (String)msg; 
			log.info(">> accept " + req); 

			long beginTime = System.currentTimeMillis();
			String response = "resp for " + req;
			ByteBuf buf = Unpooled.copiedBuffer((response + "$").getBytes()); // 同样的响应也要添加换行符作为结束
			ctx.writeAndFlush(buf);
			log.info("<< " + response + ", cost=" + (System.currentTimeMillis() - beginTime) + "ms");
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
			log.error("", e); 
			ctx.close();
		}
	} 

	public static void main(String[] args) throws InterruptedException {
		DelimiterBasedServer server = new DelimiterBasedServer(8181);
		server.start();
	}
}
