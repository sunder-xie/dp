package dp.biz.common;

import dp.dao.mapper.CommonMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by huangzhangting on 16/4/9.
 */
@Service
public class CommonBizImpl implements CommonBiz {
    @Autowired
    private CommonMapper commonMapper;

    private String getConditionSql(Map<String, Object> condition){
        if(CollectionUtils.isEmpty(condition)){
            return null;
        }

        StringBuilder conditionSql = new StringBuilder();
        for(Map.Entry<String, Object> entry : condition.entrySet()){
            if(entry.getValue()!=null){
                conditionSql.append(" and ").append(entry.getKey());
                conditionSql.append("='").append(entry.getValue()).append("'");
            }
        }
        if(conditionSql.length()==0){
            return null;
        }
        conditionSql.delete(0, 4);
        return conditionSql.toString();
    }

    @Override
    public List<Map<String, Object>> selectList(String table, String[] fieldList, Map<String, Object> condition) {
        if(fieldList==null || fieldList.length==0 || StringUtils.isEmpty(table)){
            return new ArrayList<>();
        }

        StringBuilder sb = new StringBuilder();
        for(String field : fieldList){
            sb.append(",").append(field);
        }
        sb.deleteCharAt(0);

        return commonMapper.selectList(sb.toString(), table, getConditionSql(condition));
    }

    @Override
    public List<Map<String, Object>> selectList(String table, String fields, Map<String, Object> condition) {
        if(StringUtils.isEmpty(table) || StringUtils.isEmpty(fields)){
            return new ArrayList<>();
        }

        return commonMapper.selectList(fields, table, getConditionSql(condition));
    }

}
