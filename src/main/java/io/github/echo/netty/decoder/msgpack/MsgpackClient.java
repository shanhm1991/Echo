package io.github.echo.netty.decoder.msgpack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.echo.netty.decoder.msgpack.msg.Request;
import io.github.echo.netty.decoder.msgpack.msg.RequestEncoder;
import io.github.echo.netty.decoder.msgpack.msg.Response;
import io.github.echo.netty.decoder.msgpack.msg.ResponseDecoder;
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
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

/**
 * 
 * @author shanhm1991
 *
 */
public class MsgpackClient {

	private static final Logger LOG = LoggerFactory.getLogger(MsgpackClient.class);

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
			protected void initChannel(SocketChannel channel) throws Exception {
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
	
	private class SocketChannelHandler extends ChannelInboundHandlerAdapter {
		
		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			for(int i = 1; i <= 100; i++){
				Request request = new Request(i);
				ctx.write(request);
				LOG.info(">> send req[id={}, msg={}]", request.getId(), request.getMsg());
			}
			ctx.flush();
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
		MsgpackClient client = new MsgpackClient("127.0.0.1", 8080);
		client.start();
	}
}
