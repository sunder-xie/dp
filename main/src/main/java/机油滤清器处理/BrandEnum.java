package 机油滤清器处理;

/**
 *
 * 商品品牌枚举
 *
 * Created by huangzhangting on 16/10/1.
 */
public enum BrandEnum {
    YUN_XIU(1, "云修"),
    BO_SHI(2, "博世"),
    AC_DE_KE(3, "AC德科"),
    JIAN_GUAN(4, "箭冠"),
    HAI_YE(5, "海业"),
    BAO_WANG(6, "豹王"),
    MA_LE(7, "马勒"),
    AO_SHENG(8, "奥盛"),;

    private int code;
    private String desc;

    BrandEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public int getCode() {
        return code;
    }

}
