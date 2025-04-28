function toggleSelection(
    selectedElement: HTMLElement,
    currentSelection: HTMLElement | null,
    className: string
): HTMLElement | null {
    if (currentSelection === selectedElement) {
        currentSelection.classList.remove(className);
        return null;
    } else {
        if (currentSelection) currentSelection.classList.remove(className);
        selectedElement.classList.add(className);
        return selectedElement;
    }
}

function animateElement(element: HTMLElement) {
    element.style.opacity = '0';
    element.style.transform = 'translateY(-20px)';

    setTimeout(() => {
        element.style.transition = 'opacity 0.5s ease-out, transform 0.5s ease-out';
        element.style.opacity = '1';
        element.style.transform = 'translateY(0)';
    }, 10);
}

function createListElement(
    parent: HTMLElement,
    text?: string,
    attributes: Record<string, string> = {},
    addRemove: boolean = true,
    small: boolean = false,
    toolTipText: string = null,
    removeCallback: (element: HTMLElement) => void = defaultRemoveCallback,
    animateTheElements: boolean = false
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

    if (animateTheElements)
        animateElement(item);

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
    if (parent) parent.appendChild(element);
    return element;
}

function getCurrentCurrencySymbol(): string {
    if (Object.keys(bankAccounts).length === 0 )
        return " €";

    if (bankAccountId === 0 || bankAccountId === undefined)
        return " " + Object.values(bankAccounts)[0].currencySymbol;

    return " " + bankAccounts[bankAccountId].currencySymbol;
}

function formatDateString(date: string): string {
    const [year, month, day] = date.split('-');
    const dateObj = new Date(parseInt(year), parseInt(month) - 1, parseInt(day));
    return `${dateObj.getDate()} ${monthAbbreviations[dateObj.getMonth()]} ${dateObj.getFullYear()}`;
}

function formatNumber(number: number, currency: string): string {
    return `${number.toFixed(2).replace('.', ',')} ${currency}`;
}

