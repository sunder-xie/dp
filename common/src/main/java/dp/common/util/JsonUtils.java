package dp.common.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.$Gson$Types;
import org.apache.log4j.Logger;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by huangzhangting on 17/4/13.
 */
public class JsonUtils {
    private static final Logger log = Logger.getLogger(JsonUtils.class);

    private static Gson gson;
    private static Gson gsonSerializeNulls;
    static {
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        gsonSerializeNulls = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").serializeNulls().create();
    }

    public static String toJson(Object object){
        try {
            return gson.toJson(object);
        }catch (Exception e){
            log.error("object to json string error", e);
        }
        return null;
    }

    public static String toJsonSerializeNulls(Object object){
        try {
            return gsonSerializeNulls.toJson(object);
        }catch (Exception e){
            log.error("object to json string error", e);
        }
        return null;
    }

    public static <T> T fromJson(String jsonStr, Class<T> tClass){
        try {
            return gson.fromJson(jsonStr, tClass);
        }catch (Exception e){
            log.error("json string to object error", e);
        }
        return null;
    }

    public static <T> T fromJson(JsonElement element, Class<T> tClass){
        try {
            return gson.fromJson(element, tClass);
        }catch (Exception e){
            log.error("json element to object error", e);
        }
        return null;
    }

    public static JsonObject toJsonObject(String jsonStr){
        try {
            return gson.fromJson(jsonStr, JsonObject.class);
        }catch (Exception e){
            log.error("json string to json object error", e);
        }
        return null;
    }

    public static <T> T strToCollection(String jsonStr, Class<? extends Collection> collClass, Class<?> elementClass){
        try {
            ParameterizedType type = $Gson$Types.newParameterizedTypeWithOwner(null, collClass, elementClass);
            return gson.fromJson(jsonStr, type);
        }catch (Exception e){
            log.error("json string to collection error", e);
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
