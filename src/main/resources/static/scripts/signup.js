document.addEventListener("DOMContentLoaded", async () => {
    if (!currentLanguage) {
        currentLanguage = navigator.language;
    }
    const messages = await fetchLocalization("login&signup");
    if (!messages)
        return;
    const submitButton = document.getElementById("submitButton");
    if (submitButton) {
        submitButton.addEventListener("click", (event) => {
            event.preventDefault();
            submitSignup(messages);
        });
    }
});
async function submitSignup(messages) {
    var _a, _b, _c, _d, _e;
    const firstName = (_a = document.getElementById("firstName")) === null || _a === void 0 ? void 0 : _a.value.trim();
    const lastName = (_b = document.getElementById("lastName")) === null || _b === void 0 ? void 0 : _b.value.trim();
    const email = (_c = document.getElementById("email")) === null || _c === void 0 ? void 0 : _c.value.trim();
    const password = (_d = document.getElementById("password")) === null || _d === void 0 ? void 0 : _d.value;
    const confirmPassword = (_e = document.getElementById("confirmPassword")) === null || _e === void 0 ? void 0 : _e.value;
    if (!firstName || !lastName || !email || !password || !confirmPassword) {
        showAlert('warning', messages["error_allFieldsRequired"]);
        return;
    }
    if (password !== confirmPassword) {
        showAlert('warning', messages["error_passwordNotMatch"]);
        return;
    }
    const data = { firstName, lastName, email, password };
    try {
        const response = await fetch('/signup', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json',
            },
            body: JSON.stringify(data),
        });
        const responseBody = await response.json();
        showAlert(responseBody.alertType, responseBody.message);
        if (responseBody.alertType.toLowerCase() === 'warning' || responseBody.alertType.toLowerCase() === 'error') {
            const passwordField = document.getElementById("password");
            const confirmPasswordField = document.getElementById("confirmPassword");
            if (passwordField)
                passwordField.value = '';
            if (confirmPasswordField)
                confirmPasswordField.value = '';
        }
    }
    catch (error) {
        console.error("There was a problem with the signup request:", error);
        showAlert('error', messages["error_generic"]);
    }
}
//# sourceMappingURL=signup.js.map