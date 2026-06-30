package com.boot.disruptor.manager;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.boot.disruptor.factory.MessageEventFactory;
import com.boot.disruptor.handler.MessageConsumer;
import com.boot.disruptor.model.MessageModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author panlf
 * @date 2023/1/19
 */
@Configuration
public class QueueManager {

    @Bean("messageModelRingBuffer")
    public RingBuffer<MessageModel> messageModelRingBuffer() {
        MessageEventFactory factory = new MessageEventFactory();
        int bufferSize = 1024 * 256;

        Disruptor<MessageModel> disruptor = new Disruptor<>(
                factory, bufferSize, DaemonThreadFactory.INSTANCE,
                ProducerType.SINGLE, new BlockingWaitStrategy());

        disruptor.handleEventsWith(new MessageConsumer());
        disruptor.start();

        Runtime.getRuntime().addShutdownHook(new Thread(disruptor::shutdown));

        return disruptor.getRingBuffer();
    }
}
