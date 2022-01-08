package io.github.echo.netty.decoder.protobuf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.echo.netty.decoder.protobuf.RequestProto.Request;
import io.github.echo.netty.decoder.protobuf.ResponseProto.Response;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

/**
 * 
 * @author shanhm1991
 *
 */
public class ProtobufClient {
 
	private static final Logger LOG = LoggerFactory.getLogger(ProtobufClient.class);

	private final String host;

	private final int port;

	public ProtobufClient(String host, int port) {
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
			protected void initChannel(SocketChannel channel) throws Exception {
				channel.pipeline().addLast(new ProtobufVarint32FrameDecoder());
				channel.pipeline().addLast(new ProtobufDecoder(Response.getDefaultInstance()));
				channel.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
				channel.pipeline().addLast(new ProtobufEncoder());
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
	
	private class SocketChannelHandler extends ChannelInboundHandlerAdapter {
		
		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			for(int i = 0; i < 10; i++){
				Request req = buildRequest(i);
				ctx.write(req);
				LOG.info(">> send req[id={}, msg={}]", req.getId(), req.getMsg());
			}
			ctx.flush();
		}
		
		private Request buildRequest(int index) {
			Request.Builder builder = Request.newBuilder();
			builder.setId(index);
			builder.setMsg("msg" + index);
			return builder.build();
		}
		
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			Response resp = (Response)msg; 
			LOG.info("<< resp[code={}, msg={}]", resp.getCode(), resp.getMsg()); 
		}
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) throws Exception {
			LOG.error("", e); 
			ctx.close();
		}
	} 
	
	public static void main(String[] args) throws InterruptedException {
		ProtobufClient client = new ProtobufClient("127.0.0.1", 8080);
		client.start();
	}
}
