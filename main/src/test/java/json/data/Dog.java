package json.data;

import lombok.Data;

import java.util.Date;

/**
 * Created by huangzhangting on 17/4/18.
 */
@Data
public class Dog {
    private Integer id;
    private String name;
    private Date birthday;
}
