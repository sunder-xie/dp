package 保险相关;

import base.BaseTest;
import dp.common.util.DateUtils;
import dp.common.util.IoUtil;
import dp.common.util.Print;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by huangzhangting on 17/2/24.
 */
public class SettleDataTest extends BaseTest {

    private String getFieldSql(){
        String sql = "biz_sn,\n" +
                "insurance_order_sn,\n" +
                "agent_id,\n" +
                "agent_name,\n" +
                "settle_rule_type,\n" +
                "cooperation_mode_id,\n" +
                "cooperation_mode_name,\n" +
                "car_owner_name,\n" +
                "vehicle_sn,\n" +
                "insurance_company_id,\n" +
                "insurance_company_name,\n" +
                "settle_project_id,\n" +
                "settle_project_name,\n" +
                "settle_condition_id,\n" +
                "settle_condition_name,\n" +
                "settle_condition_time,\n" +
                "settle_rate,\n" +
                "settle_fee,\n" +
                "settle_fee_status,\n" +
                "audit_people_name,\n" +
                "audit_time,\n" +
                "settle_people_name,\n" +
                "settle_time,\n" +
                "erp_flag";

        return sql;
    }

    @Test
    public void test(){
        String sql = "select " + getFieldSql() + " from settle_shop_check_detail";
        List<Map<String, Object>> dataList = commonMapper.selectListBySql(sql);
        Print.printList(dataList);

        String dateStr = DateUtils.dateToString(new Date(), DateUtils.yyyyMMdd);
        String sqlFile = "mana_add_settle_data_"+dateStr+".sql";
        writer = IoUtil.getWriter("/Users/huangzhangting/Desktop/保险项目/历史数据问题/sql/"+sqlFile);

        for(Map<String, Object> data : dataList){
            IoUtil.writeFile(writer, getSql(data));
        }

        IoUtil.closeWriter(writer);
    }
    private String getSql(Map<String, Object> data){
        StringBuilder sql = new StringBuilder();
        sql.append("insert into settle_shop_check_detail (gmt_create,creator,biz_sn,insurance_order_sn,agent_id,agent_name,settle_rule_type,cooperation_mode_id,cooperation_mode_name,car_owner_name,vehicle_sn,insurance_company_id,insurance_company_name,settle_project_id,settle_project_name,settle_condition_id,settle_condition_name,settle_condition_time,settle_rate,settle_fee,settle_fee_status,audit_people_name,audit_time,settle_people_name,settle_time,erp_flag) value (");
        sql.append("now(),'hzt'");
        appendValue(sql, data.get("biz_sn"));
        appendValue(sql, data.get("insurance_order_sn"));
        appendValue(sql, data.get("agent_id"));
        appendValue(sql, data.get("agent_name"));
        appendValue(sql, data.get("settle_rule_type"));
        appendValue(sql, data.get("cooperation_mode_id"));
        appendValue(sql, data.get("cooperation_mode_name"));
        appendValue(sql, data.get("car_owner_name"));
        appendValue(sql, data.get("vehicle_sn"));
        appendValue(sql, data.get("insurance_company_id"));
        appendValue(sql, data.get("insurance_company_name"));
        appendValue(sql, data.get("settle_project_id"));
        appendValue(sql, data.get("settle_project_name"));
        appendValue(sql, data.get("settle_condition_id"));
        appendValue(sql, data.get("settle_condition_name"));
        appendValue(sql, data.get("settle_condition_time"));
        appendValue(sql, data.get("settle_rate"));
        appendValue(sql, data.get("settle_fee"));
        appendValue(sql, data.get("settle_fee_status"));
        appendValue(sql, data.get("audit_people_name"));
        appendValue(sql, data.get("audit_time"));
        appendValue(sql, data.get("settle_people_name"));
        appendValue(sql, data.get("settle_time"));
        appendValue(sql, data.get("erp_flag"));

        sql.append(");\n");

        return sql.toString();
    }

    private void appendValue(StringBuilder sql, Object value){
        if(value==null){
            sql.append(",null");
        }else{
            sql.append(",'").append(value.toString()).append("'");
        }
    }

}
