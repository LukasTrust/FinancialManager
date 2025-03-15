document.addEventListener("DOMContentLoaded", async () => {
    if (!currentLanguage) {
        currentLanguage = navigator.language;
    }
    // Load localization messages
    const messages = await loadLocalization("login&signup");
    if (!messages)
        return;
    // Check if the error parameter is present in the URL
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('error')) {
        showAlert('error', messages["error_invalidCredentials"]);
    }
    const submitButton = document.getElementById("submitButton");
    if (submitButton) {
        submitButton.addEventListener("click", (event) => {
            event.preventDefault();
            submitSignIn(messages);
        });
    }
});
async function submitSignIn(messages) {
    const emailInput = document.getElementById("email");
    const passwordInput = document.getElementById("password");
    const email = emailInput.value.trim();
    const password = passwordInput.value;
    // Check if all fields are filled
    if (!email || !password) {
        showAlert('WARNING', messages["error_allFieldsRequired"]);
        return;
    }
    // Check if email is in valid format
    const emailRegex = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/;
    if (!emailRegex.test(email)) {
        showAlert('ERROR', messages["error_invalidEmail"]);
        return;
    }
    // If validation passes, submit the form
    const form = document.querySelector('form');
    form.submit();
}
//# sourceMappingURL=login.js.map