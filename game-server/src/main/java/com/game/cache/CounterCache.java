package com.game.cache;

import com.game.dao.entity.Counter;
import com.game.dao.mapper.CounterDao;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Counter缓存类
 * @author CodeGenerator
 * 自动生成，请勿手动修改
 */
@Component
public class CounterCache extends BaseCache<Counter> {

    private final CounterDao counterdao;

    public CounterCache(CounterDao counterdao) {
        super(counterdao);
        this.counterdao = counterdao;
    }

}