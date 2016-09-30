package vehicle;

import base.BaseTest;
import dp.common.util.Print;
import org.junit.Test;

import java.util.*;

/**
 * Created by huangzhangting on 16/5/30.
 */
public class TempTest extends BaseTest {

    public String getSql(Set<Integer> idSet){
        String ids = idSet.toString().replace("[","").replace("]","");
        return "select tb1.id,ak.key_name,ca.attr_value " +
                "from  " +
                "(select c.id,cc.brand,cc.series,c.car_name,ct.type_name  " +
                "from  " +
                "(select * from sc_truck_car where id in(  " +
                ids +
                ")) c,  " +
                "(select t1.id,t1.car_name as series,t2.car_name as brand  " +
                "from  " +
                "(select * from sc_truck_car_category where pid!=0) t1,  " +
                "sc_truck_car_category t2 where t1.pid=t2.id) cc,  " +
                "sc_truck_car_type ct  " +
                "where c.car_category_id=cc.id and c.car_type_id=ct.id) tb1,  " +
                "sc_truck_car_attr ca, sc_attr_key ak  " +
                "where tb1.id=ca.car_id and ca.attr_key_id=ak.id  ";
    }

    @Test
    public void test() throws Exception{
        String sql = "select id,notice_number,max_horse_power from db_vehicle where grade=3 and total_weight=''";
        List<Map<String, Object>> dataList = commonMapper.selectListBySql(sql);
        Print.info(dataList.size());

        Set<Integer> idSet = new HashSet<>();
        for(Map<String, Object> data : dataList){
            idSet.addAll(getIdList(data));
        }
        Print.info(idSet);

    }

    public List<Integer> getIdList(Map<String, Object> data){
        String sql = "select id,car_name from sc_truck_car where car_name like '%"+data.get("notice_number").toString()+"%'";
        List<Map<String, Object>> list = commonMapper.selectListBySql(sql);
        if(list.isEmpty()){
            return new ArrayList<>();
        }
        List<Integer> resultList = new ArrayList<>();
        String maxHorsePower = data.get("max_horse_power").toString();
        for(Map<String, Object> map : list){
            if(map.get("car_name").toString().contains(maxHorsePower)){
                resultList.add(Integer.valueOf(map.get("id").toString()));
            }
        }
        return resultList;
    }

}
