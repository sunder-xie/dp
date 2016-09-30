package crawl.jd;

import dp.common.util.StrUtil;

/**
 * Created by huangzhangting on 16/4/15.
 */
public class BrandUtil {
    public static String handleBrand(String brand){
        String b = brand.replace("（", "(");
        int idx = b.indexOf("(");
        if(idx>-1){
            return StrUtil.toUpCase(b.substring(0, idx));
        }
        return StrUtil.toUpCase(brand);
    }
}
