function toggleSelection(selectedElement, currentSelection, className) {
    if (currentSelection === selectedElement) {
        currentSelection.classList.remove(className);
        return null;
    }
    else {
        if (currentSelection)
            currentSelection.classList.remove(className);
        selectedElement.classList.add(className);
        return selectedElement;
    }
}
function animateElement(element) {
    element.style.opacity = '0';
    element.style.transform = 'translateY(-20px)';
    setTimeout(() => {
        element.style.transition = 'opacity 0.5s ease-out, transform 0.5s ease-out';
        element.style.opacity = '1';
        element.style.transform = 'translateY(0)';
    }, 10);
}
function createListElement(parent, text, attributes = {}, addRemove = true, small = false, toolTipText = null, removeCallback = defaultRemoveCallback, animateTheElements = false) {
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
function createAndAppendElement(parent, type, className = null, textContent = null, attributes = {}, eventListeners = {}) {
    const element = document.createElement(type);
    if (className)
        element.className = className;
    if (textContent)
        element.textContent = textContent;
    Object.entries(attributes).forEach(([key, value]) => element.setAttribute(key, value));
    Object.entries(eventListeners).forEach(([event, handler]) => element.addEventListener(event, handler));
    if (parent)
        parent.appendChild(element);
    return element;
}
function getCurrentCurrencySymbol() {
    if (bankAccountId === 0 || bankAccountId === undefined)
        return " " + Object.values(bankAccounts)[0].currencySymbol;
    return " " + bankAccounts[bankAccountId].currencySymbol;
}
function formatDateString(date) {
    const [year, month, day] = date.split('-');
    const dateObj = new Date(parseInt(year), parseInt(month) - 1, parseInt(day));
    return `${dateObj.getDate()} ${monthAbbreviations[dateObj.getMonth()]} ${dateObj.getFullYear()}`;
}
function formatNumber(number, currency) {
    return `${number.toFixed(2).replace('.', ',')} ${currency}`;
}
function createInputBox(parent, icon, idText, type, text = null, placeHolder = null) {
    const inputBox = createAndAppendElement(parent, "div", "inputBox");
    createAndAppendElement(inputBox, "span", icon);
    createAndAppendElement(inputBox, "label", "", "", { for: idText });
    // Create input element separately to set its value
    const inputElement = createAndAppendElement(inputBox, "input", "", "", {
        id: idText,
        name: idText,
        type: type,
    });
    if (text !== null) {
        inputElement.value = text;
    }
    if (placeHolder) {
        inputElement.placeholder = placeHolder;
    }
    return inputElement;
}
function debounceInputChange(inputElement, callback, id, messages, delay = 500) {
    inputElement.addEventListener("input", (event) => {
        clearTimeout(inputElement.dataset.timeoutId); // Clear previous timeout
        const timeoutId = setTimeout(() => {
            const newValue = event.target.value;
            callback(id, newValue, messages);
        }, delay);
        inputElement.dataset.timeoutId = timeoutId.toString(); // Store timeout ID
    });
}
async function backToOtherView(cameFromUrl) {
    if (cameFromUrl) {
        await loadURL(cameFromUrl);
    }
}
function removeElements(sourceContainer, soloItem = null) {
    let items = soloItem ? [soloItem] : Array.from(sourceContainer.querySelectorAll('.listItem'));
    items.forEach((item) => {
        // Animate the item before removing it
        animateElement(item);
        // Wait for the transition to complete
        item.addEventListener('transitionend', () => {
            item.remove(); // Remove the item from the DOM
        }, { once: true });
    });
}
function moveElements(sourceContainer, targetContainer, soloItem = null) {
    let items = [];
    if (sourceContainer) {
        items = Array.from(sourceContainer.querySelectorAll(`.listItem`));
    }
    if (soloItem) {
        items.push(soloItem);
    }
    items.forEach((item) => {
        item.addEventListener('transitionend', () => {
            animateElement(item);
        }, { once: true });
        targetContainer.appendChild(item);
    });
}
function addHoverToOtherElement(newRow, subRow) {
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
function addHoverToSiblings(element) {
    const parent = element.parentElement;
    if (!parent)
        return;
    const siblings = Array.from(parent.children);
    function addHover() {
        siblings.forEach(sibling => sibling.classList.add('hover'));
    }
    function removeHover() {
        // Check if the mouse is still within any of the siblings or the original element
        if ([...siblings].some(sib => sib.matches(':hover')))
            return;
        siblings.forEach(sibling => sibling.classList.remove('hover'));
    }
    siblings.forEach(sibling => {
        sibling.addEventListener('mouseenter', addHover);
        sibling.addEventListener('mouseleave', removeHover);
    });
}
function createCheckBoxForTable(mainRow, subRow, id, isHidden) {
    const trCheckBox = createAndAppendElement(mainRow, "td");
    if (isHidden) {
        createAndAppendElement(trCheckBox, "span", "bi bi-eye-slash");
    }
    const checkBox = createAndAppendElement(trCheckBox, "input", "tableCheckbox", "", {
        type: "checkbox",
        id: id.toString()
    });
    checkBox.addEventListener("change", () => updateRowStyle(mainRow, checkBox));
    mainRow.addEventListener("click", (event) => {
        if (event.target.type === "checkbox")
            return;
        checkBox.checked = !checkBox.checked;
        updateRowStyle(mainRow, checkBox);
        if (subRow)
            updateRowStyle(subRow, checkBox);
    });
    if (subRow) {
        subRow.addEventListener("click", (event) => {
            if (event.target.type === "checkbox")
                return;
            checkBox.checked = !checkBox.checked;
            updateRowStyle(subRow, checkBox);
            updateRowStyle(mainRow, checkBox);
        });
    }
}
function startTimer(source) {
    console.log(`Timer started from: ${source}`);
    timer = Date.now();
}
function stopTimer(source) {
    if (timer) {
        const elapsedTime = (Date.now() - timer) / 1000;
        console.log(`Timer stopped from: ${source}. Elapsed time: ${elapsedTime} seconds`);
        timer = null;
    }
    else {
        console.log(`No active timer to stop from: ${source}`);
    }
}
function debounce(func, delay) {
    let timeoutId;
    return function (...args) {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(() => func(...args), delay);
    };
}
function createListSection(parent, title, type, data, withSelect = false, left = true) {
    const marginClass = left ? "marginLeftBig" : "marginRightBig";
    const container = createAndAppendElement(parent, "div", "verticalContainer widthHalf " + marginClass);
    const header = createAndAppendElement(container, "div", "verticalContainer");
    createAndAppendElement(header, "h2", "", title);
    if (type === Type.TRANSACTION) {
        createListContainer(header, transactionToListElementObjectArray(data), withSelect);
    }
    else if (type === Type.COUNTERPARTY) {
        createListContainer(header, counterPartyToListElementObjectArray(data), withSelect);
    }
    else if (type === Type.CONTRACT) {
        createListContainer(header, contractToListElementObjectArray(data), withSelect);
    }
    return container;
}
function createListContainer(parent, listElementObjects, withSelect) {
    const listContainer = createAndAppendElement(parent, "div", "verticalContainer height40vh");
    let selectedElement = null;
    listElementObjects.forEach(listElementObject => {
        const listElement = createListElement(listContainer, listElementObject.text, { id: listElementObject.id.toString() }, true, false, listElementObject.toolTip);
        if (withSelect) {
            listElement.addEventListener("click", () => {
                if (selectedElement) {
                    selectedElement.classList.remove("selected");
                }
                if (selectedElement !== listElement) {
                    listElement.classList.add("selected");
                    selectedElement = listElement;
                }
                else {
                    selectedElement = null;
                }
            });
        }
    });
    return listContainer;
}
function chooseHeader(dialogContent, messages, updatedContainer, moveToContainer) {
    const selectedElementList = updatedContainer.querySelectorAll(".selected");
    if (selectedElementList.length === 0) {
        showAlert(AlertType.INFO, messages["error_SelectAHeader"], dialogContent);
        return;
    }
    const selectedElement = selectedElementList.item(0);
    selectedElement.classList.remove("selected");
    moveElements(moveToContainer, updatedContainer);
    moveElements(selectedElement, moveToContainer, selectedElement);
}
function setMonths(messages) {
    monthAbbreviations = messages["monthAbbreviations"]
        .split("', '")
        .map((month) => month.replace(/'/g, ''));
    transactionsHiddenToggle = false;
}
function createContractList(messages, contracts, setCounterParty = false) {
    const contractsContainer = document.getElementById("contractsContainer");
    if (!contractsContainer) {
        console.error("Contracts container not found!");
        return;
    }
    const currency = getCurrentCurrencySymbol();
    contracts.forEach((contract) => {
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
function toggleContractSelection(selectedElement, setCounterParty) {
    selectedContract = toggleSelection(selectedElement, selectedContract, "selected");
    if (setCounterParty && selectedContract)
        selectedCounterparty = selectedContract.dataset.counterPartyId;
}
function updateContractAvailability() {
    const contractElements = document.querySelectorAll("#contractsContainer .listItem");
    contractElements.forEach(contract => {
        if (selectedCounterparty === null) {
            contract.classList.remove("disabled");
        }
        else if (contract.dataset.counterPartyId === selectedCounterparty) {
            contract.classList.remove("disabled");
        }
        else {
            contract.classList.add("disabled");
        }
    });
}
function defaultRemoveCallback(element) {
    element.remove(); // Simpler and safer
}
function setUpSearchStringFields(messages, addListener = true) {
    searchStringFields.forEach(field => {
        const addButton = document.getElementById(field.addButtonId);
        const inputField = document.getElementById(field.inputId);
        const stringList = document.getElementById(field.listId);
        if (addListener)
            addButton.addEventListener("click", () => {
                const inputValue = inputField.value.trim();
                if (inputValue) {
                    addStringToList(messages, stringList, inputValue);
                    inputField.value = "";
                }
                else {
                    showAlert("info", messages["error_enterWord"]);
                }
            });
    });
}
//# sourceMappingURL=utils.js.map