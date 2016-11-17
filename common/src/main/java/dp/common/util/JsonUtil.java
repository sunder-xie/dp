package dp.common.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by huangzhangting on 15/8/27.
 */
public class JsonUtil {
    private static ObjectMapper OM = new ObjectMapper();

    /*
    *  对象转换成json字符串
    * */
    public static String objectToStr(Object object){
        try {
            return OM.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
    *  json字符串转换成对象
    * */
    public static <T> T strToObject(String content, Class<T> valueType){
        try {
            return OM.readValue(content, valueType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
    *  json字符串，转换成集合
    *  content：本身必须是json数组
    *  collectionClass：集合class 例如 List.class
    *  elementClass：元素class 例如 User.class
    * */
    public static <T> T strToCollection(String content, Class<? extends Collection> collectionClass, Class<?> elementClass){
        JavaType javaType = getCollectionType(collectionClass, elementClass);
        try {
            return OM.readValue(content, javaType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> List<T> strToList(String content, Class<?> elementClass) {
        List<T> list = strToCollection(content, List.class, elementClass);
        if(list==null){
            return new ArrayList<>();
        }
        return list;
    }

    /*
    *  JsonNode：就是json数组对象
    *  collectionClass：集合class 例如 List.class
    *  elementClass：元素class 例如 User.class
    * */
    public static <T> T jsonNodeToCollection(JsonNode jsonNode, Class<? extends Collection> collectionClass, Class<?> elementClass){
        JavaType javaType = getCollectionType(collectionClass, elementClass);
        try {
            return OM.readValue(jsonNode.toString(), javaType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
    *  将json字符串转换成json对象
    *
    *  JsonNode：就是json对象，包括数组、或单个对象
    *  content：json字符串
    *
    *  可以通过 jsonNode.findValue("pro") 查找属性值，返回值还是 JsonNode
    * */
    public static JsonNode getJsonNode(String content){
        try {
            return OM.readTree(content);
        } catch (IOException e) {
            //e.printStackTrace();
        }
        return null;
    }

    /*
    *  获得java集合类型，json在转化成java集合时需要用到
    *  collectionClass：集合class 例如 List.class
    *  elementClass：元素class 例如 User.class
    * */
    public static JavaType getCollectionType(Class<? extends Collection> collectionClass, Class<?> elementClass){
        return OM.getTypeFactory().constructCollectionType(collectionClass, elementClass);
    }

    public static String jsonNodeToStr(JsonNode jsonNode){
        return jsonNode==null?"":jsonNode.asText();
    }
    public static String jsonNodeToStr(JsonNode parentNode, String field){
        return jsonNodeToStr(parentNode.findValue(field));
    }
    public static Integer jsonNodeToInt(JsonNode jsonNode){
        return jsonNode==null?0:jsonNode.asInt();
    }
    public static Integer jsonNodeToInt(JsonNode parentNode, String field){
        return jsonNodeToInt(parentNode.findValue(field));
    }


}
