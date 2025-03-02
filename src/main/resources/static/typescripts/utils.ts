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
    text: string | undefined,
    attributes: Record<string, string> = {},
    addRemove: boolean = true,
    small: boolean = false
): HTMLElement {
    const classType = small ? "listItemSmall" : "listItem";

    const item = createAndAppendElement(parent, "div", classType);
    createAndAppendElement(item, "div", "normalText", text, attributes);

    if (addRemove) {
        const removeButton = createAndAppendElement(item, "button", "removeButton bi bi-x-lg", null, {}, {
            click: () => parent.removeChild(item),
        });

        // Apply styles properly
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