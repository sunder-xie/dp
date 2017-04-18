package json.data;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by huangzhangting on 17/4/18.
 */
@Data
public class House {
    private Integer id;
    private BigDecimal price;
    private String name;
}
