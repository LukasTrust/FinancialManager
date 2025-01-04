package financialmanager.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/localization")
public class LocalizationController {

    @GetMapping("/{subFolder}/messages_{locale}.json")
    public ResponseEntity<?> getLocalization(@PathVariable String subFolder, @PathVariable String locale) {
        try {
            String filePath = "localization/" + subFolder + "/messages_" + locale + ".json";
            Resource resource = new ClassPathResource(filePath);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Files.readString(resource.getFile().toPath()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Localization file not found");
        }
    }
}
