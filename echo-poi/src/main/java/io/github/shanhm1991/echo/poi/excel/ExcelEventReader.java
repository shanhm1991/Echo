package io.github.shanhm1991.echo.poi.excel;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.record.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * 
 * @author shanhm1991@163.com
 *
 */
public class ExcelEventReader implements IExcelReader {

	private static final Logger LOG = LoggerFactory.getLogger(ExcelEventReader.class);

	private String type;

	private OPCPackage pkg;

	private ExcelXSSFHandler xssfHandler;

	private POIFSFileSystem pfs;  

	private ExcelHSSFHandler hssfHandler;

	private ExcelSheetFilter sheetFilter;

	public ExcelEventReader(String sourceUri) throws IOException, OpenXML4JException, SAXException, DocumentException {  
		this(new File(sourceUri));
	}

	public ExcelEventReader(File file) throws IOException, OpenXML4JException, SAXException, DocumentException { 
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
		if(EXCEL_XLS.equalsIgnoreCase(type)){
			pfs = new POIFSFileSystem(new FileInputStream(file));  
			initHssf();
		}else if(EXCEL_XLSX.equalsIgnoreCase(type)){
			pkg = OPCPackage.open(file); 
			initXssf();
		}else{
			throw new UnsupportedOperationException("Excel file name must end with .xls or .xlsx");
		}
	}

	public ExcelEventReader(InputStream inputStream, String type) throws IOException, OpenXML4JException, SAXException, DocumentException { 
		this.type = type;
		if(EXCEL_XLS.equalsIgnoreCase(type)){
			pfs = new POIFSFileSystem(inputStream);  
			initHssf();
		}else if(EXCEL_XLSX.equalsIgnoreCase(type)){
			pkg = OPCPackage.open(inputStream); 
			initXssf();
		}else{
			throw new UnsupportedOperationException("Excel file name must end with .xls or .xlsx");
		}
	} 

	private void initHssf() throws IOException {
		hssfHandler = new ExcelHSSFHandler();
		HSSFRequest hssfRequest = new HSSFRequest();
		hssfRequest.addListenerForAllRecords(hssfHandler);

		HSSFEventFactory factory = new HSSFEventFactory(); 
		InputStream bookStream = pfs.createDocumentInputStream("Workbook");
		factory.processEvents(hssfRequest, bookStream);
	}

	private void initXssf() throws IOException, OpenXML4JException, SAXException, DocumentException {  
		XSSFReader reader = new XSSFReader(pkg);
		XMLReader parser = XMLReaderFactory.createXMLReader("com.sun.org.apache.xerces.internal.parsers.SAXParser");
		xssfHandler = new ExcelXSSFHandler(reader, parser);
		parser.setContentHandler(xssfHandler);
	}

	@Override
	public List<ExcelRow> readSheet() throws Exception { 
		if(EXCEL_XLS.equalsIgnoreCase(type)){
			return hssfHandler.readSheet();
		}else if(EXCEL_XLSX.equalsIgnoreCase(type)){
			return xssfHandler.readSheet();
		}else{
			throw new UnsupportedOperationException("Excel file name must end with .xls or .xlsx");
		}
	}

	@Override
	public ExcelRow readRow() throws Exception {
		if(EXCEL_XLS.equalsIgnoreCase(type)){
			return hssfHandler.readRow();
		}else if(EXCEL_XLSX.equalsIgnoreCase(type)){
			return xssfHandler.readRow();
		}else{
			throw new UnsupportedOperationException("Excel file name must end with .xls or .xlsx");
		}
	}

	@Override
	public void close() throws IOException {
		IOUtils.closeQuietly(pkg);
		IOUtils.closeQuietly(pfs);
	}

	@Override
	public void setSheetFilter(ExcelSheetFilter sheetFilter) {
		this.sheetFilter = sheetFilter;
	}

	private class ExcelXSSFHandler extends DefaultHandler {

		private XSSFReader xssfReader;

		private XMLReader xmlReader;

		private SharedStringsTable stringTable;

