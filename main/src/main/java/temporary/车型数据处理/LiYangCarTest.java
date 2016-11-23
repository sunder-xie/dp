package temporary.车型数据处理;

import base.BaseTest;
import dp.beans.car.CarInfoAllDO;
import dp.common.util.Constant;
import dp.common.util.IoUtil;
import dp.common.util.ObjectUtil;
import dp.common.util.Print;
import dp.common.util.excelutil.CommReaderXLS;
import dp.common.util.excelutil.PoiUtil;
import dp.dao.mapper.car.CarInfoAllDOMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by huangzhangting on 16/9/20.
 */
public class LiYangCarTest extends BaseTest {
    @Test
    public void justTest() throws Exception{

    }


    @Test
    public void testPrice() throws Exception{
        path = "/Users/huangzhangting/Desktop/临时数据处理/";
        String excel = path + "需要处理价格的力洋车型.xls";

        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("leyel_id", "lid");
        attrMap.put("guide_price", "price");

        CommReaderXLS readerXLS = new CommReaderXLS(attrMap, Constant.TYPE_LIST, 0);
        readerXLS.process(excel, attrMap.size());
        List<Map<String, String>> dataList = readerXLS.getDataList();
        Print.info(dataList.size());
        Print.info(dataList.get(0));

        writer = IoUtil.getWriter(path + "update_guide_price.sql");

        for(Map<String, String> data : dataList){
            String[] ps = data.get("price").split("-");
            DecimalFormat df = new DecimalFormat("0.00");
            df.setRoundingMode(RoundingMode.HALF_UP);
            double gp = (Double.parseDouble(ps[0])+Double.parseDouble(ps[1]))/2;

            String sql = "update db_car_info_all set guide_price='"+df.format(gp)
                    +"' where leyel_id='"+data.get("lid")+"';\n";

            IoUtil.writeFile(writer, sql);
        }

        IoUtil.closeWriter(writer);
    }


    /** 导出力洋车型数据 */
    private String lyCarInfoSql(){
        Map<String, String> map = lyCarFieldMap();

        StringBuilder sql = new StringBuilder();
        for(String field : map.values()){
            sql.append(",").append(field);
        }
        sql.deleteCharAt(0);
        sql.insert(0, "select ");
        sql.append(" from db_car_info_all");
        sql.append(" order by car_brand, factory_name, car_series, vehicle_type, model_year, displacement");
        return sql.toString();
    }

    private Map<String, String> lyCarFieldMap(){
        Map<String, String> map = new HashMap<>();
        map.put("id", "leyel_id");
        map.put("品牌", "car_brand");
        map.put("厂家", "factory_name");
        map.put("车系", "car_series");
        map.put("车型", "vehicle_type");
        map.put("年款", "model_year");
        map.put("排量", "displacement");
        map.put("销售名称", "market_name");
        map.put("生产年份", "create_year");
        map.put("进气形式", "intake_style");
        map.put("燃油类型", "fuel_type");
        map.put("变速器类型", "transmission_type");
        map.put("变速器描述", "transmission_desc");
        map.put("档位数", "stall_num");
        map.put("最大功率", "max_power");

        return map;
    }

    //TODO 导出力洋车型数据
    @Test
    public void exportLyCarExcel() throws Exception{

        path = "/Users/huangzhangting/Desktop/力洋车型数据/";
        IoUtil.mkdirsIfNotExist(path);

        String[] heads = new String[]{"id", "品牌", "厂家", "车系", "车型", "年款", "排量", "销售名称",
                "生产年份", "进气形式", "燃油类型", "变速器类型", "变速器描述", "档位数", "最大功率"};

        int size = heads.length;
        String[] fields = new String[size];
        Map<String, String> map = lyCarFieldMap();
        for(int i=0; i<size; i++){
            fields[i] = map.get(heads[i]);
        }

        List<Map<String, Object>> lyCarInfoList = commonMapper.selectListBySql(lyCarInfoSql());
        Print.info(lyCarInfoList.size());
        Print.info(lyCarInfoList.get(0));

        List<Map<String, String>> mapList = ObjectUtil.objToStrMapList(lyCarInfoList);

        PoiUtil poiUtil = new PoiUtil();
        poiUtil.exportXlsxWithMap("力洋车型数据", path, heads, fields, mapList);
    }


