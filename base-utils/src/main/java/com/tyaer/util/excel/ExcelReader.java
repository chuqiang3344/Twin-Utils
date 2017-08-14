package com.tyaer.util.excel;

/**
 * Created by Twin on 2016/7/25.
 */

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Twin
 * 操作Excel表格的功能类
 */
public class ExcelReader {


    public String[][] readExceltoTable(String fileUrl) {
        return readExceltoTable(fileUrl, 0);
    }

    /**
     * 读取Excel数据内容
     *
     * @param fileUrl  文件路径
     * @param sheetNum 第几个表
     * @return Map 包含单元格数据内容的Map对象
     */
    public String[][] readExceltoTable(String fileUrl, int sheetNum) {
        String[][] table = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(fileUrl);
            Workbook workbook = null;
            try {
                workbook = WorkbookFactory.create(fis);
            } catch (InvalidFormatException e) {
                e.printStackTrace();
            }
            //得到Excel工作表对象
            Sheet sheet = workbook.getSheetAt(sheetNum);
            //得到Excel工作表的行
            int rowNum = sheet.getLastRowNum() + 1;
            int colNum = sheet.getRow(0).getPhysicalNumberOfCells();
            table = new String[rowNum][colNum];
            for (int i = 0; i < rowNum; i++) {
                Row row = sheet.getRow(i);
                for (int j = 0; j < colNum; j++) {
                    table[i][j] = getCellFormatValue(row.getCell(j)).trim();
                }
            }
            fis.close();
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return table;
    }

    /**
     * 读取Excel数据内容
     *
     * @param fileUrl  文件路径
     * @param sheetNum 第几个表
     * @return Map 包含单元格数据内容的Map对象
     */
    public ArrayList<ArrayList<String>>  readExceltoList(String fileUrl, int sheetNum) {
        ArrayList<ArrayList<String>> table = new ArrayList<>();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(fileUrl);
            Workbook workbook = null;
            try {
                workbook = WorkbookFactory.create(fis);
            } catch (InvalidFormatException e) {
                e.printStackTrace();
            }
            //得到Excel工作表对象
            Sheet sheet = workbook.getSheetAt(sheetNum);
            //得到Excel工作表的行
            int rowNum = sheet.getLastRowNum() + 1;
            int colNum = sheet.getRow(0).getPhysicalNumberOfCells();
            for (int i = 0; i < rowNum; i++) {
                Row row = sheet.getRow(i);
                ArrayList<String> list = new ArrayList<>();
                for (int j = 0; j < colNum; j++) {
                    list.add(getCellFormatValue(row.getCell(j)).trim());
                }
                table.add(list);
            }
            fis.close();
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return table;
    }

