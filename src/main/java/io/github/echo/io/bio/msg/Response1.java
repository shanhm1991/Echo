package io.github.echo.io.bio.msg;

import io.github.echo.io.Util;

/**
 * 
 * @author shanhm1991
 *
 */
public class Response1 extends Request {
	
private static final byte ID = 0x1A;
	
	private static final byte VERSION = 0x2A;
	
	private static final int LEN = 50;

	
	@Override
	public byte getMessageId(){
		return ID;
	}
	
	@Override
	public int getMessageLen(){
		return LEN;
	}
	
	@Override
	protected void encode() {
		encode(ID);
		encode(VERSION);
		encode(128);//RSA加密报文后长度固定为128byte
		encode(this.getClass().getName(),50);
		encode(name,20);
		encode(msg,30);
	}

	@Override
	public void decode(byte[] byteArray) {
		name = Util.decodeString(byteArray, 0, 20);
		msg = Util.decodeString(byteArray, 20, 30);
	}

}
