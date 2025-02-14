function showAlert(type, message, duration = 5000) {
    // Create alert container
    let classType = type.toLowerCase();
    const alert = createAndAppendElement(document.body,"div");
    alert.className = `alert ${classType} show`;

    // Create icon
    const icon = createAndAppendElement(alert,"i");
    switch (type) {
        case "SUCCESS":
            icon.className = "bi bi-check-circle-fill";
            break;
        case "WARNING":
            icon.className = "bi bi-exclamation-triangle-fill";
            break;
        case "ERROR":
            icon.className = "bi bi-exclamation-octagon-fill";
            break;
        case "INFO":
            icon.className = "bi bi-info-circle-fill";
            break;
        default:
            icon.className = "bi bi-bell-fill"; // Default icon
    }

    // Create message span
    createAndAppendElement(alert,"span", "message", message);

    // Create close button
    const closeButton = createAndAppendElement(alert,"span", "button-alert");

    createAndAppendElement(closeButton,"span", "bi bi-x-circle-fill");

    // Close alert on button click
    closeButton.addEventListener("click", () => {
        removeAlert(alert);
    });

    // Add alert to the stack
    alerts.push(alert);
    updateAlertPositions();

    // Auto-remove after duration
    setTimeout(() => {
        removeAlert(alert);
    }, duration);

    // Return the alert container
    return alert;
}

function removeAlert(alert) {
    alert.classList.remove("show");
    alert.classList.add("hide");
    setTimeout(() => {
        alert.remove();
        alerts = alerts.filter(a => a !== alert);
        updateAlertPositions();
    }, 3000); // Allow animation to complete
}

function updateAlertPositions() {
    let cumulativeHeight = 10; // Starting position for the first alert

    alerts.forEach((alert) => {
        alert.style.top = `${cumulativeHeight}px`; // Set the top position
        cumulativeHeight += alert.offsetHeight + 10; // Add the alert"s height and some margin for spacing
    });
}