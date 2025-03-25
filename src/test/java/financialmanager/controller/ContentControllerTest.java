package financialmanager.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.Model;

import java.util.Collection;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class ContentControllerTest {

    @Autowired
    private ContentController contentController;

    @Test
    void contextLoads() throws Exception {
        assertThat(contentController).isNotNull();
    }

    @Test
    void login() {
        String login = contentController.getLoginPage();
        assertThat(login).isNotNull();
    }

    @Test
    void signup() {
        String signup = contentController.getSignupPage();
        assertThat(signup).isNotNull();
    }
}