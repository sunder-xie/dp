package json;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dp.common.util.JsonUtil;
import dp.common.util.JsonUtils;
import json.data.Dog;
import json.data.House;
import json.data.Person;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by huangzhangting on 17/4/18.
 */
public class JsonTest {

    @Test
    public void test_to_string(){
        Map<String, Object> map = new HashMap<>();
        map.put("key", "key1");
        map.put("value", "12312");
        map.put("nullvalue", null);
        System.out.println(JsonUtils.toJson(map));
        System.out.println(JsonUtils.toJsonSerializeNulls(map));

        Dog dog = new Dog();
        dog.setBirthday(new Date());
        dog.setName("小黑");
        System.out.println(JsonUtils.toJson(dog));
        System.out.println(JsonUtils.toJsonSerializeNulls(dog));

        List<Dog> dogs = new ArrayList<>();
        dogs.add(dog);
        dogs.add(dog);

        House house = new House();
        house.setId(888);
        house.setPrice(new BigDecimal(1080808088));

        Set<House> houseSet = new HashSet<>();
        houseSet.add(house);

        Person person = new Person();
        person.setName("hzt");
        person.setBirthday(new Date());
        person.setDogs(dogs);
        person.setHouse(house);
        person.setHouseSet(houseSet);
        System.out.println(JsonUtils.toJsonSerializeNulls(person));

    }

    @Test
    public void test_to_object(){
        String str = "{\"value\":\"12312\",\"key\":\"key1\"}";
        str = "{\"nullvalue\":null,\"value\":\"12312\",\"key\":\"key1\"}";
        Map map = JsonUtils.fromJson(str, Map.class);
        System.out.println(map);

        str = "{\"id\":null,\"name\":\"小黑\",\"birthday\":1492501622586}";
        str = "{\"id\":null,\"name\":\"小黑\",\"birthday\":\"Apr 18, 2017 3:47:02 PM\"}";
        str = "{\"id\":null,\"name\":\"小黑\",\"birthday\":\"2017-04-18 16:12:00\"}";
        Dog dog = JsonUtils.fromJson(str, Dog.class);
        System.out.println(dog);

        str = "{\"id\":null,\"name\":\"hzt\",\"birthday\":\"2017-04-18 16:14:35\",\"dogs\":null}";
        str = "{\"id\":null,\"name\":\"hzt\",\"birthday\":\"2017-04-18 16:15:49\",\"dogs\":[{\"id\":null,\"name\":\"小黑\",\"birthday\":\"2017-04-18 16:15:49\"}]}";
        str = "{\"id\":null,\"name\":\"hzt\",\"birthday\":\"2017-04-18 16:17:28\",\"dogs\":[{\"id\":null,\"name\":\"小黑\",\"birthday\":\"2017-04-18 16:17:28\"},{\"id\":null,\"name\":\"小黑\",\"birthday\":\"2017-04-18 16:17:28\"}]}";
        str = "{\"id\":123,\"name\":\"hzt\",\"birthday\":\"2017-04-18 16:20:14\",\"dogs\":[{\"id\":null,\"name\":\"小黑\",\"birthday\":\"2017-04-18 16:20:14\"},{\"id\":null,\"name\":\"小黑\",\"birthday\":\"2017-04-18 16:20:14\"}],\"house\":{\"id\":888,\"price\":1080808088,\"name\":null}}";
        str = "{\"id\":null,\"name\":\"hzt\",\"birthday\":\"2017-04-18 16:22:35\",\"dogs\":[{\"id\":null,\"name\":\"小黑\",\"birthday\":\"2017-04-18 16:22:35\"},{\"id\":null,\"name\":\"小黑\",\"birthday\":\"2017-04-18 16:22:35\"}],\"house\":{\"id\":888,\"price\":1080808088,\"name\":null},\"houseSet\":[{\"id\":888,\"price\":1080808088,\"name\":null}]}";
        Person person = JsonUtils.fromJson(str, Person.class);
        System.out.println(person);

    }


