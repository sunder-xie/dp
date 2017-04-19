package dp.common.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.$Gson$Types;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by huangzhangting on 17/4/13.
 */
@Slf4j
public class JsonUtils {
    private static Gson gson;
    private static Gson gsonSerializeNulls;
    static {
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        gsonSerializeNulls = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").serializeNulls().create();
    }

    /**
     * 对象转json
     * @param object
     * @return
     */
    public static String toJson(Object object){
        try {
            return gson.toJson(object);
        }catch (Exception e){
            log.error("object to json string error, object:"+object, e);
        }
        return null;
    }

    /**
     * 对象转json，并且序列化null属性
     * @param object
     * @return
     */
    public static String toJsonSerializeNulls(Object object){
        try {
            return gsonSerializeNulls.toJson(object);
        }catch (Exception e){
            log.error("object to json string error, object:"+object, e);
        }
        return null;
    }

    /**
     * json字符串转对象
     * @param jsonStr
     * @param tClass
     * @param <T>
     * @return
     */
    public static <T> T fromJson(String jsonStr, Class<T> tClass){
        try {
            return gson.fromJson(jsonStr, tClass);
        }catch (Exception e){
            log.error("json string to object error, content:"+jsonStr+" returnType:"+tClass, e);
        }
        return null;
    }

    /**
     * json转对象
     * @param element
     * @param tClass
     * @param <T>
     * @return
     */
    public static <T> T fromJson(JsonElement element, Class<T> tClass){
        try {
            return gson.fromJson(element, tClass);
        }catch (Exception e){
            log.error("json element to object error, content:"+element+" returnType:"+tClass, e);
        }
        return null;
    }

    /**
     * 字符串转JsonObject
     * @param jsonStr 必须是对象形式的json串
     * @return
     */
    public static JsonObject toJsonObject(String jsonStr){
        try {
            return gson.fromJson(jsonStr, JsonObject.class);
        }catch (Exception e){
            log.error("json string to json object error, content:"+jsonStr, e);
        }
        return null;
    }

    /**
     * 字符串转JsonElement
     * @param jsonStr
     *
     * 1、如果是简单字符串，则返回JsonPrimitive 例如："123"
     * 2、如果是对象字符串，则返回JsonObject
     * 3、如果是数组字符串，则返回JsonArray
     * 4、如果是解析不了的字符串，则返回JsonPrimitive
     *
     * @return
     */
    public static JsonElement toJsonElement(String jsonStr){
        try {
            return gson.fromJson(jsonStr, JsonElement.class);
        }catch (Exception e){
            log.error("json string to json element error, content:"+jsonStr, e);
        }
        return null;
    }

    /**
     * 字符串转集合对象
     * @param jsonStr
     * @param collClass
     * @param elementClass
     * @param <T>
     * @return
     */
    public static <T> T strToCollection(String jsonStr, Class<? extends Collection> collClass, Class<?> elementClass){
        try {
            ParameterizedType type = $Gson$Types.newParameterizedTypeWithOwner(null, collClass, elementClass);
            return gson.fromJson(jsonStr, type);
        }catch (Exception e){
            log.error("json string to collection error, content:"+jsonStr
                    +" collClass:"+collClass+" elementClass:"+elementClass, e);
        }
        return null;
    }

    public static <T> List<T> strToList(String jsonStr, Class<T> tClass){
        List<T> list = strToCollection(jsonStr, List.class, tClass);
        if(list==null){
            return new ArrayList<>();
        }
        return list;
    }

}
