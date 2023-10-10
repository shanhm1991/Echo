package io.github.echo.poi.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author shanhm1991@163.com
 *
 */
@Slf4j
public class ExcelReader implements IExcelReader {

	private final InputStream inputStream;

	private final String type;

	private final Map<String, Sheet> sheetMap = new HashMap<>();

	private final List<String> sheetNameList = new ArrayList<>();

	private final List<String> sheetNameGivenList = new ArrayList<>();

	private Workbook workbook = null;

	private int sheetIndexReading = 0;

	private Sheet sheet;

	private int sheetIndex = 0;

	private String sheetName;

	private int rowIndex = 0;

	private int rowCount;

	private int cellIndex = 0;

	private ExcelSheetFilter sheetFilter;

	private boolean inited = false;

	public ExcelReader(String sourceUri) throws IOException {
		this(new File(sourceUri));
	}

	public ExcelReader(File file) throws IOException {
		if(!file.exists()) {
			Resource resource = new ClassPathResource(file.getPath());
			file = resource.getFile();
		}

		String name = file.getName();
		int index = name.lastIndexOf('.');
		if(index == -1){
			throw new UnsupportedOperationException("Excel file name must end with .xls or .xlsx");
		}

		this.type = name.substring(index + 1);
		if(!EXCEL_XLS.equalsIgnoreCase(type) && !EXCEL_XLSX.equalsIgnoreCase(type)){
			throw new UnsupportedOperationException("Excel file name must end with .xls or .xlsx.");
		}

		this.inputStream = new FileInputStream(file);
		init();
	}

	public ExcelReader(InputStream inputStream, String type) throws IOException {
		this.type = type;
		this.inputStream = inputStream;
		init();
	}

	@Override
	public void setSheetFilter(ExcelSheetFilter sheetFilter) {
		this.sheetFilter = sheetFilter;
	}

	private void init() throws IOException{
		if(EXCEL_XLS.equalsIgnoreCase(type)){
			workbook = new HSSFWorkbook(inputStream);
		}else if(EXCEL_XLSX.equalsIgnoreCase(type)){
			workbook = new XSSFWorkbook(inputStream);
		}else{
			throw new UnsupportedOperationException("Excel file name must end with .xls or .xlsx");
		}
		int sheetCount = workbook.getNumberOfSheets();
		for(int i = 0;i < sheetCount;i++){
			Sheet shee = workbook.getSheetAt(i);
			sheetNameList.add(shee.getSheetName());
			sheetMap.put(shee.getSheetName(), shee);
		}
		//cann't let the customer code to directly modify sheetNameList
		sheetNameGivenList.addAll(sheetNameList);
	}

	@Override
	public List<ExcelRow> readSheet() {
		List<ExcelRow> list = new ArrayList<>();
		ExcelRow row;
		while((row = readRow()) != null){
			if(!row.isLastRow()){
				list.add(row);
			}else{
				return list;
			}
		}
		return null;
	}

	@Override
	public ExcelRow readRow() {
		if(!inited){
			inited = true;
			if(sheetFilter != null){
				sheetFilter.resetSheetListForRead(sheetNameGivenList);
			}
			initSheet();
		}
		while(true){
			if(sheet == null){
				return null;
			}
			if(sheetFilter != null && !sheetFilter.filter(sheetIndex, sheetName)){
				if(++sheetIndexReading >= sheetNameGivenList.size()){
					return null;
				}
				initSheet();
			}else{
				if(rowIndex >= rowCount){
					if(sheetIndexReading >= sheetNameGivenList.size() - 1){
						return null;
					}else{
						sheetIndexReading++;
						initSheet();
					}
				}else{
					Row row = sheet.getRow(rowIndex);
					rowIndex++;

					//row not exist, don't know why
					if(row == null){
						ExcelRow data = new ExcelRow(rowIndex, new ArrayList<>(0));
						data.setSheetIndex(sheetIndex);
						data.setSheetName(sheetName);
						data.setEmpty(true);
						data.setLastRow(rowIndex == rowCount);
						return data;
					}

					int cellCount = row.getLastCellNum();
					//Illegal Capacity: -1
					if(cellCount <= 0){
						ExcelRow data = new ExcelRow(rowIndex, new ArrayList<>(0));
						data.setSheetIndex(sheetIndex);
						data.setSheetName(sheetName);
						data.setEmpty(true);
						data.setLastRow(rowIndex == rowCount);
						return data;
					}
					List<String> list = new ArrayList<>(cellCount);

					boolean isEmpty = true;
					for(cellIndex = 0; cellIndex < cellCount; cellIndex++){
						String value = getCellValue(row.getCell(cellIndex));
						if(isEmpty && !StringUtils.isBlank(value)){
							isEmpty = false;
						}
						list.add(value);
					}
					ExcelRow rowData = new ExcelRow(rowIndex, list);
					rowData.setSheetIndex(sheetIndex);
					rowData.setSheetName(sheetName);
					rowData.setEmpty(isEmpty);
					rowData.setLastRow(rowIndex == rowCount);
					return rowData;
				}
			}
		}
	}

