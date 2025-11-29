package com.interview_scheduler.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
class BackendApplicationTests {

	@Test
	void contextLoads() {
		assertTrue(true, "Application context should load");
	}
}
