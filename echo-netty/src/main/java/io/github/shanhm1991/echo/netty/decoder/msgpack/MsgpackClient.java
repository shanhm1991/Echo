package io.github.shanhm1991.echo.netty.decoder.msgpack;

import io.github.shanhm1991.echo.netty.decoder.msgpack.msg.Request;
import io.github.shanhm1991.echo.netty.decoder.msgpack.msg.RequestEncoder;
import io.github.shanhm1991.echo.netty.decoder.msgpack.msg.Response;
import io.github.shanhm1991.echo.netty.decoder.msgpack.msg.ResponseDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author shanhm1991
 *
 */
@Slf4j
public class MsgpackClient {

	private final String host;

	private final int port;

	public MsgpackClient(String host, int port) {
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
				channel.pipeline().addLast("frame decoder", new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2));
				channel.pipeline().addLast("msg decoder", new ResponseDecoder());
				channel.pipeline().addLast("frame encoder", new LengthFieldPrepender(2)); 
				channel.pipeline().addLast("msg encoder", new RequestEncoder());
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
				Request request = new Request(i);
				ctx.write(request);
				log.info(">> send req[id={}, msg={}]", request.getId(), request.getMsg());
			}
			ctx.flush();
		}
		
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) {
			Response resp = (Response)msg;
			log.info("<< resp[code={}, msg={}]", resp.getCode(), resp.getMsg());
		}
		
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
			log.error("", e); 
			ctx.close();
		}
	} 
	
	public static void main(String[] args) throws InterruptedException {
		MsgpackClient client = new MsgpackClient("127.0.0.1", 8080);
		client.start();
	}
}
