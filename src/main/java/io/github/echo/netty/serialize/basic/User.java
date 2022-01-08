package io.github.echo.netty.serialize.basic;

import java.io.Serializable;

import org.msgpack.annotation.Message;

/**
 * 
 * @author shanhm1991
 *
 */
@Message
public class User implements Serializable {
	
	private static final long serialVersionUID = -5764732208504468420L;

	public static final User INSTANCE = new User();
	
	private String name;
	
	private int age;

	private User(){
		this.name = "shanhm";
		this.age = 28;
	}

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
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof User)){
			return false;
		}
		User u = (User)obj;
		return name.equals(u.name) && age == u.age;
	}
}
