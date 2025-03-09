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
    }
    else {
        console.error("Close button not found in createModal!");
    }
    // Show the modal
    modal.showModal();
    return modal;
}
function closeDialog() {
    var _a;
    (_a = document.querySelector("dialog")) === null || _a === void 0 ? void 0 : _a.close();
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
    if (callback) {
        button.addEventListener("click", callback);
    }
    return button;
}
function createDialogContent(headerText, headerIcon, width = "", height = "") {
    const flexContainerColumn = createAndAppendElement(document.body, "div", "flexContainerColumn");
    const header = createDialogHeader(flexContainerColumn, headerText, headerIcon);
    const closeButton = createAndAppendElement(header, "button", "closeButton");
    createAndAppendElement(closeButton, "i", "bi bi-x-lg");
    createModal(flexContainerColumn, closeButton, width, height);
    return flexContainerColumn;
}
function showMessageBox(headerText, headerIcon, mainText, leftButtonText, leftIcon, rightButtonText, rightIcon, leftButtonCallback, rightButtonCallback, toolTipLeft, toolTipRight) {
    const content = createDialogContent(headerText, headerIcon, 30, 25);
    content.style.overflow = "visible";
    createAndAppendElement(content, "h2", "", mainText, { style: "margin-top: 30px; margin-bottom: 30px" });
    const buttonContainer = createAndAppendElement(content, "div", "flexContainerSpaced");
    const leftButton = createDialogButton(buttonContainer, leftIcon, leftButtonText, "left", leftButtonCallback);
    if (toolTipLeft) {
        createAndAppendElement(leftButton, "span", "tooltipText", toolTipLeft);
    }
    const rightButton = createDialogButton(buttonContainer, rightIcon, rightButtonText, "right", rightButtonCallback);
    if (toolTipRight) {
        createAndAppendElement(rightButton, "span", "tooltipText", toolTipRight);
    }
}
function showChangeHiddenDialog(type, messages) {
    const { alreadyHidden, notHidden } = classifyHiddenOrNot(type);
    const height = type === Type.COUNTERPARTY ? 70 : "";
    const dialogContent = createDialogContent(messages["changeHiddenHeader"], "bi bi-eye", "", height);
    if (type === Type.COUNTERPARTY) {
        createAndAppendElement(dialogContent, "h2", "", messages["infoTransactionsWillAlsoBeAffected"], { style: "margin-right: auto; margin-left: 30px; margin-top: 10px; margin-top: 10px;" });
    }
    const listContainer = createAndAppendElement(dialogContent, "div", "flexContainerSpaced");
    const leftSide = createListSection(listContainer, messages["alreadyHiddenHeader"], type, alreadyHidden);
    const rightSide = createListSection(listContainer, messages["notHiddenHeader"], type, notHidden);
    createDialogButton(leftSide, "bi bi-eye", messages["unHide"], "left", async () => {
        if (type === Type.TRANSACTION)
            await updateTransactionVisibility(messages, dialogContent, leftSide, rightSide.querySelector(".listContainerColumn"), false);
        else
            await updateCounterPartyVisibility(messages, dialogContent, leftSide, rightSide.querySelector(".listContainerColumn"), false);
    });
    createDialogButton(rightSide, "bi bi-eye-slash", messages["hide"], "right", async () => {
        if (type === Type.TRANSACTION)
            await updateTransactionVisibility(messages, dialogContent, rightSide, leftSide.querySelector(".listContainerColumn"), true);
        else
            await updateCounterPartyVisibility(messages, dialogContent, rightSide, leftSide.querySelector(".listContainerColumn"), true);
    });
}
function showMergeDialog(type, messages) {
    const checkedData = getCheckedData(type);
    const dialogContent = createDialogContent(messages["mergeHeader"], "bi bi-arrows-collapse-vertical", "", 70);
    const info = type === Type.COUNTERPARTY ? messages["mergeCounterPartiesInfo"] : messages[""];
    createAndAppendElement(dialogContent, "h2", "", info, { style: "margin-right: auto; margin-left: 30px; margin-top: 10px; margin-top: 10px;" });
    const listContainer = createAndAppendElement(dialogContent, "div", "flexContainerSpaced");
    const leftSide = createListSection(listContainer, messages["counterPartyHeader"], type, []);
    const rightSide = createListSection(listContainer, messages["counterPartiesToMerge"], type, checkedData, true);
    createDialogButton(leftSide, "bi bi-arrows-collapse-vertical", messages["mergeCounterParties"], "left", async () => {
        if (type === Type.COUNTERPARTY)
            await mergeCounterParties(dialogContent, messages, leftSide, rightSide);
    });
    createDialogButton(rightSide, "bi bi-bar-chart-steps", messages["chooseHeader"], "right", () => {
        if (type === Type.COUNTERPARTY)
            chooseHeader(dialogContent, messages, rightSide.querySelector(".listContainerColumn"), leftSide.querySelector(".listContainerColumn"));
    });
}
//# sourceMappingURL=dialog.js.map