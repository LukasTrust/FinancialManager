package financialmanager.controller;

import financialmanager.generalController.ContentController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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

    @Test
    void addBank(){
        String addBank = contentController.getAddBankPage();
        assertThat(addBank).isNotNull();
    }
}