	private void initSheet(){
		rowIndex = 0;
		sheetName = sheetNameGivenList.get(sheetIndexReading);
		sheetIndex = sheetNameList.indexOf(sheetName) + 1;
		while((sheet = sheetMap.get(sheetName)) == null){
			sheetIndexReading++;
			if(sheetIndexReading >= sheetNameGivenList.size()){
				return;
			}else{
				sheetName = sheetNameGivenList.get(sheetIndexReading);
				sheetIndex = sheetNameList.indexOf(sheetName);
			}
		}
		rowCount = sheet.getLastRowNum() + 1;//poi row num start with 0
	}

	private String getCellValue(Cell cell) {
		if (cell == null) {
			return "";
		}

		switch (cell.getCellType()) {
			case NUMERIC -> {
				double value = cell.getNumericCellValue();
				if (DateUtil.isCellDateFormatted(cell)) {
					Date date = DateUtil.getJavaDate(value);
					return String.valueOf(date.getTime());
				} else {
					return double2String(value);
				}
			}
			case STRING -> {
				return cell.getStringCellValue();
			}
			case BOOLEAN -> {
				return String.valueOf(cell.getBooleanCellValue());
			}
			case FORMULA -> {
				try {
					return double2String(cell.getNumericCellValue());
				} catch (IllegalStateException e) {
					try {
						return cell.getRichStringCellValue().toString();
					} catch (IllegalStateException e2) {
						log.error("Excel format error: sheet=" + sheetName + ",row=" + rowIndex + ",column=" + cellIndex, e2);
						return "";
					}
				} catch (Exception e) {
					log.error("Excel format error: sheet=" + sheetName + ",row=" + rowIndex + ",column=" + cellIndex, e);
					return "";
				}
			}
			case ERROR -> {
				log.error("Excel format error: sheet=" + sheetName + ",row=" + rowIndex + ",column=" + cellIndex);
				return "";
			}
			default -> {
				return "";
			}
		}
	}

	static String double2String(Double d) {
		return formatDouble(d.toString());
	}

	static String formatDouble(String doubleStr) {
		boolean b = doubleStr.contains("E");
		int indexOfPoint = doubleStr.indexOf('.');
		if (b) {
			int indexOfE = doubleStr.indexOf('E');
			BigInteger xs = new BigInteger(doubleStr.substring(indexOfPoint + BigInteger.ONE.intValue(), indexOfE));
			int pow = Integer.parseInt(doubleStr.substring(indexOfE + BigInteger.ONE.intValue()));
			int xsLen = xs.toByteArray().length;
			int scale = Math.max(xsLen - pow, 0);
			doubleStr = String.format("%." + scale + "f", doubleStr);
		} else {
			Pattern p = Pattern.compile(".0$");
			Matcher m = p.matcher(doubleStr);
			if (m.find()) {
				doubleStr = doubleStr.replace(".0", "");
			}
		}
		return doubleStr;
	}

	@Override
	public void close() {
		IOUtils.closeQuietly(workbook);
		IOUtils.closeQuietly(inputStream);
	}
}
