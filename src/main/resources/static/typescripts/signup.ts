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

    const user = { firstName, lastName, email, password };

    try {
        const response = await fetch('/signup', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json',
            },
            body: JSON.stringify(user),
        });

        const responseBody: string = await response.text();

        let alertType: AlertType;

        if (responseBody.startsWith("success"))
            alertType = AlertType.SUCCESS;
        else if (responseBody.startsWith("warning"))
            alertType = AlertType.WARNING;
        else alertType = AlertType.ERROR;

        showAlert(alertType, messages[responseBody]);

        if (alertType === AlertType.WARNING || alertType === AlertType.ERROR) {
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
