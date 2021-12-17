package io.github.echo.io;

import java.io.Closeable;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

@SuppressWarnings("restriction")
public class Util {

	public static final String KEY_PUBLIC = 
			"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCTDLinNiVmZvDMrX66pEoMCpJZbtQNSTa+Imgz"
					+ "4386ITu7Xy7nS7mSOBvIieCQhPEta/FAgiTPL5g4SH/XyFdYAaKaV3/j+9IiITWsaa5t5JBsQ0VV"
					+ "aacsqgScKba6RgVRQSZUEJaMX7/YOeO8RnP1H6tiZG3vzkAi/2E5hWbp5wIDAQAB";

	public static final String KEY_PRIVATE = 
			"MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAJMMuKc2JWZm8MytfrqkSgwKkllu"
					+ "1A1JNr4iaDPjfzohO7tfLudLuZI4G8iJ4JCE8S1r8UCCJM8vmDhIf9fIV1gBoppXf+P70iIhNaxp"
					+ "rm3kkGxDRVVppyyqBJwptrpGBVFBJlQQloxfv9g547xGc/Ufq2Jkbe/OQCL/YTmFZunnAgMBAAEC"
					+ "gYACYyp8THy+9Nzj9c0g6pnpKCLIIOyAarfgzl4yuXbPUsrNd/Yi+y/AF/kbgGGM9xuTSTVZfsNq"
					+ "ObOW9lZdAnuog8mT8vqWwdBuNYAzNw6vJZbxaVt3LDzBSGhx3RxPe+zcjBMXjAQj8MJFcE81v7CK"
					+ "i03pPtP61kPu2fhnZEEZQQJBANYwYMeFSTTljIqAeePaeIVUVZDYNI2mkXD7Ml6O/hFUg5uMp+Qg"
					+ "9EXI4RJ+P1wHtU3rqRm+hHPI9ZYNpUQznesCQQCvwTSdzeqPTNHBb7tc8zADTab8Ug2uyWIpOPyq"
					+ "32lh1y+WSeJIccFEvaedncZSxi2SHt6l0apU/K7g2BA/KFj1AkEAo+uqZYgQGreC84yXvFW63u9H"
					+ "/O46ah4MORdF2TA+KS0w+56N7v15dN7jwa909g3AJ74vUFCKNcKakRgoXyXRuQJAEwxupnfN51Ad"
					+ "H8j7Vpyo5ILDCW/fOcVr1SnvAJoMMuV+q9xAITfrCYdApm2WNBx0jfS4juJFgsaMMaRZRm8aDQJA"
					+ "d0/61FiITY+j/1r/O3j4OSwOYwOTiQJf9Y0JQBM9yRTPXQVVDSvG6Xvgb4a99Npmq95HenZYxzQ1"
					+ "YWiKUmkAlA==";
	
	public static final String operators[] = { "+", "-", "*", "/" };
	
	public static String buildMsg() {
		SecureRandom random = new SecureRandom();
		return random.nextInt(10) + operators[random.nextInt(4)] + (random.nextInt(10) + 1);

	}

	public static String decodeString(byte[] byteArray,int index,int length){
		byte[] arr1 = new byte[length];
		System.arraycopy(byteArray, index, arr1, 0, length);

		int used = getByteLen(arr1);
		byte[] arr2 = new byte[used];
		System.arraycopy(arr1, 0, arr2, 0, used);
		return new String(arr2); 
	}
	
	public static int decodeInt(byte[] byteArray, int index) {  
	    return (int) ( ((byteArray[index] & 0xFF)<<24)  
	            |((byteArray[index+1] & 0xFF)<<16)  
	            |((byteArray[index+2] & 0xFF)<<8)  
	            |(byteArray[index+3] & 0xFF));   
	}  

	public static long decodeLong(byte[] byteArray,int index) {  
		long value = 0;
		for(int i = 0;i < 8;i++){
			int shift = (7 - i) << 3;
			value |=((long)0xff << shift) & ((long)byteArray[index + i] << shift);
		}
		return value;
	} 

	private static int getByteLen(byte[] data) {
		int i = 0;
		for (; i < data.length; i++) {
			if (data[i] == '\0')
				break;
		}
		return i;
	}

	public static void close(Closeable io) {
		if (io != null) {
			try {
				io.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * RSA非对称加密内容长度有限制，1024位key的最多只能加密127位数据，加密后长度固定为128位
	 * 否则就会报错(javax.crypto.IllegalBlockSizeException: Data must not be longer than 117 bytes)
	 * 解决办法是用对称加密(AES/DES etc)加密数据，然后用RSA公钥加密对称加密的密钥,
	 * 用RSA的私钥解密得到对称加密的密钥，然后完成反向操作得到明文。
	 */
	public static void buildKeyofRSA() throws NoSuchAlgorithmException{
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(1024);  
		KeyPair keyPair = keyPairGenerator.generateKeyPair();  
		RSAPublicKey rsaPublicKey = (RSAPublicKey)keyPair.getPublic();   
		RSAPrivateKey rsaPrivateKey = (RSAPrivateKey)keyPair.getPrivate(); 
		System.out.println(new BASE64Encoder().encode(rsaPrivateKey.getEncoded()));
		System.out.println(new BASE64Encoder().encode(rsaPublicKey.getEncoded()));
	}

	public static byte[] RSAEncode(byte[] byteArray) throws Exception {  
		PKCS8EncodedKeySpec encodeKey = 
				new PKCS8EncodedKeySpec(new BASE64Decoder().decodeBuffer(Util.KEY_PRIVATE));  
		PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(encodeKey);  
		Cipher cipher = Cipher.getInstance("RSA");  
		cipher.init(Cipher.ENCRYPT_MODE, privateKey);  
		return cipher.doFinal(byteArray);  
	}  

	public static byte[] RSADecode(byte[] byteArray)  throws Exception {  
		X509EncodedKeySpec x509EncodedKeySpec = 
				new X509EncodedKeySpec(new BASE64Decoder().decodeBuffer(Util.KEY_PUBLIC));  
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");  
		PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);  
		Cipher cipher = Cipher.getInstance("RSA");  
		cipher.init(Cipher.DECRYPT_MODE, publicKey);  
		return cipher.doFinal(byteArray);   
	}  
	
}
