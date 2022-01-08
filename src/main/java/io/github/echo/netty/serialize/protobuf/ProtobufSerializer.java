package io.github.echo.netty.serialize.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;

import io.github.echo.netty.serialize.protobuf.UserProtos.User2;

/**
 * 
 * https://developers.google.com/protocol-buffers/docs/downloads
 * 
 * https://blog.csdn.net/antgan/article/details/52103966
 * 
 * @author shanhm1991
 * 
 */
public class ProtobufSerializer {

	public static void main(String[] args) throws InvalidProtocolBufferException {
		User2.Builder builder = User2.newBuilder();
		builder.setName("shanhm");
		builder.setAge(18);
		User2 user = builder.build();
		
		byte[] bytes = user.toByteArray(); // encode bytes.lengeth=10
		System.out.println(bytes.length);
		
		User2 user2 = User2.parseFrom(bytes); // decode
		System.out.println(user == user2); 
	}
}
