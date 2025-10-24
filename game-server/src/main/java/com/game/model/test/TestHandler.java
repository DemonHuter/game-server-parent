package com.game.model.test;

import com.game.core.GameHandler;
import com.game.core.GameMessageHandler;
import com.game.proto.Test_Req;
import com.game.proto.msgid.MsgIdEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 系统模块处理器
 * 处理系统相关的消息，如心跳等
 */
@Component
@GameHandler(msgId = MsgIdEnum.CS_test_VALUE)
public class TestHandler extends GameMessageHandler<Test_Req> {

    private static final Logger logger = LoggerFactory.getLogger(TestHandler.class);

    @Autowired
    private TestManager testManager;
    

    @Override
    public void handleMessage(Test_Req msg) throws Exception {
        logger.debug("Test from {}: {}", getCtx().channel().remoteAddress(), msg.getReq());


        // 调用业务逻辑管理器处理心跳请求
        testManager.test(getCtx(), msg, getSequence());
    }

}