function createInputBox(
    parent: HTMLElement,
    icon: string,
    idText: string,
    type: string,
    text: string | null = null,
    placeHolder: string | null = null
): HTMLInputElement {
    const inputBox = createAndAppendElement(parent, "div", "inputBox");
    createAndAppendElement(inputBox, "span", icon);
    createAndAppendElement(inputBox, "label", "", "", {for: idText});

    // Create input element separately to set its value
    const inputElement = createAndAppendElement(inputBox, "input", "", "", {
        id: idText,
        name: idText,
        type: type,
    }) as HTMLInputElement;

    if (text !== null) {
        inputElement.value = text;
    }
    if (placeHolder) {
        inputElement.placeholder = placeHolder;
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

async function backToOtherView(cameFromUrl: string | null): Promise<void> {
    if (cameFromUrl) {
        await loadURL(cameFromUrl);
    }
}

function removeElements(sourceContainer: HTMLElement, soloItem: HTMLElement = null): void {
    let items = soloItem ? [soloItem] : Array.from(sourceContainer.querySelectorAll<HTMLElement>('.listItem'));

    items.forEach((item) => {
        // Animate the item before removing it
        animateElement(item);

        // Wait for the transition to complete
        item.addEventListener('transitionend', () => {
            item.remove(); // Remove the item from the DOM
        }, {once: true});
    });
}

function moveElements(sourceContainer: HTMLElement, targetContainer: HTMLElement, soloItem: HTMLElement = null): void {
    let items = [];
    if (sourceContainer) {
        items = Array.from(sourceContainer.querySelectorAll<HTMLElement>(`.listItem`));
    }

    if (soloItem) {
        items.push(soloItem);
    }

    items.forEach((item) => {
        item.addEventListener('transitionend', () => {
            animateElement(item);
        }, {once: true});

        targetContainer.appendChild(item);
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

function createCheckBoxForTable(mainRow: HTMLElement, subRow: HTMLElement, id: number, isHidden: boolean) {
    const trCheckBox = createAndAppendElement(mainRow, "td");

    if (isHidden) {
        createAndAppendElement(trCheckBox, "span", "bi bi-eye-slash");
    }

    const checkBox = createAndAppendElement(trCheckBox, "input", "tableCheckbox", "", {
        type: "checkbox",
        id: id.toString()
    }) as HTMLInputElement;

    checkBox.addEventListener("change", () => updateRowStyle(mainRow, checkBox));

    mainRow.addEventListener("click", (event) => {
        if ((event.target as HTMLInputElement).type === "checkbox") return;
        checkBox.checked = !checkBox.checked;
        updateRowStyle(mainRow, checkBox);
        if (subRow)
            updateRowStyle(subRow, checkBox);
    });

    if (subRow) {
        subRow.addEventListener("click", (event) => {
            if ((event.target as HTMLInputElement).type === "checkbox") return;
            checkBox.checked = !checkBox.checked;
            updateRowStyle(subRow, checkBox);
            updateRowStyle(mainRow, checkBox);
        });
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

function createListSection(
    parent: HTMLElement,
    title: string,
    type: Type,
    data: Transaction[] | CounterPartyDisplay[] | ContractDisplay[],
    withSelect: boolean = false,
    left: boolean = true
): HTMLElement {
    const marginClass = left ? "marginLeftBig" : "marginRightBig"

    const container = createAndAppendElement(parent, "div", "verticalContainer widthHalf " + marginClass);
    const header = createAndAppendElement(container, "div", "verticalContainer");
    createAndAppendElement(header, "h2", "", title);

    if (type === Type.TRANSACTION) {
        createListContainer(header, transactionToListElementObjectArray(data as Transaction[]), withSelect);
    } else if (type === Type.COUNTERPARTY) {
        createListContainer(header, counterPartyToListElementObjectArray(data as CounterPartyDisplay[]), withSelect);
    } else if (type === Type.CONTRACT) {
        createListContainer(header, contractToListElementObjectArray(data as ContractDisplay[]), withSelect);
    }

    return container;
}

function createListContainer(
    parent: HTMLElement,
    listElementObjects: ListElementObject[],
    withSelect: boolean
): HTMLElement {
    const listContainer = createAndAppendElement(parent, "div", "verticalContainer height40vh");

    let selectedElement: HTMLElement | null = null;

    listElementObjects.forEach(listElementObject => {
        const listElement = createListElement(listContainer, listElementObject.text, {id: listElementObject.id.toString()},
            true, false, listElementObject.toolTip);

        if (withSelect) {
            listElement.addEventListener("click", () => {
                if (selectedElement) {
                    selectedElement.classList.remove("selected");
                }
                if (selectedElement !== listElement) {
                    listElement.classList.add("selected");
                    selectedElement = listElement;
                } else {
                    selectedElement = null;
                }
            });
        }
    });

    return listContainer;
}

function chooseHeader(dialogContent: HTMLElement, messages: Record<string, string>,
                      updatedContainer: HTMLElement, moveToContainer: HTMLElement,): void {
    const selectedElementList = updatedContainer.querySelectorAll(".selected");

    if (selectedElementList.length === 0) {
        showAlert(AlertType.INFO, messages["error_SelectAHeader"], dialogContent)
        return;
    }

    const selectedElement = selectedElementList.item(0) as HTMLElement;
    selectedElement.classList.remove("selected");

    moveElements(moveToContainer, updatedContainer);
    moveElements(selectedElement, moveToContainer, selectedElement);
}

function setMonths(messages: Record<string, string>): void {
    monthAbbreviations = messages["monthAbbreviations"]
        .split("', '")
        .map((month: string) => month.replace(/'/g, ''));
    transactionsHiddenToggle = false;
}

function createContractList(messages: Record<string, string>, contracts: Contract[], setCounterParty: boolean = false): void {
    const contractsContainer = document.getElementById("contractsContainer");

    if (!contractsContainer) {
        console.error("Contracts container not found!");
        return;
    }

    const currency = getCurrentCurrencySymbol();

    contracts.forEach((contract: Contract) => {
        const listItem = createAndAppendElement(contractsContainer, "div", "listItem tooltip tooltipBottom");
        listItem.addEventListener("click", () => toggleContractSelection(listItem, setCounterParty));
        listItem.id = contract.id.toString();
        listItem.dataset.counterPartyId = contract.counterParty.id.toString();

        const startDate = formatDateString(contract.startDate);
        const lastPaymentDate = formatDateString(contract.lastPaymentDate);
        const amount = formatNumber(contract.amount, currency);

        listItem.dataset.startDate = startDate;
        listItem.dataset.lastPaymentDate = lastPaymentDate;
        createAndAppendElement(listItem, "div", "normalText", `${contract.name}, ${messages["amount"]}: ${amount}`);
        createAndAppendElement(listItem, "div", "tooltipText", `${messages["startDate"]}: ${startDate}   ${messages["lastPaymentDate"]}: ${lastPaymentDate}`);
    });
}

function toggleContractSelection(selectedElement: HTMLElement, setCounterParty: boolean) {
    selectedContract = toggleSelection(selectedElement, selectedContract, "selected");

    if (setCounterParty && selectedContract)
        selectedCounterparty = selectedContract.dataset.counterPartyId;
}

function updateContractAvailability(): void {
    const contractElements = document.querySelectorAll<HTMLElement>("#contractsContainer .listItem");

    contractElements.forEach(contract => {
        if (selectedCounterparty === null) {
            contract.classList.remove("disabled");
        } else if (contract.dataset.counterPartyId === selectedCounterparty) {
            contract.classList.remove("disabled");
        } else {
            contract.classList.add("disabled");
        }
    });
}

function defaultRemoveCallback(element: HTMLElement): void {
    element.remove(); // Simpler and safer
}

function setUpSearchStringFields(messages: Record<string, string>, addListener: boolean = true): void {
    searchStringFields.forEach(field => {
        const addButton = document.getElementById(field.addButtonId) as HTMLButtonElement;
        const inputField = document.getElementById(field.inputId) as HTMLInputElement;
        const stringList = document.getElementById(field.listId) as HTMLElement;

        if (addListener)
            addButton.addEventListener("click", () => {
                const inputValue = inputField.value.trim();

                if (inputValue) {
                    addStringToList(messages, stringList, inputValue);
                    inputField.value = "";
                } else {
                    showAlert("info", messages["error_enterWord"]);
                }
            });
    });
}