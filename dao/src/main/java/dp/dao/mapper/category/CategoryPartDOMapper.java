package dp.dao.mapper.category;

import dp.beans.category.CategoryPartDO;

public interface CategoryPartDOMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(CategoryPartDO record);

    int insertSelective(CategoryPartDO record);

    CategoryPartDO selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CategoryPartDO record);

    int updateByPrimaryKey(CategoryPartDO record);
}