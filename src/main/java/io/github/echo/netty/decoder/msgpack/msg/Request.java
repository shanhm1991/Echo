package io.github.echo.netty.decoder.msgpack.msg;

import org.msgpack.annotation.Message;

/**
 * 
 * @author shanhm1991
 *
 */
@Message
public class Request {

	private int id;
	
	private String msg;
	
	public Request() {
		
	}
	
	public Request(int id) {
		this.id = id;
		this.msg = "msg" + id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
}
