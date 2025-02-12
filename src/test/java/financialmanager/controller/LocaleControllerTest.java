package financialmanager.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class LocaleControllerTest {

    @Autowired
    private LocaleController localeController;

    @Test
    void contextLoads() throws Exception {
        assertThat(localeController).isNotNull();
    }
}