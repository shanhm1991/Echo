package io.github.echo.netty.decoder.msgpack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.echo.netty.decoder.msgpack.msg.Request;
import io.github.echo.netty.decoder.msgpack.msg.RequestDecoder;
import io.github.echo.netty.decoder.msgpack.msg.Response;
import io.github.echo.netty.decoder.msgpack.msg.ResponseEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

/**
 * 
 * @author shanhm1991
 *
 */
public class MsgpackServer { 

	private static final Logger LOG = LoggerFactory.getLogger(MsgpackServer.class);

	private final int port;
	
	public MsgpackServer(int port) {
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
				// decoder
				channel.pipeline().addLast("frame decoder", new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2));
				channel.pipeline().addLast("msg decoder", new RequestDecoder());
				// encoder
				channel.pipeline().addLast("frame encoder", new LengthFieldPrepender(2)); 
				channel.pipeline().addLast("msg encoder", new ResponseEncoder());
				// ChannelHandler
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
			Request req = (Request)msg; 
			LOG.info(">> accept req[id={}, msg={}]", req.getId(), req.getMsg()); 
			
			long beginTime = System.currentTimeMillis();
			String response = "resp for " + req.getMsg();
			
			Response resp = new Response();
			resp.setMsg(response); 
			resp.setCode(200);
			ctx.writeAndFlush(resp);
			LOG.info("<< resp[code={}, msg={}], cost={}ms", resp.getCode(), resp.getMsg(), System.currentTimeMillis() - beginTime);
		}
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) throws Exception {
			LOG.error("", e); 
			ctx.close();
		}
	} 
	
	public static void main(String[] args) throws InterruptedException {
		MsgpackServer server = new MsgpackServer(8080);
		server.start();
	}
}
