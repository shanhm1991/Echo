package io.github.shanhm1991.echo.poi;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import io.github.shanhm1991.echo.poi.parse.ParseSheetTask;

/**
 * 
 * @author shanhm1991@163.com
 *
 */
public class SheetTask extends ParseSheetTask<Boolean> {

    public SheetTask(File excel) {
        super(excel);
    }
	
    protected void parseValue(String value, String type, String field, Map<String,Object> data, String columnName) throws Exception {
        if(StringUtils.isBlank(value)){ 
            return;
        }
		
        if("number".equals(type)){
            data.put(field, Long.valueOf(value));
        }else{ 
            data.put(field, value);
        }
    }

    @Override
    protected Boolean handlerData(Map<String, Collection<Map<String, Object>>> excelData) throws Exception {
        for(Map.Entry<String, Collection<Map<String, Object>>> entry : excelData.entrySet()){
            System.out.println(entry.getKey());
            for(Map<String, Object> map : entry.getValue()){
                System.out.println(map);
            }
        };
        return true;
    } 

}

