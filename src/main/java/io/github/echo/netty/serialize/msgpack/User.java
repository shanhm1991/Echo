package io.github.echo.netty.serialize.msgpack;

import org.msgpack.annotation.Message;

/**
 * 
 * @author shanhm1991
 *
 */
@Message
public class User {
	
	private String name;
	
	private int age;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}
}
