package vehicle;

import dp.common.util.Constant;
import dp.common.util.DateUtils;
import dp.common.util.IoUtil;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLS;
import org.junit.Test;

import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品分类处理
 */
public class GoodsCategoryTest {
    public Map<String, Integer> localIdMap;
    public Writer writer;

    public int initId;
    public int currentId;

    public String path;
    public String categoryTb;

    public Map<String, Integer> catMaxSubCodeMap;
    public Map<Integer, String> catCodeMap;


    @Test
    public void test() throws Exception {

        initId = 50000;

        path = "/Users/huangzhangting/Documents/vehicle-data-process/商品分类/";
        String excel = path+"中配网类目--秦建飞.xls";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("一级类目", "一级分类");
        attrMap.put("二级类目", "二级分类");
        attrMap.put("三级类目", "三级分类");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(excel, 3);

        handleData(readerXLS.getDataList());
    }

    public void init(){
        currentId = initId;
        localIdMap = new HashMap<>();
        catMaxSubCodeMap = new HashMap<>();
        catCodeMap = new HashMap<>();

        categoryTb = "db_goods_category";

        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        String sql = path + "insert_category_" + dateStr + ".sql";
        writer = IoUtil.getWriter(sql);
        IoUtil.writeFile(writer, "select @nowTime := now();\n");
    }

    public String idKey(String name, Integer pid){
        StringBuilder sb = new StringBuilder();
        sb.append("S_").append(pid).append("_").append(name);
        return sb.toString();
    }

    public Integer getId(String name, Integer pid){
        Integer id = localIdMap.get(idKey(name, pid));

        return id;
    }

    //一级类目编码
    public String firstCatCode(){
        String key = "S_0";
        Integer maxCode = catMaxSubCodeMap.get(key);
        int code;
        if(maxCode==null){
            code = 50;
        }else{
            code = maxCode + 1;
        }
        catMaxSubCodeMap.put(key, code);
        return code+"";
    }

    public int subCatCode(int pid){
        String key = "S_"+pid;
        Integer maxCode = catMaxSubCodeMap.get(key);
        int code;
        if(maxCode==null){
            code = 1;
        }else {
            code = maxCode + 1;
        }
        catMaxSubCodeMap.put(key, code);
        return code;
    }

    public void handleData(List<Map<String, String>> dataList){
        if(dataList.isEmpty()){
            return;
        }
        init();

        String fcCode;
        String scCode;
        for(Map<String, String> data : dataList){

            Print.info("initId: " + initId + "  currentId: " + currentId + "  localIdMapSize: " + localIdMap.size());

            Integer fcId = getId(data.get("一级分类"), 0);
            if(fcId==null){
                newFirstCate(data);

                fcCode = catCodeMap.get(currentId)+".";
                newSecondCate(data, currentId, fcCode);

                scCode = catCodeMap.get(currentId)+".";
                newThirdCate(data, currentId, fcCode+scCode);
            }else{
                fcCode = catCodeMap.get(fcId)+".";
                Integer scId = getId(data.get("二级分类"), fcId);
                if(scId==null){
                    newSecondCate(data, fcId, fcCode);

                    scCode = catCodeMap.get(currentId)+".";
                    newThirdCate(data, currentId, fcCode+scCode);
                }else{
                    if(getId(data.get("三级分类"), scId)==null){
                        scCode = catCodeMap.get(scId)+".";
                        newThirdCate(data, scId, fcCode+scCode);
                    }else{
                        Print.info(data);
                    }
                }
            }
        }

        IoUtil.closeWriter(writer);
    }

    public String insertSql(int level, int id, int pid, String name, String code){
        StringBuilder sb = new StringBuilder();
        sb.append("insert into ").append(categoryTb);
        sb.append("(style,category_thumb,category_img,original_img,gmt_create,vehicle_code,cat_kind,");
        sb.append("cat_level,cat_id,parent_id,cat_name,cat_code) value('','','','',@nowTime,'H',2,");
        sb.append(level).append(",").append(id).append(",").append(pid);
        sb.append(",'").append(name).append("','").append(code).append("');\n");
        return sb.toString();
    }

    //一级分类
    public void newFirstCate(Map<String, String> data){
        currentId++;

        String catCode = firstCatCode();
        IoUtil.writeFile(writer, insertSql(1, currentId, 0, data.get("一级分类"), catCode));
        catCodeMap.put(currentId, catCode);
        localIdMap.put(idKey(data.get("一级分类"), 0), currentId);
    }

    //二级分类
    public void newSecondCate(Map<String, String> data, Integer pid, String parentCodes){
        currentId++;

        int code = subCatCode(pid);
        String catCode;
        if(code<10){
            catCode = "0"+code;
        }else{
            catCode = ""+code;
        }
        IoUtil.writeFile(writer, insertSql(2, currentId, pid, data.get("二级分类"), parentCodes+catCode));
        catCodeMap.put(currentId, catCode);
        localIdMap.put(idKey(data.get("二级分类"), pid), currentId);
    }

    //三级分类
    public void newThirdCate(Map<String, String> data, Integer pid, String parentCodes){
        currentId++;

        int code = subCatCode(pid);
        String catCode;
        if(code<10){
            catCode = "00"+code;
        }else if(code<100){
            catCode = "0"+code;
        }else{
            catCode = ""+code;
        }
        IoUtil.writeFile(writer, insertSql(3, currentId, pid, data.get("三级分类"), parentCodes+catCode));
        catCodeMap.put(currentId, catCode);
        localIdMap.put(idKey(data.get("三级分类"), pid), currentId);
    }

}
