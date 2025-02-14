document.addEventListener("DOMContentLoaded",  async () => {
    if (!currentLanguage) {
        currentLanguage = navigator.language;
    }

    const messages = await fetchLocalization("login&signup");

    const submitButton = document.getElementById("submitButton");
    submitButton.addEventListener("click", (event) => {
        event.preventDefault();
        submitSignup(messages);
    });
});

async function submitSignup(messages) {
    const firstName = document.getElementById("firstName").value.trim();
    const lastName = document.getElementById("lastName").value.trim();
    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;
    const confirmPassword = document.getElementById("confirmPassword").value;

    // Check if all fields are filled
    if (!firstName || !lastName || !email || !password || !confirmPassword) {
        showAlert('warning', messages["error_allFieldsRequired"]);
        return;
    }

    // Check if passwords match
    if (password !== confirmPassword) {
        showAlert('warning', messages["error_passwordNotMatch"]);
        return;
    }

    // Prepare the data object
    const data = {
        firstName,
        lastName,
        email,
        password,
    };

    try {
        // Make the POST request to the backend
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
            document.getElementById("password").value = '';
            document.getElementById("confirmPassword").value = '';
        }

    } catch (error) {
        console.error("There was a problem with the signup request:", error);
        showAlert('error', messages["error_generic"]);
    }
}