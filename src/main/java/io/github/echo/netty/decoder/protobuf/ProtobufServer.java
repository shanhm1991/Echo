package io.github.echo.netty.decoder.protobuf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.echo.netty.decoder.protobuf.RequestProto.Request;
import io.github.echo.netty.decoder.protobuf.ResponseProto.Response;
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
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

/**
 * 
 * @author shanhm1991
 *
 */
public class ProtobufServer {

	private static final Logger LOG = LoggerFactory.getLogger(ProtobufServer.class);

	private final int port;
	
	public ProtobufServer(int port) {
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
				channel.pipeline().addLast(new ProtobufVarint32FrameDecoder());
				channel.pipeline().addLast(new ProtobufDecoder(Request.getDefaultInstance()));
				channel.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
				channel.pipeline().addLast(new ProtobufEncoder());
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
			Response resp = handleRequest(req);
			ctx.writeAndFlush(resp);
			LOG.info("<< resp[code={}, msg={}], cost={}ms", resp.getCode(), resp.getMsg(), System.currentTimeMillis() - beginTime);
		}
		
		private Response handleRequest(Request req) {
			Response.Builder builder = Response.newBuilder();
			builder.setCode(200);
			builder.setMsg("resp for " + req.getMsg());
			return builder.build();
		}
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) throws Exception {
			LOG.error("", e); 
			ctx.close();
		}
	} 
	
	public static void main(String[] args) throws InterruptedException {
		ProtobufServer server = new ProtobufServer(8080);
		server.start();
	}
}