		private StylesTable stylesTable;

		private Map<String, String> sheetNameRidMap = new LinkedHashMap<>();

		private List<String> sheetNameList = new ArrayList<>();

		private List<String> sheetNameGivenList = new ArrayList<>();

		private int sheetReadingIndex = 0;

		private String sheetName;

		private int sheetIndex;

		private int rowMax;

		private int cellMax;

		private int rowIndex = 0;

		private int cellIndex = 0;

		private CellType cellType;

		private String lastContents = ""; 

		private List<String> columnList;

		private boolean isEnd = false;

		private List<ExcelRow> sheetData;

		private int rowReadingIndex = 0;

		private String formatString;

		private short formatIndex;

		public ExcelXSSFHandler(XSSFReader xssfReader, XMLReader xmlReader) throws InvalidFormatException, IOException, SAXException, DocumentException {  
			this.xmlReader = xmlReader;
			this.xssfReader = xssfReader;
			this.stringTable = xssfReader.getSharedStringsTable();
			this.stylesTable = xssfReader.getStylesTable();
			InputStream bookStream = null;
			try{
				bookStream = xssfReader.getWorkbookData();
				SAXReader reader = new SAXReader();
				reader.setEncoding("UTF-8");
				Document doc = reader.read(bookStream);
				Element book = doc.getRootElement();
				Element sheets = book.element("sheets");
				Iterator<?> it = sheets.elementIterator("sheet");
				while(it.hasNext()){
					Element sheet = (Element)it.next();
					String name = sheet.attributeValue("name");
					String rid = sheet.attributeValue("id");
					sheetNameList.add(name);
					sheetNameRidMap.put(name, rid); 
				}
			}finally{
				IOUtils.closeQuietly(bookStream); 
			}
			//cann't let the customer code to directly modify sheetNameList
			sheetNameGivenList.addAll(sheetNameList);
		}

		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (qName.equals("dimension")) {
				String dimension = attributes.getValue("ref");
				if(dimension.contains(":")){
					dimension = dimension.split(":")[1];
				}
				rowMax = getRowIndex(dimension);  
				cellMax = getCellIndex(dimension);
				sheetData = new ArrayList<>(rowMax);
			}

			if (qName.equals("row")) { 
				cellIndex = 0;
				int currentRowIndex = Integer.valueOf(attributes.getValue("r")) - 1;
				while(rowIndex < currentRowIndex){
					ExcelRow data = new ExcelRow(rowIndex + 1, new ArrayList<String>(0));
					data.setSheetIndex(sheetIndex); 
					data.setSheetName(sheetName); 
					data.setEmpty(true); 
					data.setLastRow(rowIndex == rowMax - 1); 
					sheetData.add(data);
					rowIndex++;
				}
				columnList = new ArrayList<>(cellMax);
			}

			if(qName.equals("c")) {
				lastContents = "";
				int currentCellIndex = getCellIndex(attributes.getValue("r"));
				while(cellIndex < currentCellIndex){
					columnList.add("");
					cellIndex++;
				}

				String type = attributes.getValue("t");
				if(type == null){
					cellType = CellType.BLANK;
				}else{
					switch(type){
					case "b":
						cellType = CellType.BOOLEAN; break;
					case "e":
						cellType = CellType.ERROR; break;
					case "inlineStr":
						//just use CellType._NONE to represent inlineStr
						cellType = CellType._NONE; break; 
					case "s":
						cellType = CellType.STRING; break;
					case "str":
						cellType = CellType.FORMULA; break;
					default:
						cellType = CellType.BLANK;
					}
				} 

				String cellStyle = attributes.getValue("s");
				if (cellStyle != null) {
					int styleIndex = Integer.parseInt(cellStyle);
					XSSFCellStyle style = stylesTable.getStyleAt(styleIndex);
					formatIndex = style.getDataFormat();
					formatString = style.getDataFormatString();
					if (formatString == null) {
						formatString = BuiltinFormats.getBuiltinFormat(formatIndex);
					}
				}
			}
		}

