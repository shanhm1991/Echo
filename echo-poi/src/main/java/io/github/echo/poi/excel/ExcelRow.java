package io.github.echo.poi.excel;

import io.github.echo.poi.text.IRow;

import java.util.List;

/**
 *
 * @author shanhm1991@163.com
 *
 */
public class ExcelRow implements IRow {

	private final int rowIndex;

	private final List<String> columnList;

	private int sheetIndex;

	private String sheetName;

	private boolean isLastRow;

	private boolean isEmpty;


	public ExcelRow(int rowIndex, List<String> rowData){
		this.rowIndex = rowIndex;
		this.columnList = rowData;
	}

	@Override
	public int getRowIndex() {
		return rowIndex;
	}

	@Override
	public boolean isEmpty() {
		return isEmpty;
	}

	@Override
	public List<String> getColumnList() {
		return columnList;
	}

	/**
	 * 是否最后一行
	 */
	public boolean isLastRow() {
		return isLastRow;
	}

	/**
	 * 获取Excel的sheet索引
	 */
	public int getSheetIndex() {
		return sheetIndex;
	}

	/**
	 * 获取Excel的sheet名称
	 */
	public String getSheetName() {
		return sheetName;
	}

	void setEmpty(boolean isEmpty) {
		this.isEmpty = isEmpty;
	}

	void setLastRow(boolean isLastRow) {
		this.isLastRow = isLastRow;
	}

	void setSheetIndex(int sheetIndex) {
		this.sheetIndex = sheetIndex;
	}

	void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}

	@Override
	public String toString() {
		return "{rowIndex=" + rowIndex + ", columnList=" + columnList + ", sheetIndex=" + sheetIndex
				+ ", sheetName=" + sheetName + ", isLastRow=" + isLastRow + ", isEmpty=" + isEmpty + "}";
	}
}
