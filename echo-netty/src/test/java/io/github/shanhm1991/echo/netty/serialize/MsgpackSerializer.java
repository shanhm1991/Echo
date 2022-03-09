package io.github.shanhm1991.echo.netty.serialize;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.msgpack.MessagePack;

/**
 * 
 * @author shanhm1991
 * 
 */
public class MsgpackSerializer {

	@Test
	public void test() throws IOException {
		MessagePackBean bean = getBean();
		byte[] bytes = serialize(bean);

		MessagePackBean copy = unserialize(bytes);
		Assertions.assertEquals(bean, copy);
		Assertions.assertNotSame(copy, bean);
	}

	private MessagePackBean unserialize(byte[] bytes) throws IOException {
		MessagePack pack = new MessagePack();
		return pack.read(bytes, MessagePackBean.class); 
	}

	private byte[] serialize(MessagePackBean bean) throws IOException {
		MessagePack pack = new MessagePack();
		return pack.write(bean);
	}
	
	private MessagePackBean getBean() {
		MessagePackBean bean = new MessagePackBean();
		bean.setName("shanhm");
		bean.setAge(18);
		return bean;
	} 
}
