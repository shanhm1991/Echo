package io.github.shanhm1991.echo.netty.serialize;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * 
 * https://developers.google.com/protocol-buffers/docs/downloads
 * 
 * https://blog.csdn.net/antgan/article/details/52103966
 * 
 * @author shanhm1991
 * 
 */
public class ProtoSerializer {
	
	@Test
	public void test() throws IOException {
		ProtoBean.Bean bean = getBean();
		byte[] bytes = serialize(bean);

		ProtoBean.Bean copy = unserialize(bytes);
		Assertions.assertEquals(bean, copy);
		Assertions.assertNotSame(copy, bean);
	}

	private ProtoBean.Bean unserialize(byte[] bytes) throws IOException {
		return ProtoBean.Bean.parseFrom(bytes);
	}
	
	private byte[] serialize(ProtoBean.Bean bean) {
		return bean.toByteArray();
	}

	private ProtoBean.Bean getBean() {
		ProtoBean.Bean.Builder builder = ProtoBean.Bean.newBuilder();
		builder.setName("shanhm");
		builder.setAge(18);
		return builder.build();
	}
}
