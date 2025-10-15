package com.game.dao.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * Counter实体类
 * 表名: counter
 * @author CodeGenerator
 * 自动生成，请勿手动修改
 */
public class Counter extends BaseEntity {

    /** 玩家ID（-1表示全局计数器） */
    private Long playerid;

    /** 计数器数据（JSON格式） */
    private String counterdata;

    /** 创建时间 */
    private Long createtime;

    /** 更新时间 */
    private Long updatetime;

    public Long getPlayerid() {
        return playerid;
    }

    public void setPlayerid(Long playerid) {
        this.playerid = playerid;
    }

    public String getCounterdata() {
        return counterdata;
    }

    public void setCounterdata(String counterdata) {
        this.counterdata = counterdata;
    }

    public Long getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Long createtime) {
        this.createtime = createtime;
    }

    public Long getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Long updatetime) {
        this.updatetime = updatetime;
    }

    @Override
    public String getIdx() {
        return String.valueOf(getPlayerid());
    }

    @Override
    public String toString() {
        return String.format("Counter{playerid=%s, counterdata='%s', createtime=%s, updatetime=%s}", 
                getPlayerid(), getCounterdata(), getCreatetime(), getUpdatetime());
    }
}
