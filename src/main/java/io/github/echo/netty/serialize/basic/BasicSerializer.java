package io.github.echo.netty.serialize.basic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 
 * @author shanhm1991
 * 
 */
public class BasicSerializer {
	
	public static void main(String[] args) throws IOException, ClassNotFoundException { 
		byte[] bytes = serialize();
		System.out.println(bytes.length);
		
		// 通过序列化赋值单例对象
		User user = unserialize(bytes); 
		System.out.println(user.equals(User.INSTANCE));
		System.out.println(user == User.INSTANCE); 
	}
	
	private static byte[] serialize() throws IOException {
		try(ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
				ObjectOutputStream output = new ObjectOutputStream(byteArray);){
			output.writeObject(User.INSTANCE);
			byte[] bytes = byteArray.toByteArray();
			return bytes;
		}
	}
	
	private static User unserialize(byte[] bytes) throws IOException, ClassNotFoundException {
		try(ByteArrayInputStream byteArray = new ByteArrayInputStream(bytes);
				ObjectInputStream input = new ObjectInputStream(byteArray);){
			User user = (User)input.readObject();
			return user;
		}
	}
}
