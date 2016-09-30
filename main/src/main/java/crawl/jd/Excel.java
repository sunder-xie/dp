package crawl.jd;

import dp.common.util.Print;
import dp.common.util.excelutil.PoiUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.util.List;
import java.util.Map;

/**
 * Created by huangzhangting on 16/4/13.
 */
public class Excel extends PoiUtil {

    //导出xlsx
    public void exportXlsx(String excelName, String filePath, String[] headList, String[] headList2,
                           Map<String, List<Map<String, String>>> dataMap) throws Exception{
        exportXlsx(excelName, filePath, headList, headList2, dataMap, null, 0, 1);
    }

    public void exportXlsx(String excelName, String filePath, String[] headList,
                           String[] headList2, Map<String, List<Map<String, String>>> dataMap,
                           int[] columnWith, int freezeCol, int freezeRow) throws Exception{

        if(dataMap.isEmpty()){
            Print.info("没有数据");
            return;
        }

        Workbook workbook = new SXSSFWorkbook();
        handleWorkbook(workbook, headList, headList2, dataMap, columnWith, freezeCol, freezeRow);
        toWriteExcel(workbook, excelName, ".xlsx", filePath);
    }

    private Sheet initSheet(Workbook wb, String name, String[] headList, String[] headList2,
                            int[] columnWith, int freezeCol, int freezeRow) {
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
        Cell cell;
        int size = headList.length;
        int i=0;
        for (; i < size; i++) {
            cell = row.createCell(i);
            cell.setCellValue(headList[i]);
            cell.setCellStyle(style);
            if (columnWith != null) {
                sheet.setColumnWidth(i, columnWith[i]);
            } else {
                sheet.setColumnWidth(i, DefaultColumnWith);
            }
        }

        for(i=0; i<headList2.length; i++){
            cell = row.createCell(i+size);
            cell.setCellValue(headList2[i]);
            cell.setCellStyle(style);
            if (columnWith != null) {
                sheet.setColumnWidth(i+size, columnWith[i+size]);
            } else {
                sheet.setColumnWidth(i+size, DefaultColumnWith);
            }
        }

        return sheet;
    }

    public void handleWorkbook(Workbook wb, String[] headList, String[] headList2,
                               Map<String, List<Map<String, String>>> dataMap,
                               int[] columnWith, int freezeCol, int freezeRow){

        Sheet sheet = initSheet(wb, "sheet1", headList, headList2, columnWith, freezeCol, freezeRow);

        CellStyle style = getStyle_LEFT_CENTER(wb, false, false);

        int rowNum = 0;
        int size = headList.length;
        Cell cell;
        String value;
        for(Map.Entry<String, List<Map<String, String>>> entry : dataMap.entrySet()){
            Map<String, String> jdGoods = entry.getValue().remove(0);
            for(Map<String, String> data : entry.getValue()){
                Row row = sheet.createRow(++rowNum);
                row.setHeightInPoints(TableContentRowHeight);

                int i=0;
                for(; i<size; i++){
                    cell = row.createCell(i);
                    cell.setCellStyle(style);
                    value = jdGoods.get(headList[i]);
                    cell.setCellValue(value==null?"":value);
                }

                for(i=0; i<headList2.length; i++){
                    cell = row.createCell(i+size);
                    cell.setCellStyle(style);
                    value = data.get(headList2[i]);
                    cell.setCellValue(value==null?"":value);
                }
            }
        }
    }

}
