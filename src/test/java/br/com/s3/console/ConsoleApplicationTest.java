package br.com.s3.console;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.shell.interactive.enabled=false",
    "spring.shell.script.enabled=false"
})
@DisplayName("Tests for ConsoleApplication class")
class ConsoleApplicationTest {

    @Test
    @DisplayName("Given Spring Boot application, when context is loaded, then it should load without errors")
    void givenSpringBootApplication_whenContextLoads_thenShouldLoadWithoutErrors() {
        // This test verifies if the Spring Boot application context loads correctly
        // No assertions are needed, as the test will fail if the context doesn't load
    }
}