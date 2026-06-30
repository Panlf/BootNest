package com.boot.disruptor.service.impl;

import com.lmax.disruptor.RingBuffer;
import com.boot.disruptor.model.MessageModel;
import com.boot.disruptor.service.DisruptorMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author panlf
 * @date 2023/1/19
 */
@Slf4j
@Service
public class DisruptorMessageServiceImpl implements DisruptorMessageService {
    @Resource
    private RingBuffer<MessageModel> messageModelRingBuffer;

    @Override
    public void createMessage(String message) {
        log.info("收到 message: {}", message);
        long sequence = messageModelRingBuffer.next();
        try {
            MessageModel event = messageModelRingBuffer.get(sequence);
            event.setMessage(message);
            log.info("往消息队列中添加消息：{}", event);
        } catch (Exception e) {
            log.error("failed to add event to messageModelRingBuffer: {}", e.getMessage(), e);
        } finally {
            messageModelRingBuffer.publish(sequence);
        }
    }
}
