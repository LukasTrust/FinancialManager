function showAlert(type, message, duration = 5000) {
    // Create alert container
    let classType = type.toLowerCase();
    const alert = createElement("div");
    alert.className = `alert ${classType} show`;

    // Create icon
    const icon = createElement("i");
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
    const messageSpan = createElement("span", "message", message);

    // Create close button
    const closeButton = createElement("span", "button-alert");

    const closeIcon = createElement("span", "bi bi-x-circle-fill");
    closeButton.appendChild(closeIcon);

    // Close alert on button click
    closeButton.addEventListener("click", () => {
        removeAlert(alert);
    });

    // Append children to alert
    alert.appendChild(icon);
    alert.appendChild(messageSpan);
    alert.appendChild(closeButton);

    // Append alert to body
    document.body.appendChild(alert);

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