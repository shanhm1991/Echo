package io.github.echo.poi.text;

import java.io.Closeable;

/**
 *
 * @author shanhm1991@163.com
 *
 */
public interface IReader extends Closeable {

	/**
	 * 读取下一行
	 */
	IRow readRow() throws Exception;

}
