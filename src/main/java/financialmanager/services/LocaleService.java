package financialmanager.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
@AllArgsConstructor
public class LocaleService {
    private final Map<Long, Locale> locales = new HashMap<>();

    public Locale getCurrentLocale(Long userId) {
        if (locales.containsKey(userId)) {
            return locales.get(userId);
        }

        return Locale.ENGLISH;
    }

    public void setCurrentLocale(Locale locale, Long userId) {
        if (userId == null) {
            return;
        }

        if (locales.containsKey(userId)) {
            locales.replace(userId, locale);
        }
        else {
            locales.put(userId, locale);
        }
    }
}
