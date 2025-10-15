package com.game.dao.mapper;

import com.game.dao.entity.Counter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Options;

/**
 * Counter数据访问接口
 * @author CodeGenerator
 * 自动生成，请勿手动修改
 */
@Mapper
public interface CounterDao extends BaseDao<Counter> {

    @Select("SELECT * FROM counter WHERE playerid = #{id}")
    Counter selectById(@Param("id") Long id);

    @Select("SELECT * FROM counter")
    java.util.List<Counter> selectAll();

    @Insert("INSERT INTO counter (playerid, counterdata, createtime, updatetime) VALUES (#{playerid}, #{counterdata}, #{createtime}, #{updatetime})")
    @Options(useGeneratedKeys = true, keyProperty = "playerid")
    int insert(Counter entity);

    @Update("UPDATE counter SET counterdata = #{counterdata}, createtime = #{createtime}, updatetime = #{updatetime} WHERE playerid = #{playerid}")
    int update(Counter entity);

    @Delete("DELETE FROM counter WHERE playerid = #{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM counter")
    long count();

}