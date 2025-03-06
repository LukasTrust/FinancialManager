function toggleSelection<T extends HTMLElement>(
    selectedElement: T,
    currentSelection: T | null,
    className: string
): T | null {
    if (currentSelection === selectedElement) {
        currentSelection.classList.remove(className);
        return null;
    } else {
        if (currentSelection) currentSelection.classList.remove(className);
        selectedElement.classList.add(className);
        return selectedElement;
    }
}

function createListElement(
    parent: HTMLElement,
    text?: string,
    attributes: Record<string, string> = {},
    addRemove: boolean = true,
    small: boolean = false,
    toolTipText?: string,
    removeCallback: (element: HTMLElement) => void = (element) => element.parentElement?.removeChild(element)
): HTMLElement {
    let classType = small ? "listItemSmall" : "listItem";
    if (toolTipText) {
        classType += " tooltip tooltipBottom";
    }

    const item = createAndAppendElement(parent, "div", classType);
    createAndAppendElement(item, "div", "normalText", text, attributes);

    if (toolTipText) {
        createAndAppendElement(item, "div", "tooltipText", toolTipText);
    }

    if (addRemove) {
        const removeButton = createAndAppendElement(item, "button", "removeButton bi bi-x-lg");

        removeButton.addEventListener("click", () => removeCallback(item));

        if (small) {
            removeButton.style.marginLeft = "20px";
        }
    }

    return item;
}

function createAndAppendElement(
    parent: HTMLElement | DocumentFragment,
    type: string,
    className: string | null = null,
    textContent: string | null = null,
    attributes: Record<string, string> = {},
    eventListeners: Record<string, EventListener> = {}
): HTMLElement {
    const element = document.createElement(type);
    if (className) element.className = className;
    if (textContent) element.textContent = textContent;
    Object.entries(attributes).forEach(([key, value]) => element.setAttribute(key, value));
    Object.entries(eventListeners).forEach(([event, handler]) => element.addEventListener(event, handler));
    parent.appendChild(element);
    return element;
}

function getCurrentCurrencySymbol(): string {
    return " " + bankAccountSymbols[bankAccountId];
}

function formatDateString(date: string): string {
    const [year, month, day] = date.split('-');
    const dateObj = new Date(parseInt(year), parseInt(month) - 1, parseInt(day));
    return `${dateObj.getDate()} ${monthAbbreviations[dateObj.getMonth()]} ${dateObj.getFullYear()}`;
}

function formatNumber(number: number, currency: string): string {
    return `${number.toFixed(2).replace('.', ',')} ${currency}`;
}

function createListSection(
    parent: HTMLElement,
    title: string,
    transactions: Transaction[]
): HTMLElement {
    const container = createAndAppendElement(parent, "div", "flexContainerColumn", "", { style: "width: 45%" });
    const header = createAndAppendElement(container, "div", "listContainerHeader");
    createAndAppendElement(header, "h2", "", title, { style: "margin: 10px" });
    createListContainer(header, transactions);
    return container;
}

function createInputBox(
    parent: HTMLElement,
    icon: string,
    idText: string,
    type: string,
    text: string | null = null,
): HTMLInputElement {
    const inputBox = createAndAppendElement(parent, "div", "inputBox");
    createAndAppendElement(inputBox, "span", icon);
    createAndAppendElement(inputBox, "label", "", "", { for: idText });

    // Create input element separately to set its value
    const inputElement = createAndAppendElement(inputBox, "input", "", "", {
        id: idText,
        name: idText,
        type: type,
    }) as HTMLInputElement;

    if (text !== null) {
        inputElement.value = text;
    }

    return inputElement;
}

function debounceInputChange(
    inputElement: HTMLInputElement,
    callback: (id: number, newValue: string, messages: Record<string, string>) => void,
    id: number,
    messages: Record<string, string>,
    delay = 500
) {
    inputElement.addEventListener("input", (event) => {
        clearTimeout(inputElement.dataset.timeoutId as unknown as number); // Clear previous timeout

        const timeoutId = setTimeout(() => {
            const newValue = (event.target as HTMLInputElement).value;
            callback(id, newValue, messages);
        }, delay);

        inputElement.dataset.timeoutId = timeoutId.toString(); // Store timeout ID
    });
}

