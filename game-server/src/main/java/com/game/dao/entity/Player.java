package com.game.dao.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * Player实体类
 * 表名: player
 * @author CodeGenerator
 * 自动生成，请勿手动修改
 */
public class Player extends BaseEntity {

    private String userid;

    private Long playerid;

    private String name;

    private String macaddr;

    private String channel;

    private Integer serverindex;

    private String language;

    private Long createtime;

    private Long logintime;

    private Long logouttime;

    private Integer gm;

    private String platform;

    private String devicetype;

    private String networktype;

    private String sourceid;

    private Long totalcharge;

    private java.math.BigDecimal totalprice;

    private String paygroupids;

    private String paylimit;

    private Integer firstbuy;

    private Integer secondbuy;

    private Integer thirdbuy;

    private Long buytime;

    /** 推送设置 */
    private String pushset;

    private String deviceversion;

    private String state;

    private Integer createnum;

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public Long getPlayerid() {
        return playerid;
    }

    public void setPlayerid(Long playerid) {
        this.playerid = playerid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMacaddr() {
        return macaddr;
    }

    public void setMacaddr(String macaddr) {
        this.macaddr = macaddr;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public Integer getServerindex() {
        return serverindex;
    }

    public void setServerindex(Integer serverindex) {
        this.serverindex = serverindex;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Long getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Long createtime) {
        this.createtime = createtime;
    }

    public Long getLogintime() {
        return logintime;
    }

    public void setLogintime(Long logintime) {
        this.logintime = logintime;
    }

    public Long getLogouttime() {
        return logouttime;
    }

    public void setLogouttime(Long logouttime) {
        this.logouttime = logouttime;
    }

    public Integer getGm() {
        return gm;
    }

    public void setGm(Integer gm) {
        this.gm = gm;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getDevicetype() {
        return devicetype;
    }

    public void setDevicetype(String devicetype) {
        this.devicetype = devicetype;
    }

    public String getNetworktype() {
        return networktype;
    }

    public void setNetworktype(String networktype) {
        this.networktype = networktype;
    }

    public String getSourceid() {
        return sourceid;
    }

    public void setSourceid(String sourceid) {
        this.sourceid = sourceid;
    }

    public Long getTotalcharge() {
        return totalcharge;
    }

    public void setTotalcharge(Long totalcharge) {
        this.totalcharge = totalcharge;
    }

    public java.math.BigDecimal getTotalprice() {
        return totalprice;
    }

    public void setTotalprice(java.math.BigDecimal totalprice) {
        this.totalprice = totalprice;
    }

    public String getPaygroupids() {
        return paygroupids;
    }

    public void setPaygroupids(String paygroupids) {
        this.paygroupids = paygroupids;
    }

    public String getPaylimit() {
        return paylimit;
    }

    public void setPaylimit(String paylimit) {
        this.paylimit = paylimit;
    }

    public Integer getFirstbuy() {
        return firstbuy;
    }

    public void setFirstbuy(Integer firstbuy) {
        this.firstbuy = firstbuy;
    }

    public Integer getSecondbuy() {
        return secondbuy;
    }

    public void setSecondbuy(Integer secondbuy) {
        this.secondbuy = secondbuy;
    }

    public Integer getThirdbuy() {
        return thirdbuy;
    }

    public void setThirdbuy(Integer thirdbuy) {
        this.thirdbuy = thirdbuy;
    }

    public Long getBuytime() {
        return buytime;
    }

    public void setBuytime(Long buytime) {
        this.buytime = buytime;
    }

    public String getPushset() {
        return pushset;
    }

    public void setPushset(String pushset) {
        this.pushset = pushset;
    }

    public String getDeviceversion() {
        return deviceversion;
    }

    public void setDeviceversion(String deviceversion) {
        this.deviceversion = deviceversion;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Integer getCreatenum() {
        return createnum;
    }

    public void setCreatenum(Integer createnum) {
        this.createnum = createnum;
    }

    @Override
    public String getIdx() {
        return String.valueOf(getPlayerid());
    }

    @Override
    public String toString() {
        return String.format("Player{userid='%s', playerid=%s, name='%s', macaddr='%s', channel='%s', serverindex=%s, language='%s', createtime=%s, logintime=%s, logouttime=%s, gm=%s, platform='%s', devicetype='%s', networktype='%s', sourceid='%s', totalcharge=%s, totalprice=%s, paygroupids='%s', paylimit='%s', firstbuy=%s, secondbuy=%s, thirdbuy=%s, buytime=%s, pushset='%s', deviceversion='%s', state='%s', createnum=%s}", 
                getUserid(), getPlayerid(), getName(), getMacaddr(), getChannel(), getServerindex(), getLanguage(), getCreatetime(), getLogintime(), getLogouttime(), getGm(), getPlatform(), getDevicetype(), getNetworktype(), getSourceid(), getTotalcharge(), getTotalprice(), getPaygroupids(), getPaylimit(), getFirstbuy(), getSecondbuy(), getThirdbuy(), getBuytime(), getPushset(), getDeviceversion(), getState(), getCreatenum());
    }
}
