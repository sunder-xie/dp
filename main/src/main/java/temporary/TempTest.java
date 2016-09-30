package temporary;

import dp.biz.common.CommonBiz;
import dp.common.util.Print;
import dp.dao.mapper.CommonMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huangzhangting on 16/4/9.
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:application-context.xml"})
public class TempTest {
    @Autowired
    private CommonMapper commonMapper;
    @Autowired
    private CommonBiz commonBiz;

    @Test
    public void test(){
        String sql = "select brand_id,brand_name,first_letter from db_brand where is_available=1";
        List<Map<String, Object>> dataList = commonMapper.selectListBySql(sql);
        Print.info(dataList.size());
        Print.info(dataList.get(0));

        String dataSql = "brand_id,brand_name,first_letter";
        String table = "db_brand";
        String whereSql = "is_available=1";
        dataList = commonMapper.selectList(dataSql, table, whereSql);
        Print.info(dataList.size());

        String[] fieldList = new String[]{"brand_id", "brand_name", "first_letter"};
        Map<String, Object> condition = new HashMap<>();
        condition.put("is_available", 1);

        dataList = commonBiz.selectList(table, fieldList, condition);
        Print.info(dataList.size());
    }
}
