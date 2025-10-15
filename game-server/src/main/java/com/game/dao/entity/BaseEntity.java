package com.game.dao.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 基础实体类
 */
public abstract class BaseEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public abstract String getIdx();
}