package io.github.shanhm1991.echo.netty.serialize;

import org.msgpack.annotation.Message;

import java.util.Objects;

/**
 * 
 * @author shanhm1991
 *
 */
@Message
public class MessagePackBean {
	
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

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		MessagePackBean that = (MessagePackBean) o;
		return age == that.age &&Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, age);
	}
}
