package io.github.shanhm1991.echo.netty.decoder.protobuf;

import io.github.shanhm1991.echo.netty.decoder.protobuf.RequestProto.Request;
import io.github.shanhm1991.echo.netty.decoder.protobuf.ResponseProto.Response;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author shanhm1991
 *
 */
@Slf4j
public class ProtobufServer {

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
			protected void initChannel(SocketChannel channel) {
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
	
	private static class SocketChannelHandler extends ChannelInboundHandlerAdapter {
		
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) {
			Request req = (Request)msg; 
			log.info(">> accept req[id={}, msg={}]", req.getId(), req.getMsg()); 
			
			long beginTime = System.currentTimeMillis();
			Response resp = handleRequest(req);
			ctx.writeAndFlush(resp);
			log.info("<< resp[code={}, msg={}], cost={}ms", resp.getCode(), resp.getMsg(), System.currentTimeMillis() - beginTime);
		}
		
		private Response handleRequest(Request req) {
			Response.Builder builder = Response.newBuilder();
			builder.setCode(200);
			builder.setMsg("resp for " + req.getMsg());
			return builder.build();
		}
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
			log.error("", e); 
			ctx.close();
		}
	} 
	
	public static void main(String[] args) throws InterruptedException {
		ProtobufServer server = new ProtobufServer(8080);
		server.start();
	}
}
