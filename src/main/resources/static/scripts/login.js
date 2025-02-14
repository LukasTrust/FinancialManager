document.addEventListener("DOMContentLoaded", async () => {
    if (!currentLanguage) {
        currentLanguage = navigator.language;
    }

    // Load localization messages
    const messages = await fetchLocalization("login&signup");

    // Check if the error parameter is present in the URL
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('error')) {
        // Call showAlert with type "ERROR" and a localized message
        showAlert('error', messages["error_invalidCredentials"]);
    }

    const submitButton = document.getElementById("submitButton");
    submitButton.addEventListener("click", (event) => {
        event.preventDefault();
        submitSignIn(messages);
    });
});

async function submitSignIn(messages) {
    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;

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