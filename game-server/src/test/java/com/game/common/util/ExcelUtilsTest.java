package com.game.common.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * ExcelUtils工具类测试
 */
public class ExcelUtilsTest {

    @Test
    public void testGetCellValueAsString() {
        // 创建一个测试工作簿
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("TestSheet");
            Row row = sheet.createRow(0);
            
            // 测试字符串单元格
            Cell stringCell = row.createCell(0);
            stringCell.setCellValue("testString");
            assertEquals("testString", ExcelUtils.getCellValueAsString(stringCell));
            
            // 测试数字单元格
            Cell numericCell = row.createCell(1);
            numericCell.setCellValue(123.45);
            assertEquals("123.45", ExcelUtils.getCellValueAsString(numericCell));
            
            // 测试布尔单元格
            Cell booleanCell = row.createCell(2);
            booleanCell.setCellValue(true);
            assertEquals("true", ExcelUtils.getCellValueAsString(booleanCell));
            
        } catch (IOException e) {
            fail("创建测试工作簿时出错: " + e.getMessage());
        }
    }
    
    @Test
    public void testGetCellAsInteger() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("TestSheet");
            Row row = sheet.createRow(0);
            
            // 测试整数单元格
            Cell intCell = row.createCell(0);
            intCell.setCellValue(123);
            assertEquals(Integer.valueOf(123), ExcelUtils.getCellAsInteger(intCell));
            
            // 测试字符串转整数
            Cell stringCell = row.createCell(1);
            stringCell.setCellValue("456");
            assertEquals(Integer.valueOf(456), ExcelUtils.getCellAsInteger(stringCell));
            
        } catch (IOException e) {
            fail("创建测试工作簿时出错: " + e.getMessage());
        }
    }
    
    @Test
    public void testGetCellValueByType() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("TestSheet");
            Row row = sheet.createRow(0);
            
            // 测试整数类型
            Cell intCell = row.createCell(0);
            intCell.setCellValue(123);
            assertEquals(Integer.valueOf(123), ExcelUtils.getCellValueByType(intCell, "int"));
            
            // 测试字符串类型
            Cell stringCell = row.createCell(1);
            stringCell.setCellValue("test");
            assertEquals("test", ExcelUtils.getCellValueByType(stringCell, "string"));
            
        } catch (IOException e) {
            fail("创建测试工作簿时出错: " + e.getMessage());
        }
    }
    
    @Test
    public void testParseMapValue() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("TestSheet");
            Row row = sheet.createRow(0);
            
            // 测试intIntMap类型
            Cell mapCell = row.createCell(0);
            mapCell.setCellValue("{1#10,2#20,3#30}");
            Object result = ExcelUtils.parseMapValue(mapCell, "intIntMap");
            
            assertTrue(result instanceof Map);
            Map<Integer, Integer> mapResult = (Map<Integer, Integer>) result;
            assertEquals(Integer.valueOf(10), mapResult.get(1));
            assertEquals(Integer.valueOf(20), mapResult.get(2));
            assertEquals(Integer.valueOf(30), mapResult.get(3));
            
        } catch (IOException e) {
            fail("创建测试工作簿时出错: " + e.getMessage());
        } catch (Exception e) {
            fail("解析Map值时出错: " + e.getMessage());
        }
    }
    
    @Test
    public void testParseListValue() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("TestSheet");
            Row row = sheet.createRow(0);
            
            // 测试intList类型
            Cell listCell = row.createCell(0);
            listCell.setCellValue("{1,2,3,4,5}");
            Object result = ExcelUtils.parseListValue(listCell, "intList");
            
            assertTrue(result instanceof List);
            List<Integer> listResult = (List<Integer>) result;
            assertEquals(5, listResult.size());
            assertEquals(Integer.valueOf(1), listResult.get(0));
            assertEquals(Integer.valueOf(5), listResult.get(4));
            
        } catch (IOException e) {
            fail("创建测试工作簿时出错: " + e.getMessage());
        } catch (Exception e) {
            fail("解析List值时出错: " + e.getMessage());
        }
    }
}