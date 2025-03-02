function showAlert(
    type: AlertType | string,
    message: string,
    parent: HTMLElement = document.body,
    duration: number = 5000
): HTMLElement {
    // Convert string input to AlertType
    const alertType = convertToAlertType(type);

    // Create alert container
    const classType = alertType.toLowerCase();
    const alert = createAndAppendElement(parent, "div", `alert ${classType} show`);

    // Create icon
    const icon = createAndAppendElement(alert, "i");
    icon.className = getAlertIcon(alertType);

    // Create message span
    createAndAppendElement(alert, "span", "message", message);

    // Create close button
    const closeButton = createAndAppendElement(alert, "span", "button-alert");
    createAndAppendElement(closeButton, "span", "bi bi-x-circle-fill", null, {}, {
        click: () => removeAlert(alert),
    });

    // Add alert to the stack
    alerts.push(alert);
    updateAlertPositions();

    // Auto-remove after duration
    setTimeout(() => removeAlert(alert), duration);

    return alert;
}

// Convert string to AlertType safely
function convertToAlertType(type: string | AlertType): AlertType {
    const normalizedType = type.toUpperCase();
    if (Object.values(AlertType).includes(normalizedType as AlertType)) {
        return normalizedType as AlertType;
    }
    return AlertType.INFO; // Default fallback
}

// Get correct Bootstrap icon for the alert type
function getAlertIcon(type: AlertType): string {
    switch (type) {
        case AlertType.SUCCESS:
            return "bi bi-check-circle-fill";
        case AlertType.WARNING:
            return "bi bi-exclamation-triangle-fill";
        case AlertType.ERROR:
            return "bi bi-exclamation-octagon-fill";
        case AlertType.INFO:
            return "bi bi-info-circle-fill";
        default:
            return "bi bi-bell-fill"; // Default icon
    }
}

function removeAlert(alert: HTMLElement): void {
    alert.classList.remove("show");
    alert.classList.add("hide");
    setTimeout(() => {
        alert.remove();
        alerts = alerts.filter(a => a !== alert);
        updateAlertPositions();
    }, 3000); // Allow animation to complete
}

function updateAlertPositions(): void {
    let cumulativeHeight = 10; // Starting position for the first alert

    alerts.forEach((alert) => {
        alert.style.top = `${cumulativeHeight}px`; // Set the top position
        cumulativeHeight += alert.offsetHeight + 10; // Add the alert's height and some margin for spacing
    });
}