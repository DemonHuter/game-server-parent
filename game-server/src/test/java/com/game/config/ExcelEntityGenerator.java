package com.game.config;

/**
 * Excel实体类生成器
 * 可以单独运行，用于生成单个Excel文件对应的实体类
 */
public class ExcelEntityGenerator {

    /**
     * 主方法，用于单独运行生成实体类
     *
     */
    public static void main(String[] args) {
        ExcelToEntityConverter.generateExcel("ExampleConfig.xlsx");
    }

}