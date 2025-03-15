async function setLocale() {
    currentLanguage = navigator.language;
    try {
        await fetch(`/localization/update/locale/${currentLanguage}`, {
            method: 'POST',
        });
    }
    catch (error) {
        console.log(error);
    }
}
async function loadLocalization(subDirectory) {
    const filePath = `/localization/clientSide/${subDirectory}/messages/${currentLanguage}`;
    try {
        const responseBody = await fetch(filePath);
        if (responseBody.ok) {
            const messages = await responseBody.json();
            // Apply translations to the page
            applyTranslations(messages);
            return messages;
        }
        else {
            showAlert("Error", "Error loading localization");
            console.warn(`Localization file not found in ${subDirectory}. Falling back to default.`);
        }
    }
    catch (error) {
        console.error("Error fetching localization file:", error);
    }
    showAlert("Error", "Error loading localization");
    console.warn(`Localization file not found in ${subDirectory}. Falling back to default.`);
    return null; // Return null if fetching fails
}
function applyTranslations(messages) {
    if (!messages) {
        console.warn("No messages provided for translation.");
        return;
    }
    // Loop through all elements with a data-i18n attribute
    const elements = document.querySelectorAll('[data-i18n], [data-i18n-placeholder]');
    elements.forEach(element => {
        // Apply text content translation
        const i18nKey = element.getAttribute('data-i18n');
        if (i18nKey && messages[i18nKey]) {
            element.textContent = messages[i18nKey];
        }
        // Apply placeholder translation
        const i18nPlaceholderKey = element.getAttribute('data-i18n-placeholder');
        if (i18nPlaceholderKey && messages[i18nPlaceholderKey]) {
            element.setAttribute('placeholder', messages[i18nPlaceholderKey]);
        }
    });
}
//# sourceMappingURL=localization.js.map