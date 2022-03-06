package io.github.shanhm1991.echo.poi.excel;

import java.util.List;

/**
 * 
 * @author shanhm1991@163.com
 *
 */
public interface ExcelSheetFilter {

	/**
	 * 根据sheet索引和sheet名称过滤需要处理的sheet
	 * @param sheetIndex 从1开始
	 * @param sheetName
	 * @return
	 */
	boolean filter(int sheetIndex, String sheetName);
	
	/**
	 * 重排sheet的读取顺序，或者清除不需处理的sheet
	 * @param nameList
	 */
	void resetSheetListForRead(List<String> nameList);
}
