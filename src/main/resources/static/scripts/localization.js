async function fetchLocalization(subDirectory, locale) {
    const filePath = `/localization/${subDirectory}/messages_${locale}.json`; // Adjust the path if necessary
    try {
        const response = await fetch(filePath);
        if (response.ok) {
            const messages = await response.json();
            // Apply translations to the page
            applyTranslations(messages);

            return messages;
        } else {
            console.warn(`Localization file for ${locale} not found. Falling back to default.`);
        }
    } catch (error) {
        console.error("Error fetching localization file:", error);
    }
}

function applyTranslations(messages) {
    // Loop through all elements with a data-i18n attribute
    const elements = document.querySelectorAll('[data-i18n], [data-i18n-placeholder]');

    elements.forEach(element => {
        // Check if the element has a data-i18n attribute
        const i18nKey = element.getAttribute('data-i18n');
        if (i18nKey && messages[i18nKey]) {
            // Update the text content or placeholder with the corresponding translation
            element.textContent = messages[i18nKey];
        }

        // Check if the element has a data-i18n-placeholder attribute
        const i18nPlaceholderKey = element.getAttribute('data-i18n-placeholder');
        if (i18nPlaceholderKey && messages[i18nPlaceholderKey]) {
            // Update the placeholder attribute
            element.setAttribute('placeholder', messages[i18nPlaceholderKey]);
        }
    });
}