    /**
     * 生成
     *
     * @param table
     * @param fileUrl
     * @return
     */
    public boolean writeExcel(String[][] table, String fileUrl) {
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet1 = workbook.createSheet("Sheet1");
        CellStyle cellStyle = workbook.createCellStyle();
        //表头
        Row row1 = sheet1.createRow(0);
        for (int i = 0; i < table[0].length; i++) {
            Cell cell = row1.createCell(i);
            cell.setCellValue(table[0][i]);
            cell.setCellStyle(cellStyle);
        }
        //数据
        for (int i = 1; i < table.length; i++) {
            Row row = sheet1.createRow(i);
            for (int i1 = 0; i1 < table[i].length; i1++) {
                row.createCell(i1).setCellValue(table[i][i1]);
            }
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileUrl);
            workbook.write(fileOutputStream);
            fileOutputStream.close();
            workbook.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 生成
     *
     * @param table
     * @param fileUrl
     * @return
     */
    public boolean writeExcel(ArrayList<ArrayList<String>> lists, String fileUrl) {
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet1 = workbook.createSheet("Sheet1");
        CellStyle cellStyle = workbook.createCellStyle();
        //表头
        Row row1 = sheet1.createRow(0);
        List<String> list1 = lists.get(0);
        for (int i = 0; i < list1.size(); i++) {
            Cell cell = row1.createCell(i);
            cell.setCellValue(list1.get(i));
            cell.setCellStyle(cellStyle);
        }
        //数据
        for (int i = 1; i < lists.size(); i++) {
            Row row = sheet1.createRow(i);
            List<String> listi = lists.get(i);
            for (int i1 = 0; i1 < listi.size(); i1++) {
                row.createCell(i1).setCellValue(listi.get(i1));
            }
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileUrl);
            workbook.write(fileOutputStream);
            fileOutputStream.close();
            workbook.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 读取Excel表格表头的内容
     *
     * @return String 表头内容的数组
     */
    public String[] readExcelTitle(String fileUrl) {
        String[] title = null;
        try {
            Workbook sheets = WorkbookFactory.create(new File(fileUrl));
            Sheet sheet = sheets.getSheetAt(0);
            Row row = sheet.getRow(0);
            // 标题总列数
            int colNum = row.getPhysicalNumberOfCells();
            System.out.println("colNum:" + colNum);
            title = new String[colNum];
            for (int i = 0; i < colNum; i++) {
                title[i] = getCellFormatValue(row.getCell(i));
            }
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return title;
    }

    /**
     * 读取Excel数据内容
     *
     * @return Map 包含单元格数据内容的Map对象
     */
    public Map<Integer, String> readExcelContent(String fileUrl) {
        Map<Integer, String> content = new HashMap<Integer, String>();
        String str = "";
        try {
            Workbook sheets = WorkbookFactory.create(new File(fileUrl));
            Sheet sheet = sheets.getSheetAt(0);
            // 得到总行数
            int rowNum = sheet.getLastRowNum();
            Row row = sheet.getRow(0);
            int colNum = row.getPhysicalNumberOfCells();
            // 正文内容应该从第二行开始,第一行为表头的标题
            for (int i = 1; i <= rowNum; i++) {
                row = sheet.getRow(i);
                int j = 0;
                while (j < colNum) {
                    // 每个单元格的数据内容用"-"分割开，以后需要时用String类的replace()方法还原数据
                    // 也可以将每个单元格的数据设置到一个javabean的属性中，此时需要新建一个javabean
                    // str += getStringCellValue(row.getCell((short) j)).trim() +
                    // "-";
                    str += getCellFormatValue(row.getCell(j)).trim() + "    ";
                    j++;
                }
                content.put(i, str);
                str = "";
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }
        return content;
    }


    /**
     * 获取单元格数据内容为字符串类型的数据
     *
     * @param cell Excel单元格
     * @return String 单元格数据内容
     */
    private String getStringCellValue(HSSFCell cell) {
        String strCell = "";
        CellType cellTypeEnum = cell.getCellTypeEnum();
        switch (cellTypeEnum) {
            case STRING:
                strCell = cell.getStringCellValue();
                break;
            case NUMERIC:
                strCell = String.valueOf(cell.getNumericCellValue());
                break;
            case BOOLEAN:
                strCell = String.valueOf(cell.getBooleanCellValue());
                break;
            case BLANK:
                strCell = "";
                break;
            default:
                strCell = "";
                break;
        }
        if (strCell.equals("") || strCell == null) {
            return "";
        }
        if (cell == null) {
            return "";
        }
        return strCell;
    }

    /**
     * 获取单元格数据内容为日期类型的数据
     *
     * @param cell Excel单元格
     * @return String 单元格数据内容
     */
    private String getDateCellValue(HSSFCell cell) {
        String result = "";
        try {
            CellType cellType = cell.getCellTypeEnum();
            if (cellType == CellType.NUMERIC) {
                Date date = cell.getDateCellValue();
                result = (date.getYear() + 1900) + "-" + (date.getMonth() + 1)
                        + "-" + date.getDate();
            } else if (cellType == CellType.STRING) {
                String date = getStringCellValue(cell);
                result = date.replaceAll("[年月]", "-").replace("日", "").trim();
            } else if (cellType == CellType.BLANK) {
                result = "";
            }
        } catch (Exception e) {
            System.out.println("日期格式不正确!");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 根据HSSFCell类型设置数据
     *
     * @param cell
     * @return
     */
    private String getCellFormatValue(Cell cell) {
        String cellvalue = "";
        if (cell != null) {
            // 判断当前Cell的Type
            CellType typeEnum = cell.getCellTypeEnum();
            switch (typeEnum) {
                // 如果当前Cell的Type为NUMERIC
                case NUMERIC:
                case FORMULA: {
                    // 判断当前的cell是否为Date
                    if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        // 如果是Date类型则，转化为Data格式

                        //方法1：这样子的data格式是带时分秒的：2011-10-12 0:00:00
                        //cellvalue = cell.getDateCellValue().toLocaleString();

                        //方法2：这样子的data格式是不带带时分秒的：2011-10-12
                        Date date = cell.getDateCellValue();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        cellvalue = sdf.format(date);
                    } else {
                        // 如果是纯数字
                        // 取得当前Cell的数值
                        double value = cell.getNumericCellValue();
                        cellvalue = String.valueOf((long) value);
                    }
                    break;
                }
                // 如果当前Cell的Type为STRIN
                case STRING:
                    // 取得当前的Cell字符串
                    cellvalue = cell.getRichStringCellValue().getString();
                    break;
                // 默认的Cell值
                default:
                    cellvalue = " ";
            }
        } else {
            cellvalue = "";
        }
        return cellvalue;

    }
}