function createModal(
    contentHTML: HTMLElement,
    closeButton?: HTMLElement | null,
    width: number = 0,
    height: number = 0,
    fitContent: boolean = false,
): HTMLDialogElement {
    // Create the dialog element
    const modal = document.createElement("dialog") as HTMLDialogElement;
    modal.appendChild(contentHTML);
    document.body.appendChild(modal);

    if (fitContent) {
        modal.classList.add("heightFitContent");
    }

    // Set modal size
    if (width !== 0) {
        modal.style.width = `${width}%`;
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
    window.setTimeout(() => {
        document.querySelector("dialog")?.close();
    }, 1000);
}

function createDialogHeader(parent: HTMLElement, text: string, icon: string): HTMLElement {
    const header = createAndAppendElement(parent, "h1", "horizontalContainer marginTopBig marginLeftBig marginBottomBig");
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
    height: number,
    fitContent: boolean = false,
): HTMLElement {
    const verticalContainer = createAndAppendElement(document.body, "div", "verticalContainer marginTop" +
        " marginBottomBig marginLeftBig marginRightBig height95");
    const header = createDialogHeader(verticalContainer, headerText, headerIcon);
    const closeButton = createAndAppendElement(header, "button", "exitButton") as HTMLButtonElement;
    createAndAppendElement(closeButton, "i", "bi bi-x-lg");

    createModal(verticalContainer, closeButton, width, height, fitContent);

    return verticalContainer;
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

    const buttonContainer = createAndAppendElement(content, "div", "horizontalContainer");

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
        createAndAppendElement(dialogContent, "h2", "marginBottom marginLeftBig alignSelfStart", messages["infoTransactionsWillAlsoBeAffected"])
    }

    const listContainer = createAndAppendElement(dialogContent, "div", "horizontalContainer heightInherit");

    const leftSide = createListSection(listContainer, messages["alreadyHiddenHeader"], type, alreadyHidden);
    const rightSide = createListSection(listContainer, messages["notHiddenHeader"], type, notHidden, false, false);

    createDialogButton(leftSide, "bi bi-eye", messages["unHide"], "left", async () => {
        await updateVisibility(messages, dialogContent, leftSide, rightSide.querySelector(".verticalContainer"), false, type);
    });

    createDialogButton(rightSide, "bi bi-eye-slash", messages["hide"], "right", async () => {
        await updateVisibility(messages, dialogContent, rightSide, leftSide.querySelector(".verticalContainer"), true, type);
    });
}

function showMergeDialog<T extends CounterPartyDisplay>(type: Type, messages: Record<string, string>): void {
    const checkedData = getCheckedData(type) as T[];

    const dialogContent = createDialogContent(messages["mergeHeader"], "bi bi-arrows-collapse-vertical", 0, 70);

    createAndAppendElement(dialogContent, "h2", "marginBottom marginLeftBig alignSelfStart", messages["mergeInfo"])

    const listContainer = createAndAppendElement(dialogContent, "div", "horizontalContainer heightInherit");

    const leftSide = createListSection(listContainer, messages["leftHeader"], type, []);
    const rightSide = createListSection(listContainer, messages["rightHeader"], type, checkedData, true, false);

    createDialogButton(leftSide, "bi bi-arrows-collapse-vertical", messages["mergeButton"], "left", async () => {
        await mergeData(dialogContent, messages, leftSide, rightSide, type);
    });

    createDialogButton(rightSide, "bi bi-bar-chart-steps", messages["chooseHeader"], "right", () => {
        chooseHeader(dialogContent, messages, rightSide.querySelector(".listContainer"), leftSide.querySelector(".listContainer"));
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