package dp.common.util;

import lombok.extern.slf4j.Slf4j;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by linjian on 17/4/5.
 */
@Slf4j
public class MapUtils {

    public static Map<String, Object> objectToMap(Object object) {
        Map<String, Object> map = new HashMap<>();
        //继承属性无法操作
//        try {
//            Class cls = object.getClass();
//            Field[] fields = cls.getDeclaredFields();
//            for (Field field : fields) {
//                field.setAccessible(true);
//                Object val = field.get(object);
//                if(val==null){
//                    continue;
//                }
//                map.put(field.getName(), val);
//            }
//            return map;
//        } catch (IllegalAccessException e) {
//            log.error("转换失败，object：{}", JsonUtil.objectToStr(object));
//            e.printStackTrace();
//        }

        try {
            BeanInfo bean = Introspector.getBeanInfo(object.getClass());
            PropertyDescriptor[] pds = bean.getPropertyDescriptors();
            for (PropertyDescriptor pd : pds) {
                String fieldName = pd.getName();
                if (!"class".equals(fieldName)) {
                    Object value = pd.getReadMethod().invoke(object);
                    if(value!=null){
                        map.put(fieldName, value);
                    }
                }
            }
        }catch (Exception e){
            log.error("objectToMap error", e);
        }

        return map;
    }

    public static <T> T mapToObject(Map<String, Object> map, Class<T> cls) {
        try {
            T object = cls.newInstance();
            for (String key : map.keySet()) {
                Field field = cls.getDeclaredField(key);
                field.setAccessible(true);
                field.set(object, map.get(key));
            }
            return object;
        } catch (InstantiationException | NoSuchFieldException | IllegalAccessException e) {
            log.error("转换失败，map：{}", JsonUtils.toJsonSerializeNulls(map));
            e.printStackTrace();
        }
        return null;
    }
}
