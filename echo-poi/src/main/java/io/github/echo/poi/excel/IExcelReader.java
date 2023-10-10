package io.github.echo.poi.excel;

import io.github.echo.poi.text.IReader;

import java.util.List;

/**
 *
 * @author shanhm1991@163.com
 *
 */
public interface IExcelReader extends IReader {

	String EXCEL_XLS = "xls";

	String EXCEL_XLSX = "xlsx";

	/**
	 * read next row
	 */
	ExcelRow readRow() throws Exception;

	/**
	 * read next sheet
	 * @return if the current sheet has remaining then return the rest, otherwise return the data of next sheet
	 */
	List<ExcelRow> readSheet() throws Exception;

	/**
	 * set sheet filter
	 */
	void setSheetFilter(ExcelSheetFilter sheetFilter);
}
