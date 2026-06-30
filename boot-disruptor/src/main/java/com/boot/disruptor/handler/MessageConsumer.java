package com.boot.disruptor.handler;

import com.lmax.disruptor.EventHandler;
import com.boot.disruptor.model.MessageModel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author panlf
 * @date 2023/1/19
 */
@Slf4j
public class MessageConsumer implements EventHandler<MessageModel> {
    @Override
    public void onEvent(MessageModel messageModel,
                        long l,
                        boolean b) throws Exception {
        try {
            Thread.sleep(1000);
            log.info("消费者处理消息开始");
            if (messageModel != null) {
                log.info("消费者消费的信息是：{}", messageModel);
            }
        } catch (Exception e) {
            log.error("消费者处理消息失败", e);
        }
        log.info("消费者处理消息结束");
    }
}
