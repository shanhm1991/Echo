package io.github.echo.netty.decoder.msgpack.msg;

import org.msgpack.MessagePack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
* 
* @author shanhm1991@163.com
* 
*/
public class ResponseEncoder extends MessageToByteEncoder<Object> {

	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
		out.writeBytes(new MessagePack().write(msg));
	} 
}
