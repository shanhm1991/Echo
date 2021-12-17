package io.github.echo.io.bio.msg;

import java.security.SecureRandom;

import io.github.echo.io.Util;

/**
 * 
 * @author shanhm1991
 *
 */
public class Request1 extends Request {
	
	private static final byte ID = 0x1F;
	
	private static final byte VERSION = 0x2F;
	
	private static final int LEN = 30;
	
	private static String operators[] = { "+", "-", "*", "/" };

	private static SecureRandom random = new SecureRandom();
	
	@Override
	public byte getMessageId(){
		return ID;
	}
	
	@Override
	public int getMessageLen(){
		return LEN;
	}
	
	@Override
	protected String buildMsg(){
		return random.nextInt(10) + operators[random.nextInt(4)] + (random.nextInt(10) + 1);
	}

	@Override
	protected void encode() {
		encode(ID);
		encode(VERSION);
		encode(128);//RSA加密报文后长度固定为128byte
		encode(this.getClass().getName(),50);
		encode(name,20);
		encode(msg,10);
	}

	@Override
	public void decode(byte[] byteArray) {
		name = Util.decodeString(byteArray, 0, 20);
		msg = Util.decodeString(byteArray, 20, 10);
	}
}
