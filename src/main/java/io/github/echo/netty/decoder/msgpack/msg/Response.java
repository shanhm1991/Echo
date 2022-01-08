package io.github.echo.netty.decoder.msgpack.msg;

import org.msgpack.annotation.Message;

/**
 * 
 * @author shanhm1991
 *
 */
@Message
public class Response {

	private int code;
	
	private String msg;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
}
