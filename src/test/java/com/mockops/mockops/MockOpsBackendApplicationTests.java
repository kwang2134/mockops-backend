package com.mockops.mockops;

import com.mockops.config.TestMailConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestMailConfig.class)
class MockOpsBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
