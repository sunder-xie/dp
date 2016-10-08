package temporary.车型数据处理;

import base.BaseTest;
import dp.common.util.DateUtils;
import dp.common.util.IoUtil;
import dp.common.util.ObjectUtil;
import dp.common.util.Print;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by huangzhangting on 16/9/22.
 */
public class 车款机油用量处理 extends BaseTest {

    @Test
    public void justTest() throws Exception{
        System.out.println(getOilCapacitySql());
    }


    private String getOilCapacitySql(){
        String sql = "select c1.* " +
                "from " +
                "(select tt1.car_models_id as id, max(tt1.engine_oil_num) as oil_capacity  " +
                "from (select t2.car_models_id,t1.engine_oil_num  " +
                "from db_car_info_all t1, db_car_all t2  " +
                "where engine_oil_num != '' and t1.leyel_id=t2.new_l_id  " +
                "group by t2.car_models_id,t1.engine_oil_num) tt1  " +
                "group by tt1.car_models_id) c1, " +
                "(select * from db_car_category where level=6) c2 " +
                "where c1.id=c2.id  " +
                "and c2.power!='电动' and c2.`name` not like '%柴油%' ";

        return sql;
    }

    //统计没有机油用量的数据，需要将有机油用量的sql导入
    private String getNoCapacitySql(){
        return "select * from db_car_category " +
                "where level=6 and oil_capacity=0 " +
                "and power!='电动' and `name` not like '%柴油%' and `year`>='2005' " +
                "and brand not in( " +
                "'阿尔法-罗密欧','阿斯顿马丁','安驰','Alpina','巴博斯', " +
                "'宝龙','保斐利','宾利','布加迪','宝沃','大发','大宇', " +
                "'法拉利','富奇','GMC','光冈','海格','悍马','黑豹','华北', " +
                "'黄海','华阳','恒天','华颂','九龙','金程','卡尔森', " +
                "'科尼赛克','卡威','凯翼','兰博基尼','劳伦士','劳斯莱斯', " +
                "'路特斯','罗孚','玛莎拉蒂','迈巴赫','美亚','迈凯伦', " +
                "'帕加尼','庞蒂克','启腾','RUF','SPRINGO','Scion','萨博','世爵', " +
                "'赛宝','通田','特斯拉','威兹曼','西雅特','新凯','云雀','知豆' " +
                ")";
    }


    @Test
    public void test_oil_capacity() throws Exception{
        List<Map<String, Object>> dataList = commonMapper.selectListBySql(getOilCapacitySql());
        Print.info(dataList.size());
        Print.info(dataList.get(0));

        Map<String, List<String>> oilCarIdsMap = new HashMap<>();

        for(Map<String, Object> data : dataList){
            double oilCapacity = Double.parseDouble(data.get("oil_capacity").toString());
            String str = ObjectUtil.dbToStrHalfUp(oilCapacity);
            List<String> carIds = oilCarIdsMap.get(str);
            if(carIds==null){
                carIds = new ArrayList<>();
                oilCarIdsMap.put(str, carIds);
            }
            carIds.add(data.get("id").toString());
        }

        path = "/Users/huangzhangting/Desktop/安心保险数据对接/车型机油用量/";

        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        writer = IoUtil.getWriter(path + "update_oil_capacity_" + dateStr + ".sql");

        for(Map.Entry<String, List<String>> entry : oilCarIdsMap.entrySet()){
            handleSql(entry.getKey(), entry.getValue());
        }

        IoUtil.closeWriter(writer);
    }

    private void handleSql(String oilCapacity, List<String> carIdList){
        int size = carIdList.size();
        Print.info(oilCapacity+"  "+size);

        if(size==1){
            String sql = "update db_car_category set oil_capacity="+oilCapacity+" where id="+carIdList.get(0)+";\n";
            IoUtil.writeFile(writer, sql);
            return;
        }

        int count = 200;
        int lastIndex = size - 1;
        StringBuilder sql = new StringBuilder();
        for(int i=0; i<size; i++){
            sql.append(carIdList.get(i));
            if((i+1)%count==0){
                writeSql(oilCapacity, sql);
                sql.setLength(0);
                continue;
            }
            if(i==lastIndex){
                writeSql(oilCapacity, sql);
                break;
            }
            sql.append(",");
        }
    }
    private void writeSql(String oilCapacity, StringBuilder sql){
        String str = "update db_car_category set oil_capacity="+oilCapacity+" where id in(";
        sql.insert(0, str);
        sql.append(");\n");
        IoUtil.writeFile(writer, sql.toString());
    }


/*

    private boolean unNeedAdd(String id){
        String sql = "select id from db_car_category where level=6 and oil_capacity=0 and id="+id;
        List<String> list = commonMapper.selectOneFieldBySql(sql);
        return list.isEmpty();
    }


    //@Test
    public void add_oil_capacity() throws Exception{
        path = "/Users/huangzhangting/Desktop/安心保险数据对接/车型机油用量/";
        String fileName = path + "yc51OilDosage20160922.txt";

        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        writer = IoUtil.getWriter(path + "add_oil_capacity_"+dateStr+".sql");

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));

        for (String line = br.readLine(); line != null; line = br.readLine()) {
            //System.out.println(line);
            String[] strings = line.split(",");

            if(unNeedAdd(strings[0])){
                continue;
            }

            StringBuilder sql = new StringBuilder();
            sql.append("update db_car_category set oil_capacity=");
            sql.append(strings[1]);
            sql.append(" where id=");
            sql.append(strings[0]);
            sql.append(" and oil_capacity=0 and level=6;\n");

            IoUtil.writeFile(writer, sql.toString());
        }

        br.close();

        IoUtil.closeWriter(writer);
    }
*/


    //生成更新力洋车型机油用量的sql
    @Test
    public void add_oil_capacity_ly_id() throws Exception{
        path = "/Users/huangzhangting/Desktop/安心保险数据对接/车型机油用量/";

        String key = "yc";
        String fileName = path + "yc51LyXOilDosage20160923.txt";

        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        writer = IoUtil.getWriter(path + key +"_add_engine_oil_num_"+dateStr+".sql");

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));

        for (String line = br.readLine(); line != null; line = br.readLine()) {
            //System.out.println(line);
            String[] strings = line.split(",");

            StringBuilder sql = new StringBuilder();
            sql.append("update db_car_info_all set engine_oil_num=");
            sql.append(strings[1].replace("L", ""));
            sql.append(" where leyel_id='");
            sql.append(strings[0]);
            sql.append("' and engine_oil_num='';\n");

            IoUtil.writeFile(writer, sql.toString());
        }

        br.close();

        IoUtil.closeWriter(writer);
    }

}
