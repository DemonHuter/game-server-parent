package com.game.config.data;

import java.io.Serializable;
import java.util.*;
import java.math.BigDecimal;

/**
 * AdditionConfig配置实体类
 * 自动生成的配置实体类，请勿手动修改
 */
public class AdditionConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /** intIntMap */
    private Map<Integer, Integer> mapValue;

    /** intList */
    private List<Integer> listValue;

    /** int */
    private Integer effectId;

    /** int */
    private Integer intValue;

    /** int */
    private Integer validTime;

    /** int */
    private Integer id;

    /** int */
    private Integer type;

    public AdditionConfig() {}

    public Map<Integer, Integer> getMapValue() {
        return mapValue;
    }

    public void setMapValue(Map<Integer, Integer> mapValue) {
        this.mapValue = mapValue;
    }

    public List<Integer> getListValue() {
        return listValue;
    }

    public void setListValue(List<Integer> listValue) {
        this.listValue = listValue;
    }

    public Integer getEffectId() {
        return effectId;
    }

    public void setEffectId(Integer effectId) {
        this.effectId = effectId;
    }

    public Integer getIntValue() {
        return intValue;
    }

    public void setIntValue(Integer intValue) {
        this.intValue = intValue;
    }

    public Integer getValidTime() {
        return validTime;
    }

    public void setValidTime(Integer validTime) {
        this.validTime = validTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "AdditionConfig{" +
                "mapValue=" + mapValue +
                ", listValue=" + listValue +
                ", effectId=" + effectId +
                ", intValue=" + intValue +
                ", validTime=" + validTime +
                ", id=" + id +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdditionConfig that = (AdditionConfig) o;
        return mapValue == that.mapValue &&
                listValue == that.listValue &&
                Objects.equals(effectId, that.effectId) &&
                Objects.equals(intValue, that.intValue) &&
                Objects.equals(validTime, that.validTime) &&
                Objects.equals(id, that.id) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mapValue, listValue, effectId, intValue, validTime, id, type);
    }
}