function createListContainer(
    parent: HTMLElement,
    transactions: Transaction[]
): HTMLElement {
    const listContainer = createAndAppendElement(parent, "div", "listContainerColumn", "", {
        style: "min-height: 420px; max-height: 420px;",
    });
    transactions.forEach(transaction => {
        createListElement(listContainer, transaction.counterParty?.name, { id: transaction.id.toString() });
    });
    return listContainer;
}

async function backToOtherView(cameFromUrl: string | null): Promise<void> {
    if (cameFromUrl) {
        await loadURL(cameFromUrl);
    }
}

function moveElements(sourceContainer: HTMLElement, targetContainer: HTMLElement): void {
    const items = Array.from(sourceContainer.querySelectorAll<HTMLElement>(`.listItem`));

    items.forEach((item, index) => {
        // Add a delay for staggered animation
        setTimeout(() => {
            item.classList.add('moving');

            // Wait for the transition to complete
            item.addEventListener('transitionend', () => {
                targetContainer.appendChild(item);
                item.classList.remove('moving');
            }, { once: true });
        }, index * 150); // 150ms delay between items for a smoother stagger
    });
}

function createCheckBoxForRowGroup(rowGroup: HTMLElement, newRow: HTMLElement, id: number, isHidden: boolean) {
    const trCheckBox = createAndAppendElement(newRow, "td", null, "", { style: "width: 5%" });

    if (isHidden) {
        createAndAppendElement(trCheckBox, "span", "bi bi-eye-slash");
    }

    const checkBox = createAndAppendElement(trCheckBox, "input", "tableCheckbox", "", {
        type: "checkbox",
        id: id.toString(),
        style: "margin-left: 10px;",
    }) as HTMLInputElement;

    checkBox.addEventListener("change", () => updateRowGroupStyle(rowGroup, checkBox));

    rowGroup.addEventListener("click", (event) => {
        if ((event.target as HTMLInputElement).type === "checkbox") return;
        checkBox.checked = !checkBox.checked;
        updateRowGroupStyle(rowGroup, checkBox);
    });
}

function addHoverToOtherElement(newRow: HTMLElement, subRow: HTMLElement) {
    // Add event listeners to newRow
    newRow.addEventListener('mouseenter', () => {
        subRow.classList.add('hover'); // Add a class to subRow to mimic hover
    });

    newRow.addEventListener('mouseleave', () => {
        subRow.classList.remove('hover'); // Remove the class from subRow
    });

    // Add event listeners to subRow
    subRow.addEventListener('mouseenter', () => {
        newRow.classList.add('hover'); // Add a class to newRow to mimic hover
    });

    subRow.addEventListener('mouseleave', () => {
        newRow.classList.remove('hover'); // Remove the class from newRow
    });
}

function createCheckBoxForTable(newRow: HTMLElement, id: number, isHidden: boolean) {
    const trCheckBox = createAndAppendElement(newRow, "td", null, "", { style: "width: 5%" });

    if (isHidden) {
        createAndAppendElement(trCheckBox, "span", "bi bi-eye-slash");
    }

    const checkBox = createAndAppendElement(trCheckBox, "input", "tableCheckbox", "", {
        type: "checkbox",
        id: id.toString(),
        style: "margin-left: 10px;",
    }) as HTMLInputElement;

    checkBox.addEventListener("change", () => updateRowStyle(newRow, checkBox));

    newRow.addEventListener("click", (event) => {
        if ((event.target as HTMLInputElement).type === "checkbox") return;
        checkBox.checked = !checkBox.checked;
        updateRowStyle(newRow, checkBox);
    });
}

function startTimer(source: string): void {
    console.log(`Timer started from: ${source}`);
    timer = Date.now();
}

function stopTimer(source: string): void {
    if (timer) {
        const elapsedTime = (Date.now() - timer) / 1000;
        console.log(`Timer stopped from: ${source}. Elapsed time: ${elapsedTime} seconds`);
        timer = null;
    } else {
        console.log(`No active timer to stop from: ${source}`);
    }
}

function debounce<T extends (...args: any[]) => void>(
    func: T,
    delay: number
): (...args: Parameters<T>) => void {
    let timeoutId: ReturnType<typeof setTimeout>;

    return function (this: void, ...args: Parameters<T>) {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(() => func(...args), delay);
    };
}