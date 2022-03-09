package io.github.shanhm1991.echo.netty.decoder.msgpack.msg;

import java.util.List;

import org.msgpack.MessagePack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

/**
 * 
 * @author shanhm1991
 *
 */
public class RequestDecoder extends MessageToMessageDecoder<ByteBuf>{

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
		int len = msg.readableBytes();
		byte[] bytes = new byte[len];
		msg.getBytes(msg.readerIndex(), bytes, 0, len);
		
		MessagePack pack = new MessagePack();
		out.add(pack.read(bytes, Request.class));
	}
}
