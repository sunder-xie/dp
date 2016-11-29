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
public class CommReaderXLSX extends ReadExcelXLSX {
    protected int titleRow;

    protected Map<Integer, String> attrIdxMap;
    protected Map<String, String> attrMap;
    protected int attrSize;

    protected List<Map<String, String>> dataList;
    protected Map<String, Map<String, String>> dataMap;
    protected Map<String, List<Map<String, String>>> dataListMap;

    protected String type;
    protected int mapKeyCol;

    public CommReaderXLSX(Map<String, String> attrMap){
        this(attrMap, Constant.TYPE_LIST, 0);
    }

    public CommReaderXLSX(Map<String, String> attrMap, String type, int mapKeyCol) {
        this.attrMap = attrMap;
        this.type = type;
        this.mapKeyCol = mapKeyCol;

        titleRow = 0;
        attrIdxMap = new HashMap<>();
        dataList = new ArrayList<>();
        dataMap = new HashMap<>();
        dataListMap = new HashMap<>();
    }

    @Override
    public void operateRows(int sheetIndex, int curRow, List<String> rowList) throws Exception {
        switch (type){
            case Constant.TYPE_LIST: handleRowsForList(sheetIndex, curRow, rowList); break;
            case Constant.TYPE_MAP: handleRowsForMap(sheetIndex, curRow, rowList); break;
            case Constant.TYPE_LIST_MAP: handleRowsForListMap(sheetIndex, curRow, rowList); break;
            default: break;
        }
    }

    protected void initAttrIdxMap(List<String> rowList){
        attrSize = rowList.size();
        for(int i=0; i<attrSize; i++){
            attrIdxMap.put(i, StrUtil.strip(rowList.get(i)));
        }
    }

    protected Map<String, String> getData(List<String> rowList){
        int size = rowList.size();
        if(size < attrSize){
            for(int j=size; j<attrSize; j++){
                rowList.add("");
            }
            size = attrSize;
        }

        Map<String, String> data = new HashMap<>();
        for(int i=0; i<size; i++){
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

    //
    protected void handleRowsForListMap(int sheetIndex, int curRow, List<String> rowList){
        if(sheetIndex==0){
            if(curRow==titleRow){
                initAttrIdxMap(rowList);
            }else if(curRow>titleRow){
                addDataForListMap(rowList);
            }
        }else{
            addDataForListMap(rowList);
        }
    }
    protected void addDataForListMap(List<String> rowList){
        String key = StrUtil.toUpCase(rowList.get(mapKeyCol));
        List<Map<String, String>> list = dataListMap.get(key);
        if(list==null){
            list = new ArrayList<>();
            dataListMap.put(key, list);
        }
        list.add(getData(rowList));
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

    public Map<String, List<Map<String, String>>> getDataListMap() {
        return dataListMap;
    }
}
