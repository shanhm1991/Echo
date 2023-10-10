package io.github.echo.poi.text;

import java.util.List;

/**
 *
 * @author shanhm1991@163.com
 *
 */
public interface IRow {

	/**
	 * 获取当前行索引
	 */
	int getRowIndex();

	/**
	 * 行内容是否为空
	 */
	boolean isEmpty();

	/**
	 * 获取行列数据
	 */
	List<String> getColumnList();

}
