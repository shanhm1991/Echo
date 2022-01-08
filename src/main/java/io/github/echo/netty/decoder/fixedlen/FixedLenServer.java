package io.github.echo.netty.decoder.fixedlen;

import org.apache.log4j.Logger;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * 
 * @author shanhm1991
 *
 */
public class FixedLenServer {

	private static final Logger LOG = Logger.getLogger(FixedLenServer.class);

	private final int port;

	public FixedLenServer(int port) {
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
			protected void initChannel(SocketChannel channel) throws Exception {
				channel.pipeline().addLast(new FixedLengthFrameDecoder(8)); 
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

	private class SocketChannelHandler extends ChannelInboundHandlerAdapter {

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			String req = (String)msg; 
			LOG.info(">> accept " + req); 

			long beginTime = System.currentTimeMillis();
			String response = "resp for " + req;
			ByteBuf buf = Unpooled.copiedBuffer(response.getBytes()); 
			ctx.writeAndFlush(buf);
			LOG.info("<< " + response + ", cost=" + (System.currentTimeMillis() - beginTime) + "ms");
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) throws Exception {
			LOG.error("", e); 
			ctx.close();
		}
	} 

	public static void main(String[] args) throws InterruptedException {
		FixedLenServer server = new FixedLenServer(8181);
		server.start();
	}
}
