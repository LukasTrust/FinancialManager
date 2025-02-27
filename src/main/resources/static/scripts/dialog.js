function createModal(contentHTML, closeButton, width = "", height = "") {
    // Create the dialog element
    const modal = document.createElement("dialog");
    modal.appendChild(contentHTML);
    document.body.appendChild(modal);

    // Apply styling for width and height
    if (typeof width === "number") {
        modal.style.width = `${width}%`;
    }

    if (typeof height === "number") {
        modal.style.height = `${height}%`;
    }

    // Add event listener to close the modal
    if (closeButton) {
        closeButton.addEventListener("click", () => modal.close());
    } else {
        console.error("Close button not found in createModal!");
    }

    // Show the modal
    modal.showModal();

    return modal;
}

function closeDialog() {
    document.querySelector("dialog")?.close();
}

function createDialogHeader(parent, text, icon) {
    const header = createAndAppendElement(parent, "h1", "flexContainer");
    createAndAppendElement(header, "i", icon);
    createAndAppendElement(header, "span", "", text);
    return header;
}

function createDialogButton(parent, iconClass, text, alignment, callback) {
    const button = createAndAppendElement(parent, "button", "iconButton tooltip tooltipBottom", "", {
        style: `margin-top: 20px; ${alignment === "left" ? "margin-right: auto" : "margin-left: auto"}`
    });
    createAndAppendElement(button, "i", iconClass, "", { style: "margin-right: 10px" });
    createAndAppendElement(button, "div", "normalText", text);
    if (callback) button.addEventListener("click", callback);
    return button;
}

function createDialogContent(headerText, headerIcon, width = "", height = "") {
    const flexContainerColumn = createAndAppendElement("", "div", "flexContainerColumn");
    const header = createDialogHeader(flexContainerColumn, headerText, headerIcon);
    const closeButton = createAndAppendElement(header, "button", "closeButton");
    createAndAppendElement(closeButton, "i", "bi bi-x-lg");
    createModal(flexContainerColumn, closeButton, width, height);
    return flexContainerColumn;
}

function showMessageBox(headerText, headerIcon, mainText, leftButtonText, leftIcon,
                        rightButtonText, rightIcon, leftButtonCallback, rightButtonCallback, toolTipLeft, toolTipRight) {
    const content = createDialogContent(headerText, headerIcon, 30, 25);
    content.style.overflow = "visible";

    createAndAppendElement(content, "h2", "", mainText, {style: "margin-top: 30px; margin-bottom: 30px"});

    const buttonContainer = createAndAppendElement(content, "div", "flexContainerSpaced");

    const leftButton = createDialogButton(buttonContainer, leftIcon, leftButtonText, "left", leftButtonCallback);
    if (toolTipLeft) createAndAppendElement(leftButton, "span", "tooltipText", toolTipLeft);

    const rightButton = createDialogButton(buttonContainer, rightIcon, rightButtonText, "right", rightButtonCallback);
    if (toolTipRight) createAndAppendElement(rightButton, "span", "tooltipText", toolTipRight);
}