package io.github.echo.io.bio.msg;

import org.apache.log4j.Logger;

import io.github.echo.io.Util;

/**
 * 
 * @author shanhm1991
 *
 */
public abstract class Request {
	
	private static final Logger LOG = Logger.getLogger(Request.class);
	
	public static final int HEADER_LEN = 56;
	
	protected String name;

	protected String msg;
	
	private int byteIndex;

	private byte[] byteArray;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	public void init(String name){
		this.name = name;
		this.msg = buildMsg();
		this.byteArray = new byte[60 + getMessageLen()]; 
	}
	
	public abstract byte getMessageId();
	
	public abstract int getMessageLen();
	
	protected String buildMsg(){
		return "";
	}
	
	protected abstract void encode();
	
	public abstract void decode(byte[] byteArray);

	public byte[] getBytes() {
		encode();
		byte[] arr = new byte[HEADER_LEN + 128]; //56 + 128 简单处理，消息头56位，消息体加密后固定128位
		
		//加密消息体部分
		byte[] bodyArr = new byte[getMessageLen()];
		System.arraycopy(byteArray, HEADER_LEN, bodyArr, 0, getMessageLen());
		try {
			byte[] encodeArr = Util.RSAEncode(bodyArr);  //128byte
			System.arraycopy(byteArray, 0, arr, 0, HEADER_LEN);
			System.arraycopy(encodeArr, 0, arr, HEADER_LEN, 128);
		} catch (Exception e) {
			LOG.error("消息加密异常，" + e.getMessage()); 
		}
		return arr;
	}

	protected void encode(byte b){
		byteArray[byteIndex] = b;
		byteIndex++;
	}

	protected void encode(int value){ 
		for (int i = 3; i >= 0; i--, value = value >> 8){
			byteArray[byteIndex + i] = (byte)(value & 0xFF);
		}
		byteIndex += 4;
	}

	protected void encode(long value) {
		for (int i = 7; i >= 0; i--, value = value >> 8){
			byteArray[byteIndex + i] = (byte)(value & 0xFF);
		}
		byteIndex += 8;
	}

	protected void encode(String value, int length){ 
		byte[] arr = value.getBytes();
		int len = arr.length;
		System.arraycopy(arr, 0, byteArray, byteIndex, len); 
		byteIndex += length;
	}
}