		public void endElement(String uri, String localName, String qName) throws SAXException {
			/**
			 * All fields are uniformly read into string,
			 * and the date type will be first converted to milliseconds.
			 */
			if (qName.equals("v")) {
				switch (cellType){
				case STRING:
					int sharedIndex = Integer.valueOf(lastContents);
					lastContents = stringTable.getItemAt(sharedIndex).toString();
					break;
				case _NONE:
					lastContents = new XSSFRichTextString(lastContents).toString();
					break;
				case FORMULA:
					try {
						lastContents = ExcelReader.formatDouble(lastContents);
					}  catch (Exception e) {
						lastContents = new XSSFRichTextString(lastContents).toString();
						LOG.error("Excel format error: sheet=" + sheetName + ",row=" + rowIndex + ",column=" + cellIndex, e);
					}
					break;
				case BOOLEAN:
				case ERROR:
				default:

				} 

				//如果是常规和文本，则什么也不做，否则看是否是日期，如果不是日期再尝试转数值
				if (formatString != null) {
					if("General".equals(formatString) || "@".equals(formatString)){

					}else if(DateUtil.isADateFormat(formatIndex,formatString)){
						try{
							double value = Double.valueOf(lastContents);
							if(DateUtil.isValidExcelDate(value)) {
								Date date = DateUtil.getJavaDate(value, false);
								lastContents = String.valueOf(date.getTime());
							}
						}catch(Exception e){
							//ignore
						}
					}else{
						try{
							lastContents = ExcelReader.formatDouble(lastContents);
						}catch(Exception e){
							//ignore
						}
					}
					formatString = null;
				}
			}

			if (qName.equals("c")) {
				columnList.add(lastContents);
				cellIndex++;
			}

			if (qName.equals("row")) {
				ExcelRow data = new ExcelRow(rowIndex + 1, columnList);
				data.setSheetIndex(sheetIndex); 
				data.setSheetName(sheetName); 
				data.setEmpty(checkEmpty()); 
				data.setLastRow(rowIndex == rowMax - 1); 
				sheetData.add(data);
				rowIndex++;
			}
		}

		private boolean checkEmpty(){
			if(columnList == null || columnList.size() == 0){
				return true;
			}
			for(String str : columnList){
				if(!StringUtils.isBlank(str)){
					return false;
				}
			}
			return true;
		}

		public void characters(char[] ch, int start, int length) throws SAXException {
			lastContents += new String(ch, start, length);
		}

		private int getRowIndex(String dimension){
			int len = dimension.length();
			int index = 0;
			for(int i = len - 1; i >= 0; i--){
				if(dimension.charAt(i) > 64){//A
					index = i;
					break;
				}
			}
			return Integer.valueOf(dimension.substring(index + 1));
		}

		private int getCellIndex(String dimension){
			int len = dimension.length();
			int index = 0;
			for(int i = len - 1; i >= 0; i--){
				if(dimension.charAt(i) > 64){//A
					index = i;
					break;
				}
			}
			String cellstr = dimension.substring(0, index + 1);

			int cellIndex = 0;
			int indexLen = cellstr.length();
			int bitIndex = 0;
			for(int i = indexLen - 1; i >= 0; i--){ 
				int base = 1;
				int c = cellstr.charAt(i) - 65;
				if(bitIndex > 0){
					c++;//number of columns used 26 jinzhi
					for(int j = 0; j < bitIndex; j++){
						base *=  26;
					}
				}
				cellIndex += c * base;
				bitIndex++;
			}
			return cellIndex;
		}

		public List<ExcelRow> readSheet() throws Exception{ 
			while(true){
				List<ExcelRow> list = new ArrayList<>();
				if(isEnd){
					return null;
				}else if(sheetData == null){
					if(sheetFilter != null){
						sheetFilter.resetSheetListForRead(sheetNameGivenList);
					}
					read();
					continue;
				}else if(rowReadingIndex > 0 && rowReadingIndex < sheetData.size()){
					while(rowReadingIndex < sheetData.size()){
						ExcelRow row = sheetData.get(rowReadingIndex);
						rowReadingIndex++;
						list.add(row);
					}
					read();
					return list;
				}else{
					list.addAll(sheetData);
					read();
					return list;
				}
			}
		}

