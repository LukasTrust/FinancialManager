function createModal(
    contentHTML: HTMLElement,
    closeButton?: HTMLElement | null,
    width: number = 0,
    height: number = 0
): HTMLDialogElement {
    // Create the dialog element
    const modal = document.createElement("dialog") as HTMLDialogElement;
    modal.appendChild(contentHTML);
    document.body.appendChild(modal);

    // Set modal size
    if (width !== 0) {
        modal.style.width =`${width}%`;
    }
    if (height !== 0) {
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

function closeDialog(): void {
    document.querySelector("dialog")?.close();
}

function createDialogHeader(parent: HTMLElement, text: string, icon: string): HTMLElement {
    const header = createAndAppendElement(parent, "h1", "flexContainer");
    createAndAppendElement(header, "i", icon);
    createAndAppendElement(header, "span", "", text);
    return header;
}

function createDialogButton(
    parent: HTMLElement,
    iconClass: string,
    text: string,
    alignment: "left" | "right",
    callback?: () => void
): HTMLButtonElement {
    const button = createAndAppendElement(parent, "button", "iconButton tooltip tooltipBottom", "", {
        style: `margin-top: 20px; ${alignment === "left" ? "margin-right: auto" : "margin-left: auto"}`
    }) as HTMLButtonElement;

    createAndAppendElement(button, "i", iconClass, "", {style: "margin-right: 10px"});
    createAndAppendElement(button, "div", "normalText", text);

    if (callback) {
        button.addEventListener("click", callback);
    }

    return button;
}

function createDialogContent(
    headerText: string,
    headerIcon: string,
    width: number,
    height: number
): HTMLElement {
    const flexContainerColumn = createAndAppendElement(document.body, "div", "flexContainerColumn");
    const header = createDialogHeader(flexContainerColumn, headerText, headerIcon);
    const closeButton = createAndAppendElement(header, "button", "closeButton") as HTMLButtonElement;
    createAndAppendElement(closeButton, "i", "bi bi-x-lg");

    createModal(flexContainerColumn, closeButton, width, height);

    return flexContainerColumn;
}

function showMessageBox(
    headerText: string,
    headerIcon: string,
    mainText: string,
    leftButtonText: string,
    leftIcon: string,
    rightButtonText: string,
    rightIcon: string,
    leftButtonCallback?: () => void,
    rightButtonCallback?: () => void,
    toolTipLeft?: string,
    toolTipRight?: string
): void {
    const content = createDialogContent(headerText, headerIcon, 30, 20);
    content.style.overflow = "visible";

    createAndAppendElement(content, "h2", "", mainText, {style: "margin-top: 30px; margin-bottom: 30px"});

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

function showChangeHiddenDialog(type: Type, messages: Record<string, string>): void {
    const {alreadyHidden, notHidden} = classifyHiddenOrNot<Transaction>(type);

    const height = type !== Type.TRANSACTION ? 70 : 0;

    const dialogContent = createDialogContent(messages["changeHiddenHeader"], "bi bi-eye", 0, height);

    if (type !== Type.TRANSACTION) {
        createAndAppendElement(dialogContent, "h2", "", messages["infoTransactionsWillAlsoBeAffected"],
            {style: "margin-right: auto; margin-left: 30px; margin-top: 10px; margin-top: 10px;"})
    }

    const listContainer = createAndAppendElement(dialogContent, "div", "flexContainerSpaced");

    const leftSide = createListSection(listContainer, messages["alreadyHiddenHeader"], type, alreadyHidden);
    const rightSide = createListSection(listContainer, messages["notHiddenHeader"], type, notHidden);

    createDialogButton(leftSide, "bi bi-eye", messages["unHide"], "left", async () => {
        await updateVisibility(messages, dialogContent, leftSide, rightSide.querySelector(".listContainerColumn"), false, type);
    });

    createDialogButton(rightSide, "bi bi-eye-slash", messages["hide"], "right", async () => {
        await updateVisibility(messages, dialogContent, rightSide, leftSide.querySelector(".listContainerColumn"), true, type);
    });
}

function showMergeDialog<T extends CounterPartyDisplay>(type: Type, messages: Record<string, string>): void {
    const checkedData = getCheckedData(type) as T[];

    const dialogContent = createDialogContent(messages["mergeHeader"], "bi bi-arrows-collapse-vertical", 0, 70);

    const info = messages["mergeInfo"];

    createAndAppendElement(dialogContent, "h2", "", info,
        {style: "margin-right: auto; margin-left: 30px; margin-top: 10px; margin-top: 10px;"})

    const listContainer = createAndAppendElement(dialogContent, "div", "flexContainerSpaced");

    const leftSide = createListSection(listContainer, messages["leftHeader"], type, []);
    const rightSide = createListSection(listContainer, messages["rightHeader"], type, checkedData, true);

    createDialogButton(leftSide, "bi bi-arrows-collapse-vertical", messages["mergeButton"], "left", async () => {
        await mergeData(dialogContent, messages, leftSide, rightSide, type);
    });

    createDialogButton(rightSide, "bi bi-bar-chart-steps", messages["chooseHeader"], "right", () => {
        chooseHeader(dialogContent, messages, rightSide.querySelector(".listContainerColumn"), leftSide.querySelector(".listContainerColumn"));
    });
}

function showLoadingBar(messages: Record<string, string>, width: number = 30, height: number = 10) {
    window.setTimeout(() => {
    // Create modal content container
    const modalContent = document.createElement("div");
    modalContent.classList.add("loadingBarContent");

    // Create and add the h1 heading
    createAndAppendElement(modalContent, "h1", "", messages["loadingHeader"], {style: "margin-top: 10px; margin-bottom: 20px"});

    // Create loading bar container
    const loadingBarContainer = createAndAppendElement(modalContent, "div", "loadingBarContainer");

    // Create moving loading bar
    createAndAppendElement(loadingBarContainer, "div", "loadingBar");

    // Show modal with specified size
    createModal(modalContent, null, width, height);
    }, 500);
}