    @Autowired
    private CarInfoAllDOMapper carInfoAllDOMapper;

    @Test
    public void test_export() throws Exception{
        path = "/Users/huangzhangting/Desktop/力洋车型数据/";
        IoUtil.mkdirsIfNotExist(path);

        List<CarInfoAllDO> carInfoAllDOs = carInfoAllDOMapper.selectAllCarList();
        Print.printList(carInfoAllDOs);

        String[] headName = {
                "力洋ID", "厂家", "品牌", "车系", "车型", "销售名称", "年款", "排放标准", "车辆类型", "车辆级别"
                , "指导价格(万元)", "上市年份", "上市月份", "生产年份", "停产年份", "生产状态", "销售状态", "国别", "国产合资进口", "气缸容积"
                , "排量(升)", "进气形式", "燃料类型", "燃油标号", "最大马力(ps)", "最大功率(kw)", "最大功率转速(rpm)", "最大扭矩(N·m)", "最大扭矩转速(rpm)", "气缸排列形式"
                , "气缸数(个)", "每缸气门数(个)", "压缩比", "供油方式", "工信部综合油耗", "市区工况油耗", "市郊工况油耗", "加速时间(0-100km/h)", "最高车速", "发动机特有技术"
                , "三元催化器", "冷却方式", "缸径", "行程", "发动机描述", "变速器类型", "变速器描述", "档位数", "前制动器类型", "后制动器类型"
                , "前悬挂类型", "后悬挂类型", "转向机形式", "助力类型", "最小离地间隙", "最小转弯半径", "离去角", "接近角", "发动机位置", "驱动方式"
                , "驱动形式", "车身型式", "长度(mm)", "宽度(mm)", "高度(mm)", "轴距(mm)", "前轮距(mm)", "后轮距(mm)", "整备质量(kg)", "最大载重质量(kg)"
                , "油箱容积(L)", "行李厢容积(L)", "车顶型式", "车篷型式", "车门数", "座位数", "前轮胎规格", "后轮胎规格", "前轮毂规格", "后轮毂规格"
                , "轮毂材料", "备胎规格", "电动天窗", "全景天窗", "氙气大灯", "前雾灯", "后雨刷", "空调", "自动空调"
        };

        String[] fieldName = {
                "leyelId", "factoryName", "carBrand", "carSeries", "vehicleType", "marketName", "modelYear", "envStandard", "carType", "carLevel"
                , "guidePrice", "publicYear", "publicMonth", "createYear", "stopYear", "productionStatus", "marketStatus", "productionCountry", "productionType", "cylinderCapacity"
                , "displacement", "intakeStyle", "fuelType", "fuelFlag", "maxHorsepower", "maxPower", "maxPowerSpeed", "maxTorque", "maxTorqueSpeed", "cylinderStyle"
                , "cylinderNum", "valvePerCylinder", "compressionRatio", "fuelWay", "fuelConsumptionAverage", "fuelConsumptionDowntown", "fuelConsumptionSuburbs", "accelerationTime", "topSpeed", "engineUniqueTech"
                , "catalyticConverter", "coolStyle", "bore", "stroke", "engineDesc", "transmissionType", "transmissionDesc", "stallNum", "frontBrakeType", "backBrakeType"
                , "frontSuspensionNum", "rearSuspensionType", "steeringStyle", "boosterType", "minClearance", "minTurningRadius", "departureAngle", "approachAngle", "enginePosition", "driveWay"
                , "driveStyle", "bodyStyle", "length", "width", "height", "wheelbase", "frontTread", "rearTread", "curbWeight", "fullyLoadedWeight"
                , "tankCapacity", "trunkCapacity", "roofType", "hoodType", "doorNum", "seatNum", "frontTireStyle", "rearTireStyle", "frontWheelStyle", "rearWheelStyle"
                , "wheelMaterial", "spareTireStyle", "electronicSkylights", "panoramicSunroof", "xenonLamp", "frontFogLamp", "backWindshielWiper", "airConditioning", "autoAirConditioning"
        };

        PoiUtil poiUtil = new PoiUtil();
        poiUtil.exportXLSX("力洋车型数据", path, headName, fieldName, carInfoAllDOs);

    }

}
