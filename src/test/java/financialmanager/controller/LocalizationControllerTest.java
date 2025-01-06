package financialmanager.controller;

import financialmanager.generalController.LocalizationController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class LocalizationControllerTest {

    @Autowired
    private LocalizationController localizationController;

    @Test
    void contextLoads() throws Exception {
        assertThat(localizationController).isNotNull();
    }

    @Test
    void getLocalizationSubDirectoryNull(){
        ResponseEntity<?> response = localizationController.getLocalization(null, "en");
        assertThat(response != null);
        assertThat(response.getStatusCode() == HttpStatus.NOT_FOUND);
        assertThat(response.getBody() == "Localization file not found");
    }

    @Test
    void getLocalizationSubDirectoryEmpty(){
        ResponseEntity<?> response = localizationController.getLocalization("", "en");
        assertThat(response != null);
        assertThat(response.getStatusCode() == HttpStatus.NOT_FOUND);
        assertThat(response.getBody() == "Localization file not found");
    }

    @Test
    void getGeneralStringsNoLanguage() {
        ResponseEntity<?> response = localizationController.getLocalization("general", "");
        assertThat(response != null);
        assertThat(response.getStatusCode() == HttpStatus.NOT_FOUND);
        assertThat(response.getBody() == "Localization file not found");
    }

    @Test
    void getGeneralStringsEnglish() {
        ResponseEntity<?> response = localizationController.getLocalization("general", "en");
        assertThat(response != null);
        assertThat(response.getStatusCode() == HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void getGeneralStringsGerman() {
        ResponseEntity<?> response = localizationController.getLocalization("general", "de");
        assertThat(response != null);
        assertThat(response.getStatusCode() == HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void getLoginSignupStringsNoLanguage() {
        ResponseEntity<?> response = localizationController.getLocalization("login&Signup", "");
        assertThat(response != null);
        assertThat(response.getStatusCode() == HttpStatus.NOT_FOUND);
        assertThat(response.getBody() == "Localization file not found");
    }

    @Test
    void getLoginSignupStringsEnglish() {
        ResponseEntity<?> response = localizationController.getLocalization("login&Signup", "en");
        assertThat(response != null);
        assertThat(response.getStatusCode() == HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void getLoginSignupStringsGerman() {
        ResponseEntity<?> response = localizationController.getLocalization("login&Signup", "de");
        assertThat(response != null);
        assertThat(response.getStatusCode() == HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }
}