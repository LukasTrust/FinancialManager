document.addEventListener("DOMContentLoaded", () => {
    const signUpForm = document.querySelector('section');
    signUpForm.style.opacity = "0";

    setTimeout(() => {
        signUpForm.style.transition = "opacity 1s ease-in-out";
        signUpForm.style.opacity = "1";
    }, 500);

    const submitButton = document.getElementById("submitButton");
    submitButton.addEventListener("click", (event) => {
        event.preventDefault();
        submitSignup();
    });
});

async function submitSignup() {
    const firstName = document.getElementById("firstName").value.trim();
    const lastName = document.getElementById("lastName").value.trim();
    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;
    const confirmPassword = document.getElementById("confirmPassword").value;

    // Check if all fields are filled
    if (!firstName || !lastName || !email || !password || !confirmPassword) {
        showAlert('WARNING', 'All fields are required');
        return;
    }

    // Check if passwords match
    if (password !== confirmPassword) {
        showAlert('WARNING', 'Passwords do not match');
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

        if (responseBody.alertType === 'WARNING' || responseBody.alertType === 'Error') {
            document.getElementById("password").value = '';
            document.getElementById("confirmPassword").value = '';
        }

    } catch (error) {
        console.error("There was a problem with the signup request:", error);
        showAlert('ERROR', 'Signup failed. Please try again.');
    }
}