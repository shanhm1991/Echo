package org.eto.essay.questions.question2;

/**
 * 
 * 有大约40万个数字(数字范围：0～200000000000000），数字没有重复，这些数字在一个txt文件中，每行为一个数字。
 *  1.这个txt文件放在一个WEB服务器上， 可以通过http://ip:8888/numbers.txt 下载。
 *  2.求这些数字中，最大的100个数字之和。
 *  3.注意：运行堆内存只有4M （ java -Xmx4m )
 *  4.不允许写临时文件
 *  5.要求耗时：小于2秒 (CPU:Intel i5-4590 3.3GHz)
 * 
 * @author shanhm1991
 *
 */
public class Test {

	public static void main(String[] args) {
		
		Case1.test(); //14ms
		System.out.println();
		
		Case2.test(); //12ms
		System.out.println();
		
		Case3.test(); //8ms
	}
}
