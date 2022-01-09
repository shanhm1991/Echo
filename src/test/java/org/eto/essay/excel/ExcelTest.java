package org.eto.essay.excel;

import java.io.File;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import io.github.echo.essay.reader.ExcelEventReader;
import io.github.echo.essay.reader.ExcelReader;
import io.github.echo.essay.reader.ExcelRow;
import io.github.echo.essay.reader.IExcelReader;

/**
 * 
 * @author shanhm1991@163.com
 *
 */
public class ExcelTest {
	
	private static Logger logger = LoggerFactory.getLogger(ExcelTest.class);
	
	@Test
	public void testExcelReader() throws Exception{ 
		IExcelReader reader = new ExcelReader("excel/ExcelTest.xlsx");
		ExcelRow row = null;
		while((row = reader.readRow()) != null){
			logger.info(row.toString());
		}
		reader.close();
	}

	@Test
	public void testExcelEventReader() throws Exception{ 
		IExcelReader reader = new ExcelEventReader("excel/ExcelTest.xlsx");
		ExcelRow row = null;
		while((row = reader.readRow()) != null){
			logger.info(row.toString());
		}
		reader.close();
	}
	
	@Test
	public void testSheetReader() throws Exception{ 
		Resource resource = new ClassPathResource("excel/SheetTest.xlsx");
		File excel = resource.getFile();
		
		SheetTask sheetTask = new SheetTask(excel);
		sheetTask.setExcelRule("excel/sheetRule.xml");
		sheetTask.call();
	}
}
