package dp.common.util.excelutil;

import dp.common.util.Constant;
import dp.common.util.StrUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huangzhangting on 16/4/9.
 */

public class CommReaderXLS extends ReadExcelXLS {
    protected int titleRow;

    protected Map<Integer, String> attrIdxMap;
    protected Map<String, String> attrMap;

    protected List<Map<String, String>> dataList;
    protected Map<String, Map<String, String>> dataMap;

    protected String type;
    protected int mapKeyCol;

    public CommReaderXLS(Map<String, String> attrMap, String type, int mapKeyCol) {
        this.attrMap = attrMap;
        this.type = type;
        this.mapKeyCol = mapKeyCol;

        titleRow = 0;
        attrIdxMap = new HashMap<>();
        dataList = new ArrayList<>();
        dataMap = new HashMap<>();
    }

    @Override
    public void operateRows(int sheetIndex, int curRow, List<String> rowList) throws Exception {
        switch (type){
            case Constant.TYPE_LIST: handleRowsForList(sheetIndex, curRow, rowList); break;
            case Constant.TYPE_MAP: handleRowsForMap(sheetIndex, curRow, rowList); break;
            default: break;
        }
    }

    protected void initAttrIdxMap(List<String> rowList){
        for(int i=0; i<rowList.size(); i++){
            attrIdxMap.put(i, StrUtil.strip(rowList.get(i)));
        }
    }

    protected Map<String, String> getData(List<String> rowList){
        Map<String, String> data = new HashMap<>();
        for(int i=0; i<rowList.size(); i++){
            String attr = attrMap.get(attrIdxMap.get(i));
            if(attr==null) {
                continue;
            }
            data.put(attr, StrUtil.strip(rowList.get(i)));
        }
        return data;
    }

    protected void handleRowsForList(int sheetIndex, int curRow, List<String> rowList){
        if(sheetIndex==0){
            if(curRow==titleRow){
                initAttrIdxMap(rowList);
            }else if(curRow>titleRow){
                dataList.add(getData(rowList));
            }
        }else{
            dataList.add(getData(rowList));
        }
    }

    protected void handleRowsForMap(int sheetIndex, int curRow, List<String> rowList){
        if(sheetIndex==0){
            if(curRow==titleRow){
                initAttrIdxMap(rowList);
            }else if(curRow>titleRow){
                addDataForMap(rowList);
            }
        }else{
            addDataForMap(rowList);
        }
    }
    protected void addDataForMap(List<String> rowList){
        String key = StrUtil.toUpCase(rowList.get(mapKeyCol));
        if(dataMap.get(key)!=null){
            return;
        }

        dataMap.put(key, getData(rowList));
    }


    public void setTitleRow(int titleRow) {
        this.titleRow = titleRow;
    }

    public List<Map<String, String>> getDataList() {
        return dataList;
    }

    public Map<String, Map<String, String>> getDataMap() {
        return dataMap;
    }
}
