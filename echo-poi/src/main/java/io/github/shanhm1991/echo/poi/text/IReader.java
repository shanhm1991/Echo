package io.github.shanhm1991.echo.poi.text;

import java.io.Closeable;

/**
 * 
 * @author shanhm1991@163.com
 *
 */
public interface IReader extends Closeable {
	
	/**
	 * 读取下一行
	 * @return ReaderRow
	 * @throws Exception Exception
	 */
	IRow readRow() throws Exception;
	
}
