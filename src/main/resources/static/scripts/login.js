document.addEventListener("DOMContentLoaded", () => {
    // Check if the error parameter is present in the URL
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('error')) {
        // Call showAlert with type "ERROR" and a custom message
        showAlert('ERROR', 'Invalid username or password. Please try again.', 5000);
    }

    const submitButton = document.getElementById("submitButton");
    submitButton.addEventListener("click", (event) => {
        event.preventDefault();
        submitSignin();
    });
});

async function submitSignin() {
    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;

    // Check if all fields are filled
    if (!email || !password) {
        showAlert('WARNING', 'All fields are required');
        return;
    }

    // Check if email is in valid format
    const emailRegex = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/;
    if (!emailRegex.test(email)) {
        showAlert('ERROR', 'Please enter a valid email address');
        return;
    }

    // If validation passes, submit the form
    const form = document.querySelector('form');
    form.submit();
}