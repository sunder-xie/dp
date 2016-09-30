package dp.dao.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface CommonMapper {

    List<Map<String, Object>> selectListBySql(@Param("sql")String sql);

    List<Map<String, Object>> selectList(@Param("dataSql")String dataSql, @Param("table")String table,
                                         @Param("whereSql")String whereSql);

    List<String> selectOneFieldBySql(@Param("sql")String sql);

}