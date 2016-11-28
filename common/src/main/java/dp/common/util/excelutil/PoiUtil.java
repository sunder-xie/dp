package dp.common.util.excelutil;

import dp.common.util.DateUtils;
import dp.common.util.ObjectUtil;
import dp.common.util.Print;
import dp.common.util.StrUtil;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.*;


/**
 * Created by huangzhangting on 15/6/27.
 *
 */
public class PoiUtil {

    public static short ExcelTitleFontSize = (short)18;
    public static short TableHeadFontSize = (short)11;
    public static short TableHeadFontSize_sup = (short)14;
    public static short TableContentFontSize = (short)13;
    public static short TableContentFontSize2 = (short)10;
    public static String TableFontName = "宋体";

    public static float ExcelTitleRowHeight = 50f;
    public static float TableHeadRowHeight = 22f;
    public static float TableContentRowHeight = 20f;

    public static float DefaultRowHeight = 20f;

    public static int DefaultColumnWith = 3500;


    //excel标题样式
    public CellStyle getExcelTitleStyle(Workbook wb){

        Font font = wb.createFont();
        font.setFontHeightInPoints(PoiUtil.ExcelTitleFontSize);
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        font.setFontName(TableFontName);

        CellStyle style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setWrapText(true);
        style.setFont(font);

        return style;
    }

    //表格的表头样式
    public CellStyle getTableHeadStyle(Workbook wb){
        Font font = wb.createFont();
        font.setFontHeightInPoints(PoiUtil.TableHeadFontSize);
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        font.setFontName(TableFontName);

        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setWrapText(true);
        style.setBorderBottom((short) 1);
        style.setBorderLeft((short) 1);
        style.setBorderRight((short) 1);
        style.setBorderTop((short) 1);


        return style;
    }

    //表格的表头样式
    public CellStyle getTableHeadStyle_sup(Workbook wb){
        Font font = wb.createFont();
        font.setFontHeightInPoints(PoiUtil.TableHeadFontSize_sup);
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        font.setFontName(TableFontName);

        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setWrapText(true);
        style.setBorderBottom((short)1);
        style.setBorderLeft((short)1);
        style.setBorderRight((short)1);
        style.setBorderTop((short)1);

        return style;
    }

    //表格内容样式   水平垂直都剧中，有边框
    public CellStyle getStyle_CENTER_CENTER(Workbook wb, boolean boldWeight){
        Font font = wb.createFont();
        font.setFontHeightInPoints(PoiUtil.TableContentFontSize);
        if(boldWeight){
            font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        }
        font.setFontName(TableFontName);

        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setWrapText(true);
        style.setBorderBottom((short)1);
        style.setBorderLeft((short)1);
        style.setBorderRight((short)1);
        style.setBorderTop((short)1);

        return style;
    }

    //表格内容样式   水平居左，垂直剧中，有边框
    public CellStyle getStyle_LEFT_CENTER(Workbook wb, boolean boldWeight, boolean important){
        Font font = wb.createFont();
        font.setFontHeightInPoints(PoiUtil.TableContentFontSize2);
        if(boldWeight){
            font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        }
        font.setFontName(TableFontName);

        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        style.setAlignment(CellStyle.ALIGN_LEFT);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
//        style.setWrapText(true);
        style.setBorderBottom((short)1);
        style.setBorderLeft((short)1);
        style.setBorderRight((short)1);
        style.setBorderTop((short)1);
        if(important){
            style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            style.setFillForegroundColor(HSSFColor.YELLOW.index);
        }

        return style;
    }

    //表格内容样式   水平居右，垂直剧中，有边框
    public CellStyle getStyle_RIGHT_CENTER(Workbook wb, boolean boldWeight){
        Font font = wb.createFont();
        font.setFontHeightInPoints(PoiUtil.TableContentFontSize);
        if(boldWeight){
            font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        }
        font.setFontName(TableFontName);

        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setWrapText(true);
        style.setBorderBottom((short)1);
        style.setBorderLeft((short)1);
        style.setBorderRight((short)1);
        style.setBorderTop((short)1);

        return style;
    }

    //表格内容样式  水平居左，垂直居上，有边框
    public CellStyle getStyle_LEFT_TOP(Workbook wb){
        Font font = wb.createFont();
        font.setFontHeightInPoints(PoiUtil.TableContentFontSize);
        font.setFontName(TableFontName);

        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        style.setAlignment(CellStyle.ALIGN_LEFT);
        style.setVerticalAlignment(CellStyle.VERTICAL_TOP);
        style.setWrapText(true);
        style.setBorderBottom((short) 1);
        style.setBorderLeft((short) 1);
        style.setBorderRight((short) 1);
        style.setBorderTop((short) 1);

        return style;
    }

