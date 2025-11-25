package com.game.config.data;

import java.io.Serializable;
import java.util.*;
import java.math.BigDecimal;

/**
 * ExampleConfig配置实体类
 * 自动生成的配置实体类，请勿手动修改
 */
public class ExampleConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /** intList2 */
    private List<List<Integer>> pIntList2;

    /** intIntMap */
    private Map<Integer, Integer> pIntIntMap;

    /** string */
    private String pStr;

    /** strStrMap */
    private Map<String, String> pStrStrMap;

    /** float */
    private Float pFloat;

    /** intStrMap */
    private Map<String, String> pIntStrMap;

    /** stringList */
    private List<String> pStrList;

    /** int */
    private Integer Id;

    /** int */
    private Integer pInt;

    /** intList */
    private List<Integer> pIntList;

    /** stringList2 */
    private List<List<String>> pStrList2;

    public ExampleConfig() {}

    public List<List<Integer>> getPIntList2() {
        return pIntList2;
    }

    public void setPIntList2(List<List<Integer>> pIntList2) {
        this.pIntList2 = pIntList2;
    }

    public Map<Integer, Integer> getPIntIntMap() {
        return pIntIntMap;
    }

    public void setPIntIntMap(Map<Integer, Integer> pIntIntMap) {
        this.pIntIntMap = pIntIntMap;
    }

    public String getPStr() {
        return pStr;
    }

    public void setPStr(String pStr) {
        this.pStr = pStr;
    }

    public Map<String, String> getPStrStrMap() {
        return pStrStrMap;
    }

    public void setPStrStrMap(Map<String, String> pStrStrMap) {
        this.pStrStrMap = pStrStrMap;
    }

    public Float getPFloat() {
        return pFloat;
    }

    public void setPFloat(Float pFloat) {
        this.pFloat = pFloat;
    }

    public Map<String, String> getPIntStrMap() {
        return pIntStrMap;
    }

    public void setPIntStrMap(Map<String, String> pIntStrMap) {
        this.pIntStrMap = pIntStrMap;
    }

    public List<String> getPStrList() {
        return pStrList;
    }

    public void setPStrList(List<String> pStrList) {
        this.pStrList = pStrList;
    }

    public Integer getId() {
        return Id;
    }

    public void setId(Integer Id) {
        this.Id = Id;
    }

    public Integer getPInt() {
        return pInt;
    }

    public void setPInt(Integer pInt) {
        this.pInt = pInt;
    }

    public List<Integer> getPIntList() {
        return pIntList;
    }

    public void setPIntList(List<Integer> pIntList) {
        this.pIntList = pIntList;
    }

    public List<List<String>> getPStrList2() {
        return pStrList2;
    }

    public void setPStrList2(List<List<String>> pStrList2) {
        this.pStrList2 = pStrList2;
    }

    @Override
    public String toString() {
        return "ExampleConfig{" +
                "pIntList2=" + pIntList2 +
                ", pIntIntMap=" + pIntIntMap +
                ", pStr=" + pStr +
                ", pStrStrMap=" + pStrStrMap +
                ", pFloat=" + pFloat +
                ", pIntStrMap=" + pIntStrMap +
                ", pStrList=" + pStrList +
                ", Id=" + Id +
                ", pInt=" + pInt +
                ", pIntList=" + pIntList +
                ", pStrList2=" + pStrList2 +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExampleConfig that = (ExampleConfig) o;
        return pIntList2 == that.pIntList2 &&
                pIntIntMap == that.pIntIntMap &&
                Objects.equals(pStr, that.pStr) &&
                pStrStrMap == that.pStrStrMap &&
                Objects.equals(pFloat, that.pFloat) &&
                pIntStrMap == that.pIntStrMap &&
                pStrList == that.pStrList &&
                Objects.equals(Id, that.Id) &&
                Objects.equals(pInt, that.pInt) &&
                pIntList == that.pIntList &&
                pStrList2 == that.pStrList2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pIntList2, pIntIntMap, pStr, pStrStrMap, pFloat, pIntStrMap, pStrList, Id, pInt, pIntList, pStrList2);
    }
}
