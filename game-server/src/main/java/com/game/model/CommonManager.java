package com.game.model;

import com.game.core.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @description: <TODO description class purpose>
 * @author: guosheng
 * @date: 2025/10/10 10:26
 */
@Component
public class CommonManager {
    @Autowired
    protected MessageSender messageSender;

}