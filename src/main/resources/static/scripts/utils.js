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
function createListElement(parent, text, attributes = {}, addRemove = true, small = false) {
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
function createListSection(parent, title, transactions) {
    const container = createAndAppendElement(parent, "div", "flexContainerColumn", "", { style: "width: 45%" });
    const header = createAndAppendElement(container, "div", "listContainerHeader");
    createAndAppendElement(header, "h2", "", title, { style: "margin: 10px" });
    createListContainer(header, transactions);
    return container;
}
function createListContainer(parent, transactions) {
    const listContainer = createAndAppendElement(parent, "div", "listContainerColumn", "", {
        style: "min-height: 420px; max-height: 420px;",
    });
    transactions.forEach(transaction => {
        var _a;
        createListElement(listContainer, (_a = transaction.counterParty) === null || _a === void 0 ? void 0 : _a.name, { id: transaction.id.toString() });
    });
    return listContainer;
}
async function backToOtherView(cameFromUrl) {
    if (cameFromUrl) {
        await loadURL(cameFromUrl);
    }
}
function moveElements(sourceContainer, targetContainer) {
    const items = Array.from(sourceContainer.querySelectorAll(`.listItem`));
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
//# sourceMappingURL=utils.js.map