document.addEventListener("DOMContentLoaded", async () => {
    // Load localization messages
    const messages = await fetchLocalization("login&signup");
    if (!messages) return;

    // Check if the error parameter is present in the URL
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('error')) {
        // Call showAlert with type "ERROR" and a localized message
        showAlert('error', messages["error_invalidCredentials"]);
    }

    const submitButton = document.getElementById("submitButton") as HTMLButtonElement;
    if (submitButton) {
        submitButton.addEventListener("click", (event: MouseEvent) => {
            event.preventDefault();
            submitSignIn(messages);
        });
    }
});

async function submitSignIn(messages: { [key: string]: string }) {
    const emailInput = document.getElementById("email") as HTMLInputElement;
    const passwordInput = document.getElementById("password") as HTMLInputElement;

    const email: string = emailInput.value.trim();
    const password: string = passwordInput.value;

    // Check if all fields are filled
    if (!email || !password) {
        showAlert('WARNING', messages["error_allFieldsRequired"]);
        return;
    }

    // Check if email is in valid format
    const emailRegex: RegExp = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/;
    if (!emailRegex.test(email)) {
        showAlert('ERROR', messages["error_invalidEmail"]);
        return;
    }

    // If validation passes, submit the form
    const form = document.querySelector('form') as HTMLFormElement;
    form.submit();
}