    @Test
    public void test_json_object(){
        String str = "{\"id\":null,\"name\":\"hzt\",\"birthday\":\"2017-04-18 16:22:35\",\"dogs\":[{\"id\":null,\"name\":\"小黑\",\"birthday\":\"2017-04-18 16:22:35\"},{\"id\":null,\"name\":\"小黑\",\"birthday\":\"2017-04-18 16:22:35\"}],\"house\":{\"id\":888,\"price\":1080808088,\"name\":null},\"houseSet\":[{\"id\":888,\"price\":1080808088,\"name\":null}]}";
        JsonObject jsonObject = JsonUtils.toJsonObject(str);
        System.out.println(jsonObject);
        if(jsonObject==null){
            return;
        }
        JsonElement name = jsonObject.get("name");
        System.out.println(name.getAsString());

        JsonArray array = jsonObject.getAsJsonArray("dogs");
        System.out.println(array);
        JsonElement dog = array.get(0);
        System.out.println(dog.getClass());
        JsonPrimitive b = dog.getAsJsonObject().getAsJsonPrimitive("birthday");
        System.out.println(b.getAsString());

    }


    @Test
    public void test_json_to_collection(){
        String str = "[{\"id\":null,\"name\":\"小黑\",\"birthday\":\"2017-04-18 16:22:35\"},{\"id\":null,\"name\":\"小黑\",\"birthday\":\"2017-04-18 16:22:35\"}]";
        List<Dog> list = JsonUtils.strToCollection(str, List.class, Dog.class);
        System.out.println(list);
        System.out.println(list.size());

        Set<Dog> set = JsonUtils.strToCollection(str, Set.class, Dog.class);
        System.out.println(set);
        System.out.println(set.size());

        List<Dog> dogs = JsonUtils.strToList(str, Dog.class);
        System.out.println(dogs);
        System.out.println(dogs.size());
    }


    @Test
    public void test_json(){
        String str = "{\"sign\":\"TH0f2xCq/V6/24aLpTs6wYNB/uSGznNA3nTEaahKhRGtpDfoi/mCjPYYr3GNGPRv7lZtN5pgoXPDwI9LVRdbESoZCMlKQ6QF1RGINgOURL1YP8k9L0bY+JeTc/PpRM6x0PjgAtoDEigOp4cXPTGEAgczSBB7VTMs47IJXYe2TrY=\",\"status\":\"OK\",\"signedValue\":\"{\\\"totalNum\\\":4,\\\"inExpDetail\\\":[{\\\"bizOrderNo\\\":\\\"hms20170414003\\\",\\\"curFreezenAmount\\\":0,\\\"tradeNo\\\":\\\"1704141654471299276499\\\",\\\"oriAmount\\\":1000,\\\"curAmount\\\":2000,\\\"chgAmount\\\":1000,\\\"changeTime\\\":\\\"2017-04-14 16:54:47\\\",\\\"accountSetName\\\":\\\"商户测试应用1现金账户集\\\"},{\\\"bizOrderNo\\\":\\\"hms20170411001\\\",\\\"curFreezenAmount\\\":0,\\\"tradeNo\\\":\\\"1704121121313013274997\\\",\\\"oriAmount\\\":0,\\\"curAmount\\\":1000,\\\"chgAmount\\\":1000,\\\"changeTime\\\":\\\"2017-04-12 11:21:31\\\",\\\"accountSetName\\\":\\\"商户测试应用1现金账户集\\\"},{\\\"bizOrderNo\\\":\\\"test_refund000\\\",\\\"curFreezenAmount\\\":0,\\\"tradeNo\\\":\\\"1704111753117183274870\\\",\\\"oriAmount\\\":1,\\\"curAmount\\\":0,\\\"chgAmount\\\":-1,\\\"changeTime\\\":\\\"2017-04-11 17:53:11\\\",\\\"accountSetName\\\":\\\"商户测试应用1现金账户集\\\"},{\\\"bizOrderNo\\\":\\\"consumeApply_order_3\\\",\\\"curFreezenAmount\\\":0,\\\"tradeNo\\\":\\\"1704111644393570274812\\\",\\\"oriAmount\\\":0,\\\"curAmount\\\":1,\\\"chgAmount\\\":1,\\\"changeTime\\\":\\\"2017-04-11 16:44:39\\\",\\\"accountSetName\\\":\\\"商户测试应用1现金账户集\\\"}],\\\"bizUserId\\\":\\\"88888\\\"}\"}";
        JsonNode jsonNode = JsonUtil.getJsonNode(str);
        System.out.println(jsonNode);
    }

}
