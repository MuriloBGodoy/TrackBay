package com.trackwheel;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** O perfil dev fornece os repositorios in-memory que os services exigem. */
@SpringBootTest
@ActiveProfiles("dev")
class TrackWheelBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
