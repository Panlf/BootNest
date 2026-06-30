package com.boot.disruptor;

import com.boot.disruptor.service.DisruptorMessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DisruptorApplicationTests {

    @Autowired
    private DisruptorMessageService disruptorMessageService;

    @Test
    void contextLoads() {
        assertNotNull(disruptorMessageService);
    }

    @Test
    void testSendMessage() {
        for (int i = 0; i < 5; i++) {
            disruptorMessageService.createMessage("test-message-" + i);
        }
    }

    @Test
    void testMultipleMessages() {
        int count = 10;
        for (int i = 0; i < count; i++) {
            disruptorMessageService.createMessage("batch-message-" + i);
        }
    }
}
