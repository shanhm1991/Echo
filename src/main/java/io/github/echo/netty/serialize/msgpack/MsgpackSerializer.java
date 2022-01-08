package io.github.echo.netty.serialize.msgpack;

import java.io.IOException;

import org.msgpack.MessagePack;

/**
 * 
 * @author shanhm1991
 * 
 */
public class MsgpackSerializer {

	public static void main(String[] args) throws IOException {
		User user1 = new User();
		user1.setName("shanhm");
		user1.setAge(18);
		
		MessagePack pack = new MessagePack();
		byte[] bytes = pack.write(user1);
		
		User user2 = pack.read(bytes, User.class);
		
		System.out.println(bytes.length);
		System.out.println(user1 == user2);
	}
}
