package io.github.echo.poi.excel;

import java.util.List;

/**
 *
 * @author shanhm1991@163.com
 *
 */
public interface ExcelSheetFilter {

	/**
	 * 根据sheet索引(从1开始)和sheet名称过滤需要处理的sheet
	 */
	boolean filter(int sheetIndex, String sheetName);

	/**
	 * 重排sheet的读取顺序，或者清除不需处理的sheet
	 */
	void resetSheetListForRead(List<String> nameList);
}
