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
function createListElement(parent, text, attributes = {}, addRemove = true, small = false, toolTipText, removeCallback = (element) => { var _a; return (_a = element.parentElement) === null || _a === void 0 ? void 0 : _a.removeChild(element); }) {
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
function createAndAppendElement(parent, type, className = null, textContent = null, attributes = {}, eventListeners = {}) {
    const element = document.createElement(type);
    if (className)
        element.className = className;
    if (textContent)
        element.textContent = textContent;
    Object.entries(attributes).forEach(([key, value]) => element.setAttribute(key, value));
    Object.entries(eventListeners).forEach(([event, handler]) => element.addEventListener(event, handler));
    parent.appendChild(element);
    return element;
}
function getCurrentCurrencySymbol() {
    if (bankAccountId === 0)
        return " " + Object.values(bankAccountSymbols)[0];
    return " " + bankAccountSymbols[bankAccountId];
}
function formatDateString(date) {
    const [year, month, day] = date.split('-');
    const dateObj = new Date(parseInt(year), parseInt(month) - 1, parseInt(day));
    return `${dateObj.getDate()} ${monthAbbreviations[dateObj.getMonth()]} ${dateObj.getFullYear()}`;
}
function formatNumber(number, currency) {
    return `${number.toFixed(2).replace('.', ',')} ${currency}`;
}
function createInputBox(parent, icon, idText, type, text = null) {
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
    items.forEach((item, index) => {
        setTimeout(() => {
            item.parentElement.removeChild(item);
            item.addEventListener('transitionend', () => {
                item.remove();
            }, { once: true });
        }, index * 150); // Stagger effect
    });
}
function moveElements(sourceContainer, targetContainer, soloItem = null) {
    let items = Array.from(sourceContainer.querySelectorAll(`.listItem`));
    if (soloItem) {
        items.push(soloItem);
    }
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
function createCheckBoxForRowGroup(rowGroup, newRow, id, isHidden) {
    const trCheckBox = createAndAppendElement(newRow, "td", null, "", { style: "width: 5%" });
    if (isHidden) {
        createAndAppendElement(trCheckBox, "span", "bi bi-eye-slash");
    }
    const checkBox = createAndAppendElement(trCheckBox, "input", "tableCheckbox", "", {
        type: "checkbox",
        id: id.toString(),
        style: "margin-left: 10px;",
    });
    checkBox.addEventListener("change", () => updateRowGroupStyle(rowGroup, checkBox));
    rowGroup.addEventListener("click", (event) => {
        if (event.target.type === "checkbox")
            return;
        checkBox.checked = !checkBox.checked;
        updateRowGroupStyle(rowGroup, checkBox);
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
function createCheckBoxForTable(newRow, id, isHidden) {
    const trCheckBox = createAndAppendElement(newRow, "td", null, "", { style: "width: 5%" });
    if (isHidden) {
        createAndAppendElement(trCheckBox, "span", "bi bi-eye-slash");
    }
    const checkBox = createAndAppendElement(trCheckBox, "input", "tableCheckbox", "", {
        type: "checkbox",
        id: id.toString(),
        style: "margin-left: 10px;",
    });
    checkBox.addEventListener("change", () => updateRowStyle(newRow, checkBox));
    newRow.addEventListener("click", (event) => {
        if (event.target.type === "checkbox")
            return;
        checkBox.checked = !checkBox.checked;
        updateRowStyle(newRow, checkBox);
    });
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
function createListSection(parent, title, type, data, withSelect = false) {
    const container = createAndAppendElement(parent, "div", "flexContainerColumn", "", { style: "width: 45%" });
    const header = createAndAppendElement(container, "div", "listContainerHeader");
    createAndAppendElement(header, "h2", "", title, { style: "margin: 10px" });
    if (type === Type.TRANSACTION) {
        createListContainer(header, transactionToListElementObjectArray(data), withSelect);
    }
    else if (type === Type.COUNTERPARTY) {
        createListContainer(header, counterPartyToListElementObjectArray(data), withSelect);
    }
    return container;
}
function createListContainer(parent, listElementObjects, withSelect) {
    const listContainer = createAndAppendElement(parent, "div", "listContainerColumn", "", {
        style: "min-height: 420px; max-height: 420px;",
    });
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
//# sourceMappingURL=utils.js.map