		public ExcelRow readRow() throws Exception {
			while(true){
				if(isEnd){
					return null;
				}else if(sheetData == null){
					if(sheetFilter != null){
						sheetFilter.resetSheetListForRead(sheetNameGivenList);
					}
					read();
					continue;
				}

				if(rowReadingIndex < sheetData.size()){
					ExcelRow row = sheetData.get(rowReadingIndex);
					rowReadingIndex++;
					return row;
				}else{
					read();
				}
			}
		}

		private void read() throws Exception {
			if(sheetReadingIndex < sheetNameGivenList.size()) {
				sheetName = sheetNameGivenList.get(sheetReadingIndex);
				sheetIndex = sheetNameList.indexOf(sheetName) + 1;
				if(sheetIndex == -1){
					rowReadingIndex = 0;
					sheetReadingIndex++;
					return;
				}

				if(sheetFilter != null && !sheetFilter.filter(sheetIndex, sheetName)){
					rowReadingIndex = 0;
					sheetReadingIndex++;
					return;
				}

				String relId = sheetNameRidMap.get(sheetName);
				rowIndex = 0;
				InputStream sheetStream = null;
				try{
					sheetStream = xssfReader.getSheet(relId);
					xmlReader.parse(new InputSource(sheetStream));
				}finally{
					IOUtils.closeQuietly(sheetStream);
				}
				rowReadingIndex = 0;
				sheetReadingIndex++;
			}else{
				isEnd = true;
			}
		}
	}

	private class ExcelHSSFHandler implements HSSFListener {

		private SSTRecord stringTable;

		private List<String> sheetNameList = new ArrayList<>();

		private List<String> sheetNameGivenList = new ArrayList<>();

		private int sheetIndex = 0;

		private String sheetName;  

		private int rowMax;

		private short cellMax;

		private int rowIndex = 0;

		private Map<String, List<ExcelRow>> bookData = new HashMap<>();

		private List<ExcelRow> sheetData;

		private ExcelRow rowData;

		@Override
		public void processRecord(Record record) {
			switch (record.getSid()) {  
			case SSTRecord.sid:
				stringTable = (SSTRecord)record;
				break;
			case BoundSheetRecord.sid:
				BoundSheetRecord boundSheet = (BoundSheetRecord)record;
				sheetNameList.add(boundSheet.getSheetname());
				break;
			case BOFRecord.sid:  
				BOFRecord bof = (BOFRecord)record;
				if (bof.getType() == BOFRecord.TYPE_WORKSHEET) {
					sheetName = sheetNameList.get(sheetIndex);
					rowIndex = 0;
					sheetIndex++;
				}
				break;  
			case DimensionsRecord.sid:
				DimensionsRecord dimensions = (DimensionsRecord)record;
				rowMax = dimensions.getLastRow();
				cellMax = dimensions.getLastCol();
				sheetData = new ArrayList<>(rowMax);
				rowData = new ExcelRow(rowIndex + 1, new ArrayList<>(cellMax));
				rowData.setSheetIndex(sheetIndex);
				rowData.setSheetName(sheetName); 
				rowData.setLastRow(rowIndex == rowMax - 1); 
				bookData.put(sheetName, sheetData);
				break;
			case NumberRecord.sid:
				NumberRecord number = (NumberRecord) record;
				prepareRowData(number.getRow());
				fillRowData(number.getColumn(), ExcelReader.double2String(number.getValue())); 
				break;
			case LabelSSTRecord.sid:
				LabelSSTRecord labelSST = (LabelSSTRecord)record;
				prepareRowData(labelSST.getRow());
				fillRowData(labelSST.getColumn(), stringTable.getString(labelSST.getSSTIndex()).toString()); 
				break;
			case FormulaRecord.sid:
				FormulaRecord formula = (FormulaRecord)record;
				prepareRowData(formula.getRow());
				fillRowData(formula.getColumn(), ExcelReader.double2String(formula.getValue())); 
				break;
			case BlankRecord.sid:
				BlankRecord blank = (BlankRecord)record;
				prepareRowData(blank.getRow());
				fillRowData(blank.getColumn(), ""); 
				break;
			case BoolErrRecord.sid:
				BoolErrRecord bool = (BoolErrRecord)record;
				prepareRowData(bool.getRow());
				fillRowData(bool.getColumn(), String.valueOf(bool.getBooleanValue())); 
				break;
			}  
		}

		private void fillRowData(short index, String value){
			List<String> columnList = rowData.getColumnList();
			while(columnList.size() < index - 1){
				columnList.add("");
			}
			columnList.add(value);
		}

		private void prepareRowData(int rowNum){
			if(rowNum > rowIndex){
				rowData.setEmpty(false); 
				sheetData.add(rowData);
				rowIndex++;
				fillEmptyRow(rowNum); 
				rowData = new ExcelRow(rowIndex + 1, new ArrayList<>(cellMax));
				rowData.setSheetIndex(sheetIndex);
				rowData.setSheetName(sheetName); 
				rowData.setLastRow(rowIndex == rowMax - 1); 
			}
		}

		private void fillEmptyRow(int rowNum){
			while(rowNum > rowIndex){
				rowData = new ExcelRow(rowIndex + 1, new ArrayList<String>(0));
				rowData.setSheetIndex(sheetIndex); 
				rowData.setSheetName(sheetName); 
				rowData.setEmpty(true); 
				rowData.setLastRow(rowIndex == rowMax - 1); 
				sheetData.add(rowData);
				rowIndex++;
			}
			rowIndex = rowNum;
		}

		private int sheetReadingIndex = 0;

		private int rowReadingIndex = 0;

		private List<ExcelRow> sheetReadingData;

		private boolean inited = false;

		private boolean isEnd = false;

		public List<ExcelRow> readSheet() throws Exception{ 
			if(!inited){
				init();
			}
			while(true){
				List<ExcelRow> list = new ArrayList<>();
				if(isEnd){
					return null;
				}else if(!inited){
					init();
					read();
					continue;
				}else if(rowReadingIndex > 0 && rowReadingIndex < sheetData.size()){
					while(rowReadingIndex < sheetData.size()){
						ExcelRow row = sheetReadingData.get(rowReadingIndex);
						rowReadingIndex++;
						list.add(row);
					}
					read();
					return list;
				}else{
					list.addAll(sheetReadingData);
					read();
					return list;
				}
			}
		}

		public ExcelRow readRow() throws Exception {
			while(true){
				if(isEnd){
					return null;
				}else if(!inited){
					init();
					read();
					continue;
				}
				if(rowReadingIndex < sheetReadingData.size()){
					ExcelRow row = sheetReadingData.get(rowReadingIndex);
					rowReadingIndex++;
					return row;
				}else{
					read();
				}
			}
		}

		private void read() throws Exception {
			if(sheetReadingIndex < sheetNameGivenList.size()) {
				sheetName = sheetNameGivenList.get(sheetReadingIndex);
				sheetIndex = sheetNameList.indexOf(sheetName) + 1;
				if(sheetIndex == -1){
					rowReadingIndex = 0;
					sheetReadingIndex++;
					return;
				}
				if(sheetFilter != null && !sheetFilter.filter(sheetIndex, sheetName)){
					rowReadingIndex = 0;
					sheetReadingIndex++;
					return;
				}
				sheetReadingData = bookData.get(sheetName);
				rowReadingIndex = 0;
				sheetReadingIndex++;
			}else{
				isEnd = true;
			}
		}

		private void init(){
			inited = true;
			sheetNameGivenList.addAll(sheetNameList);
			if(sheetFilter != null){
				sheetFilter.resetSheetListForRead(sheetNameGivenList);
			}
		}
	}
}