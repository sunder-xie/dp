package dp.biz.common;

import java.util.List;
import java.util.Map;

/**
 * Created by huangzhangting on 16/4/9.
 */
public interface CommonBiz {
    List<Map<String, Object>> selectList(String table, String[] fieldList, Map<String, Object> condition);

    List<Map<String, Object>> selectList(String table, String fields, Map<String, Object> condition);

}