    //表格内容样式   水平右对齐，垂直居中，无边框
    public CellStyle getStyle_RIGHT_CENTER_noBorder(Workbook wb, boolean boldWeight){
        Font font = wb.createFont();
        font.setFontHeightInPoints(PoiUtil.TableContentFontSize);
        if(boldWeight){
            font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        }
        font.setFontName(TableFontName);


        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setWrapText(true);

        return style;
    }

    //输出excel
    public void toWriteExcel(Workbook wb, String excelName, String type, String filePath) throws Exception {
        excelName = excelName+"-"+DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);

        OutputStream os = new FileOutputStream(filePath+excelName+type);

        wb.write(os);
        os.flush();
        os.close();
    }

    //导出xls
    public void exportXLS(String excelName, String filePath, String[] headList, String[] fieldList,
                          Collection<?> dataList) throws Exception{
        exportXLS(excelName, filePath, headList, fieldList, dataList, null, 0, 1);
    }

    public void exportXLS(String excelName, String filePath, String[] headList, String[] fieldList, Collection<?> dataList,
                           int[] columnWith, int freezeCol, int freezeRow) throws Exception{
        Workbook workbook = new HSSFWorkbook();
        handleWorkbookWithBean(workbook, headList, fieldList, dataList, columnWith, freezeCol, freezeRow);
        toWriteExcel(workbook, excelName, ".xls", filePath);
    }

    //导出xlsx
    public void exportXLSX(String excelName, String filePath, String[] headList, String[] fieldList,
                           Collection<?> dataList) throws Exception{
        exportXLSX(excelName, filePath, headList, fieldList, dataList, null, 0, 1);
    }

    public void exportXLSX(String excelName, String filePath, String[] headList, String[] fieldList, Collection<?> dataList,
                           int[] columnWith, int freezeCol, int freezeRow) throws Exception{
        Workbook workbook = new SXSSFWorkbook();
        handleWorkbookWithBean(workbook, headList, fieldList, dataList, columnWith, freezeCol, freezeRow);
        toWriteExcel(workbook, excelName, ".xlsx", filePath);
    }

    public void handleWorkbookWithBean(Workbook wb, String[] headList, String[] fieldList, Collection<?> dataList,
                             int[] columnWith, int freezeCol, int freezeRow) throws Exception {

        Sheet sheet = initSheet(wb, "sheet1", headList, columnWith, freezeCol, freezeRow);

        CellStyle style = getStyle_LEFT_CENTER(wb, false, false);

        int rowNum = 0;
        int size = headList.length;
        Field f;
        Object value;
        for(Object data : dataList){
            Row row = sheet.createRow(++rowNum);
            row.setHeightInPoints(TableContentRowHeight);

            Class cla = data.getClass();
            for(int i=0; i<size; i++){
                Cell cell = row.createCell(i);
                cell.setCellStyle(style);

                String fn = fieldList[i];
                if(fn.contains(".")){
                    String[] fns = fn.split(".");
                    f = cla.getDeclaredField(fns[0]);
                    f.setAccessible(true);
                    //引用对象
                    Object refObj = f.get(data);
                    Class refCla = refObj.getClass();
                    f = refCla.getDeclaredField(fns[1]);
                    f.setAccessible(true);
                    value = f.get(refObj);
                    cell.setCellValue(value==null?"":value.toString());
                }else{
                    f = cla.getDeclaredField(fn);
                    f.setAccessible(true);
                    value = f.get(data);
                    cell.setCellValue(value==null?"":value.toString());
                }
            }
        }

    }

    protected Sheet initSheet(Workbook wb, String name, String[] headList, int[] columnWith, int freezeCol, int freezeRow){
        Sheet sheet = wb.createSheet(name);
        sheet.setDefaultRowHeightInPoints(DefaultRowHeight);

        if (freezeCol < 0) {
            freezeCol = 0;
        }
        if (freezeRow < 0) {
            freezeRow = 0;
        }
        sheet.createFreezePane(freezeCol, freezeRow);//冻结 列、行

        Row row = sheet.createRow(0);
        row.setHeightInPoints(TableHeadRowHeight);

        CellStyle style = getStyle_LEFT_CENTER(wb, true, false);

        int size = headList.length;
        for (int i = 0; i < size; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headList[i]);
            cell.setCellStyle(style);
            if (columnWith != null) {
                sheet.setColumnWidth(i, columnWith[i]);
            } else {
                sheet.setColumnWidth(i, DefaultColumnWith);
            }
        }

        return sheet;
    }


    //导出xls
    public void exportXlsWithMap(String excelName, String filePath, String[] headList,
                                 Collection<Map<String, String>> dataList) throws Exception{
        exportXlsWithMap(excelName, filePath, headList, dataList, null, 0, 1);
    }

    public void exportXlsWithMap(String excelName, String filePath, List<String> headList,
                                 Collection<Map<String, String>> dataList) throws Exception{

        exportXlsWithMap(excelName, filePath, StrUtil.convertList(headList), dataList, null, 0, 1);
    }

    public void exportXlsWithMap(String excelName, String filePath, String[] headList, Collection<Map<String, String>> dataList,
                                  int[] columnWith, int freezeCol, int freezeRow) throws Exception{
        if(dataList.isEmpty()){
            Print.info("没有数据");
            return;
        }

        Workbook workbook = new HSSFWorkbook();
        handleWorkbookWithMap(workbook, headList, dataList, columnWith, freezeCol, freezeRow);
        toWriteExcel(workbook, excelName, ".xls", filePath);
    }

    //导出xlsx
    public void exportXlsxWithMap(String excelName, String filePath, String[] headList,
                                  Collection<Map<String, String>> dataList) throws Exception{
        exportXlsxWithMap(excelName, filePath, headList, dataList, null, 0, 1);
    }

    public void exportXlsxWithMap(String excelName, String filePath, List<String> headList,
                                  Collection<Map<String, String>> dataList) throws Exception{
        exportXlsxWithMap(excelName, filePath, StrUtil.convertList(headList), dataList, null, 0, 1);
    }

    public void exportXlsxWithMap(String excelName, String filePath, String[] headList, Collection<Map<String, String>> dataList,
                           int[] columnWith, int freezeCol, int freezeRow) throws Exception{

        if(dataList.isEmpty()){
            Print.info("没有数据");
            return;
        }

        Workbook workbook = new SXSSFWorkbook();
        handleWorkbookWithMap(workbook, headList, dataList, columnWith, freezeCol, freezeRow);
        toWriteExcel(workbook, excelName, ".xlsx", filePath);
    }

    public void handleWorkbookWithMap(Workbook wb, String[] headList, Collection<Map<String, String>> dataList,
                               int[] columnWith, int freezeCol, int freezeRow){

        Sheet sheet = initSheet(wb, "sheet1", headList, columnWith, freezeCol, freezeRow);

        CellStyle style = getStyle_LEFT_CENTER(wb, false, false);

        int rowNum = 0;
        int size = headList.length;
        for(Map<String, String> data : dataList){
            Row row = sheet.createRow(++rowNum);
            row.setHeightInPoints(TableContentRowHeight);

            for(int i=0; i<size; i++){
                Cell cell = row.createCell(i);
                cell.setCellStyle(style);
                String value = data.get(headList[i]);
                cell.setCellValue(value==null?"":value);
            }
        }
    }

    //导出xlsx
    public void exportXlsxWithMap(String excelName, String filePath, String[] headList, String[] fieldList,
                                  Collection<Map<String, String>> dataList) throws Exception{
        exportXlsxWithMap(excelName, filePath, headList, fieldList, dataList, null, 0, 1);
    }

    public void exportXlsxWithMap(String excelName, String filePath, List<String> headList, List<String> fieldList,
                                  Collection<Map<String, String>> dataList) throws Exception{
        exportXlsxWithMap(excelName, filePath, StrUtil.convertList(headList),
                StrUtil.convertList(fieldList), dataList, null, 0, 1);
    }

    public void exportXlsxWithMap(String excelName, String filePath, String[] headList,
                                  String[] fieldList, Collection<Map<String, String>> dataList,
                                  int[] columnWith, int freezeCol, int freezeRow) throws Exception{

        if(dataList.isEmpty()){
            Print.info("没有数据");
            return;
        }

        Workbook workbook = new SXSSFWorkbook();
        handleWorkbookWithMap(workbook, headList, fieldList, dataList, columnWith, freezeCol, freezeRow);
        toWriteExcel(workbook, excelName, ".xlsx", filePath);
    }

    public void handleWorkbookWithMap(Workbook wb, String[] headList, String[] fieldList, Collection<Map<String, String>> dataList,
                                      int[] columnWith, int freezeCol, int freezeRow){

        Sheet sheet = initSheet(wb, "sheet1", headList, columnWith, freezeCol, freezeRow);

        CellStyle style = getStyle_LEFT_CENTER(wb, false, false);

        int rowNum = 0;
        int size = fieldList.length;
        for(Map<String, String> data : dataList){
            Row row = sheet.createRow(++rowNum);
            row.setHeightInPoints(TableContentRowHeight);

            for(int i=0; i<size; i++){
                Cell cell = row.createCell(i);
                cell.setCellStyle(style);
                String value = data.get(fieldList[i]);
                cell.setCellValue(value==null?"":value);
            }
        }
    }


    //特殊方法
    public static List<Map<String, String>> convert(List<Map<String, Object>> dataList){
        return ObjectUtil.objToStrMapList(dataList);
    }

}
