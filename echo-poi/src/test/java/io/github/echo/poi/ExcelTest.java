package io.github.echo.poi;

import io.github.echo.poi.excel.ExcelEventReader;
import io.github.echo.poi.excel.ExcelReader;
import io.github.echo.poi.excel.ExcelRow;
import io.github.echo.poi.excel.IExcelReader;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;

/**
 *
 * @author shanhm1991@163.com
 *
 */
@Slf4j
public class ExcelTest {

	@Test
	public void testExcelReader() throws Exception{
		IExcelReader reader = new ExcelReader("excel/ExcelTest.xlsx");
		ExcelRow row;
		while((row = reader.readRow()) != null){
			System.out.println(row);
		}
		reader.close();
		Assertions.assertTrue(true);
	}

	@Test
	public void testExcelEventReader() throws Exception{
		IExcelReader reader = new ExcelEventReader("excel/ExcelTest.xlsx");
		ExcelRow row;
		while((row = reader.readRow()) != null){
			System.out.println(row);
		}
		reader.close();
		Assertions.assertTrue(true);
	}

	@Test
	public void testSheetReader() throws Exception{
		Resource resource = new ClassPathResource("excel/SheetTest.xlsx");
		File excel = resource.getFile();

		SheetTask sheetTask = new SheetTask(excel);
		sheetTask.setExcelRule("excel/sheetRule.xml");
		sheetTask.call();
		Assertions.assertTrue(true);
	}
}
