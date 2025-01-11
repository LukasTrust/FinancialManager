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

    @Test
    void addBankAccount(){
        String addBankAccount = contentController.getAddBankPage(new Model() {
            @Override
            public Model addAttribute(String attributeName, Object attributeValue) {
                return null;
            }

            @Override
            public Model addAttribute(Object attributeValue) {
                return null;
            }

            @Override
            public Model addAllAttributes(Collection<?> attributeValues) {
                return null;
            }

            @Override
            public Model addAllAttributes(Map<String, ?> attributes) {
                return null;
            }

            @Override
            public Model mergeAttributes(Map<String, ?> attributes) {
                return null;
            }

            @Override
            public boolean containsAttribute(String attributeName) {
                return false;
            }

            @Override
            public Object getAttribute(String attributeName) {
                return null;
            }

            @Override
            public Map<String, Object> asMap() {
                return Map.of();
            }
        });
        assertThat(addBankAccount).isNotNull();
    }
}