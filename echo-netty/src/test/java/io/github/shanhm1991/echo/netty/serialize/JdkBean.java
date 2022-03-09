package io.github.shanhm1991.echo.netty.serialize;

import java.io.Serializable;

import org.msgpack.annotation.Message;

/**
 * 
 * @author shanhm1991
 *
 */
@Message
public class JdkBean implements Serializable {
	
	private static final long serialVersionUID = -5764732208504468420L;

	public static final JdkBean INSTANCE = new JdkBean();
	
	private String name;
	
	private int age;

	private JdkBean(){
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
		if(!(obj instanceof JdkBean)){
			return false;
		}
		JdkBean u = (JdkBean)obj;
		return name.equals(u.name) && age == u.age;
	}
}
