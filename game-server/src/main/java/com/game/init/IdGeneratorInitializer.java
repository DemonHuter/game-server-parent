package com.game.init;

import com.game.cache.CacheManager;
import com.game.common.util.IdGenerator;
import com.game.constant.SystemInitializeOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * ID生成器初始化器
 * 在系统启动时初始化所有缓存
 */
@Component
public class IdGeneratorInitializer implements SystemInitializer {
    
    private static final Logger logger = LoggerFactory.getLogger(IdGeneratorInitializer.class);

    @Value("${game.serverIndex:0}")
    private int serverIndex;
    @Override
    public int getOrder() {
        return SystemInitializeOrder.IDGENERATOR;
    }
    
    @Override
    public void initialize() throws Exception {
        IdGenerator.getInstance().init(serverIndex);

        logger.info("IdGenerator initialized {}", serverIndex);
    }
}