document.addEventListener("DOMContentLoaded", async () => {
    if (!currentLanguage) {
        currentLanguage = navigator.language;
    }

    const messages = await fetchLocalization("login&signup");
    if (!messages) return;

    const submitButton = document.getElementById("submitButton") as HTMLButtonElement | null;
    if (submitButton) {
        submitButton.addEventListener("click", (event: Event) => {
            event.preventDefault();
            submitSignup(messages);
        });
    }
});

async function submitSignup(messages: Record<string, string>): Promise<void> {
    const firstName = (document.getElementById("firstName") as HTMLInputElement | null)?.value.trim();
    const lastName = (document.getElementById("lastName") as HTMLInputElement | null)?.value.trim();
    const email = (document.getElementById("email") as HTMLInputElement | null)?.value.trim();
    const password = (document.getElementById("password") as HTMLInputElement | null)?.value;
    const confirmPassword = (document.getElementById("confirmPassword") as HTMLInputElement | null)?.value;

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

        const responseBody: { alertType: string; message: string } = await response.json();
        showAlert(responseBody.alertType, responseBody.message);

        if (responseBody.alertType.toLowerCase() === 'warning' || responseBody.alertType.toLowerCase() === 'error') {
            const passwordField = document.getElementById("password") as HTMLInputElement | null;
            const confirmPasswordField = document.getElementById("confirmPassword") as HTMLInputElement | null;
            if (passwordField) passwordField.value = '';
            if (confirmPasswordField) confirmPasswordField.value = '';
        }
    } catch (error) {
        console.error("There was a problem with the signup request:", error);
        showAlert('error', messages["error_generic"]);
    }
}
