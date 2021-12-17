package io.github.echo.io.bio.msg;

import io.github.echo.io.Util;

/**
 * 
 * @author shanhm1991
 *
 */
public class Request2 extends Request {

	private static final byte ID = 0x1E;
	
	private static final byte VERSION = 0x2E;

	private static final int LEN = 58;
	
	private long time = System.currentTimeMillis();
	
	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	@Override
	public byte getMessageId() {
		return ID;
	}

	@Override
	public int getMessageLen() {
		return LEN;
	}

	@Override
	protected void encode() {
		encode(ID);
		encode(VERSION);
		encode(128);//RSA加密报文后长度固定为128byte
		encode(this.getClass().getName(),50);
		encode(name,10);
		encode(msg,40);
		encode(time);
	}

	@Override
	public void decode(byte[] byteArray) {
		name = Util.decodeString(byteArray, 0, 10);
		msg = Util.decodeString(byteArray, 10, 40);
		time = Util.decodeLong(byteArray,50);
	}

	@Override
	protected String buildMsg() {
		return "格式化:" + time; 
	}
}
