package com.ticketaca;

import com.ticketaca.global.config.TestcontainersConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
@Import(TestcontainersConfig.class)
class TickeTacaApplicationTests {

    @DisplayName("Spring Context가 정상적으로 로드된다")
    @Test
    void contextLoads() {
    }
}
