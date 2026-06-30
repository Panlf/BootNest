package com.boot.intercept;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BootInterceptApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void publicEndpoint_shouldSucceed() throws Exception {
        mockMvc.perform(get("/api/public/greet").param("name", "MiMo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Hello, MiMo"));
    }

    @Test
    void healthEndpoint_shouldSucceed() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("UP"));
    }

    @Test
    void protectedEndpoint_withoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/data/summary"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void protectedEndpoint_withValidToken_shouldSucceed() throws Exception {
        mockMvc.perform(get("/api/data/summary")
                        .header("Authorization", "Bearer test-token-user001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.service").value("boot-intercept"));
    }

    @Test
    void saveData_withToken_shouldSucceed() throws Exception {
        mockMvc.perform(post("/api/data/save")
                        .header("Authorization", "Bearer test-token-user001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"key\":\"value\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void rateLimitEndpoint_exceedingLimit_shouldReturn429() throws Exception {
        String token = "Bearer test-token-user002";
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/api/data/list").header("Authorization", token))
                    .andExpect(status().isOk());
        }
        mockMvc.perform(get("/api/data/list").header("Authorization", token))
                .andExpect(status().is(429))
                .andExpect(jsonPath("$.code").value(429));
    }
}
