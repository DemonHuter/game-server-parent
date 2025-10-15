package com.game.config.data;

import java.io.Serializable;
import java.util.*;
import java.math.BigDecimal;

/**
 * PlayerModel配置实体类
 * 自动生成的配置实体类，请勿手动修改
 */
public class PlayerModel implements Serializable {

    private static final long serialVersionUID = 1L;

    public PlayerModel() {}

    @Override
    public String toString() {
        return "PlayerModel{" +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerModel that = (PlayerModel) o;
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash();
    }
}
