package com.game.dao.mapper;

import com.game.dao.entity.Player;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Options;

/**
 * Player数据访问接口
 * @author CodeGenerator
 * 自动生成，请勿手动修改
 */
@Mapper
public interface PlayerDao extends BaseDao<Player> {

    @Select("SELECT * FROM player WHERE playerid = #{playerid}")
    Player selectById(@Param("playerid") Long playerid);

    @Select("SELECT * FROM player")
    java.util.List<Player> selectAll();

    @Insert("INSERT INTO player (userid, playerid, name, macaddr, channel, serverindex, language, createtime, logintime, logouttime, gm, platform, devicetype, networktype, sourceid, totalcharge, totalprice, paygroupids, paylimit, firstbuy, secondbuy, thirdbuy, buytime, pushset, deviceversion, state, createnum) VALUES (#{userid}, #{playerid}, #{name}, #{macaddr}, #{channel}, #{serverindex}, #{language}, #{createtime}, #{logintime}, #{logouttime}, #{gm}, #{platform}, #{devicetype}, #{networktype}, #{sourceid}, #{totalcharge}, #{totalprice}, #{paygroupids}, #{paylimit}, #{firstbuy}, #{secondbuy}, #{thirdbuy}, #{buytime}, #{pushset}, #{deviceversion}, #{state}, #{createnum})")
    @Options(useGeneratedKeys = true, keyProperty = "playerid")
    int insert(Player entity);

    @Update("UPDATE player SET userid = #{userid}, name = #{name}, macaddr = #{macaddr}, channel = #{channel}, serverindex = #{serverindex}, language = #{language}, createtime = #{createtime}, logintime = #{logintime}, logouttime = #{logouttime}, gm = #{gm}, platform = #{platform}, devicetype = #{devicetype}, networktype = #{networktype}, sourceid = #{sourceid}, totalcharge = #{totalcharge}, totalprice = #{totalprice}, paygroupids = #{paygroupids}, paylimit = #{paylimit}, firstbuy = #{firstbuy}, secondbuy = #{secondbuy}, thirdbuy = #{thirdbuy}, buytime = #{buytime}, pushset = #{pushset}, deviceversion = #{deviceversion}, state = #{state}, createnum = #{createnum} WHERE playerid = #{playerid}")
    int update(Player entity);

    @Delete("DELETE FROM player WHERE playerid = #{playerid}")
    int deleteById(@Param("playerid") Long playerid);

    @Select("SELECT COUNT(*) FROM player")
    long count();

}
