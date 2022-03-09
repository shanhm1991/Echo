package io.github.shanhm1991.echo.netty.serialize;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
public class JdkSerializer {

	@Test
	public void test() throws IOException, ClassNotFoundException {
		JdkBean bean = JdkBean.INSTANCE;
		byte[] bytes = serialize(bean);

		JdkBean copy = unserialize(bytes);
		Assertions.assertEquals(bean, copy);
		Assertions.assertNotSame(copy, bean);
	}

	private byte[] serialize(JdkBean bean) throws IOException {
		try(ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			ObjectOutputStream output = new ObjectOutputStream(byteArray)){
			output.writeObject(bean);
			return byteArray.toByteArray();
		}
	}

	private JdkBean unserialize(byte[] bytes) throws IOException, ClassNotFoundException {
		try(ByteArrayInputStream byteArray = new ByteArrayInputStream(bytes);
			ObjectInputStream input = new ObjectInputStream(byteArray)){
			return (JdkBean)input.readObject();
		}
	}
}
