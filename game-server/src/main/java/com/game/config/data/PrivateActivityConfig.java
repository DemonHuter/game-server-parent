package com.game.config.data;

import java.io.Serializable;
import java.util.*;
import java.math.BigDecimal;

/**
 * PrivateActivityConfig配置实体类
 * 自动生成的配置实体类，请勿手动修改
 */
public class PrivateActivityConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /** int */
    private Integer starTimeDay;

    /** string */
    private String param;

    /** int */
    private Integer endTimeDay;

    /** int */
    private Integer id;

    /** int */
    private Integer mainLevel;

    /** int */
    private Integer systemUnlockId;

    /** string */
    private String selectBonus;

    public PrivateActivityConfig() {}

    public Integer getStarTimeDay() {
        return starTimeDay;
    }

    public void setStarTimeDay(Integer starTimeDay) {
        this.starTimeDay = starTimeDay;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public Integer getEndTimeDay() {
        return endTimeDay;
    }

    public void setEndTimeDay(Integer endTimeDay) {
        this.endTimeDay = endTimeDay;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMainLevel() {
        return mainLevel;
    }

    public void setMainLevel(Integer mainLevel) {
        this.mainLevel = mainLevel;
    }

    public Integer getSystemUnlockId() {
        return systemUnlockId;
    }

    public void setSystemUnlockId(Integer systemUnlockId) {
        this.systemUnlockId = systemUnlockId;
    }

    public String getSelectBonus() {
        return selectBonus;
    }

    public void setSelectBonus(String selectBonus) {
        this.selectBonus = selectBonus;
    }

    @Override
    public String toString() {
        return "PrivateActivityConfig{" +
                "starTimeDay=" + starTimeDay +
                ", param=" + param +
                ", endTimeDay=" + endTimeDay +
                ", id=" + id +
                ", mainLevel=" + mainLevel +
                ", systemUnlockId=" + systemUnlockId +
                ", selectBonus=" + selectBonus +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrivateActivityConfig that = (PrivateActivityConfig) o;
        return Objects.equals(starTimeDay, that.starTimeDay) &&
                Objects.equals(param, that.param) &&
                Objects.equals(endTimeDay, that.endTimeDay) &&
                Objects.equals(id, that.id) &&
                Objects.equals(mainLevel, that.mainLevel) &&
                Objects.equals(systemUnlockId, that.systemUnlockId) &&
                Objects.equals(selectBonus, that.selectBonus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(starTimeDay, param, endTimeDay, id, mainLevel, systemUnlockId, selectBonus);
    }
}
