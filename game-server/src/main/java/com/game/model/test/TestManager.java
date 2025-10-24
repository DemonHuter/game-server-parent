package com.game.model.test;

import com.game.core.AsyncMessageUtils;
import com.game.proto.Test_Req;
import com.game.proto.Test_Res;
import com.game.proto.msgid.MsgIdEnum;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Service;

/**
 * @description: <TODO description class purpose>
 * @author: guosheng
 * @date: 2025/10/24 10:49
 */
@Service
public class TestManager {

    public void test(ChannelHandlerContext ctx, Test_Req req,int sequence) throws Exception {
        Test_Res response = Test_Res.newBuilder()
                .setRes("receive " + req.getReq() + " response!!!!")
                .build();

        AsyncMessageUtils.sendPlayerMsgAsync(ctx, MsgIdEnum.SC_test_VALUE, sequence, response.toByteString());
    }
}
