package json.data;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by huangzhangting on 17/4/18.
 */
@Data
public class Person {
    private Integer id;
    private String name;
    private Date birthday;
    private List<Dog> dogs;
    private House house;
    private Set<House> houseSet;
}
