package dp.dao.mapper.car;

import dp.beans.car.CarInfoAllDO;

import java.util.List;

public interface CarInfoAllDOMapper {
    int deleteByPrimaryKey(Integer carModel);

    int insert(CarInfoAllDO record);

    int insertSelective(CarInfoAllDO record);

    CarInfoAllDO selectByPrimaryKey(Integer carModel);

    int updateByPrimaryKeySelective(CarInfoAllDO record);

    int updateByPrimaryKey(CarInfoAllDO record);


    List<CarInfoAllDO> selectAllCarList();

}