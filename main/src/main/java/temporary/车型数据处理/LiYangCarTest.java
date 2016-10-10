package temporary.车型数据处理;

import base.BaseTest;
import dp.common.util.Constant;
import dp.common.util.IoUtil;
import dp.common.util.ObjectUtil;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.PoiUtil;
import org.junit.Test;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by huangzhangting on 16/9/20.
 */
public class LiYangCarTest extends BaseTest {
    @Test
    public void justTest() throws Exception{

    }


    @Test
    public void testPrice() throws Exception{
        path = "/Users/huangzhangting/Desktop/临时数据处理/";
        String excel = path + "需要处理价格的力洋车型.xls";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("leyel_id", "lid");
        attrMap.put("guide_price", "price");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(excel, attrMap.size());
        List<Map<String, String>> dataList = readerXLS.getDataList();
        Print.info(dataList.size());
        Print.info(dataList.get(0));

        writer = IoUtil.getWriter(path + "update_guide_price.sql");

        for(Map<String, String> data : dataList){
            String[] ps = data.get("price").split("-");
            DecimalFormat df = new DecimalFormat("0.00");
            df.setRoundingMode(RoundingMode.HALF_UP);
            double gp = (Double.parseDouble(ps[0])+Double.parseDouble(ps[1]))/2;

            String sql = "update db_car_info_all set guide_price='"+df.format(gp)
                    +"' where leyel_id='"+data.get("lid")+"';\n";

            IoUtil.writeFile(writer, sql);
        }

        IoUtil.closeWriter(writer);
    }


    /** 导出力洋车型数据 */
    private String lyCarInfoSql(){
        Map<String, String> map = lyCarFieldMap();

        StringBuilder sql = new StringBuilder();
        for(String field : map.values()){
            sql.append(",").append(field);
        }
        sql.deleteCharAt(0);
        sql.insert(0, "select ");
        sql.append(" from db_car_info_all");
        sql.append(" order by car_brand, factory_name, car_series, vehicle_type, model_year, displacement");
        return sql.toString();
    }

    private Map<String, String> lyCarFieldMap(){
        Map<String, String> map = new HashMap<>();
        map.put("id", "leyel_id");
        map.put("品牌", "car_brand");
        map.put("厂家", "factory_name");
        map.put("车系", "car_series");
        map.put("车型", "vehicle_type");
        map.put("年款", "model_year");
        map.put("排量", "displacement");
        map.put("销售名称", "market_name");
        map.put("生产年份", "create_year");
        map.put("进气形式", "intake_style");
        map.put("燃油类型", "fuel_type");
        map.put("变速器类型", "transmission_type");
        map.put("变速器描述", "transmission_desc");
        map.put("档位数", "stall_num");
        map.put("最大功率", "max_power");

        return map;
    }

    //TODO 导出力洋车型数据
    @Test
    public void exportLyCarExcel() throws Exception{

        path = "/Users/huangzhangting/Desktop/力洋车型数据/";
        IoUtil.mkdirsIfNotExist(path);

        String[] heads = new String[]{"id", "品牌", "厂家", "车系", "车型", "年款", "排量", "销售名称",
                "生产年份", "进气形式", "燃油类型", "变速器类型", "变速器描述", "档位数", "最大功率"};

        int size = heads.length;
        String[] fields = new String[size];
        Map<String, String> map = lyCarFieldMap();
        for(int i=0; i<size; i++){
            fields[i] = map.get(heads[i]);
        }

        List<Map<String, Object>> lyCarInfoList = commonMapper.selectListBySql(lyCarInfoSql());
        Print.info(lyCarInfoList.size());
        Print.info(lyCarInfoList.get(0));

        List<Map<String, String>> mapList = ObjectUtil.objToStrMapList(lyCarInfoList);

        PoiUtil poiUtil = new PoiUtil();
        poiUtil.exportXlsxWithMap("力洋车型数据", path, heads, fields, mapList);
    }

}
