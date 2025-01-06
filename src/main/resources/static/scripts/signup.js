document.addEventListener("DOMContentLoaded",  async () => {
    // Load localization messages
    const userLocale = navigator.language || 'en';
    const messages = await fetchLocalization("login&signup", userLocale);

    const signUpForm = document.querySelector('section');
    signUpForm.style.opacity = "0";

    setTimeout(() => {
        signUpForm.style.transition = "opacity 1s ease-in-out";
        signUpForm.style.opacity = "1";
    }, 500);

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
        showAlert('warning', messages["allFieldsRequired"]);
        return;
    }

    // Check if passwords match
    if (password !== confirmPassword) {
        showAlert('warning', messages["passwordNotMatch"]);
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
            password.value = '';
            confirmPassword.value = '';
        }

    } catch (error) {
        console.error("There was a problem with the signup request:", error);
        showAlert('error', messages["generic"]);
    }
}