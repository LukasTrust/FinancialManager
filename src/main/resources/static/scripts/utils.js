function createListElement(parent, text, attributes = {}, addRemove = true, small = false) {
    // Create a new list item
    const classType = small ? "listItemSmall" : "listItem";
    const styleType = small ? {style: "margin-left: 20px"} : {};

    const item = createAndAppendElement(parent,"div", classType);

    createAndAppendElement(item,"div", "normalText", text, attributes);

    // Create a remove button
    if (addRemove) {
        createAndAppendElement(item,"button", "removeButton bi bi-x-lg",
            null, styleType, {click: () => parent.removeChild(item)});
    }

    return item;
}

function createAndAppendElement(parent, type, className = null, textContent = null, attributes = {}, eventListeners = {}) {
    const element = document.createElement(type);
    if (className) element.className = className;
    if (textContent) element.textContent = textContent;
    Object.entries(attributes).forEach(([key, value]) => element.setAttribute(key, value));
    Object.entries(eventListeners).forEach(([event, handler]) => element.addEventListener(event, handler));
    if (parent) parent.appendChild(element);
    return element;
}

function getCurrentCurrencySymbol() {
    return " " + bankAccountSymbols[bankAccountId];
}

function formatDateString(date) {
    // Split the input date string into year, month, and day
    const [year, month, day] = date.split('-');

    // Create a Date object (note: months are 0-indexed in JavaScript)
    const dateObj = new Date(year, month - 1, day);

    // Get the day, month abbreviation, and year
    const formattedDay = dateObj.getDate();
    const formattedMonth = monthAbbreviations[dateObj.getMonth()];
    const formattedYear = dateObj.getFullYear();

    // Return the formatted date string
    return `${formattedDay} ${formattedMonth} ${formattedYear}`;
}

function formatNumber(number, currency) {
    const formattedNumber = Number(number).toFixed(2).replace('.', ',');

    // Combine the formatted number with the currency symbol
    return `${formattedNumber} ${currency}`;
}

async function backToOtherView(cameFromUrl) {
    if (cameFromUrl != null){
        await loadURL(cameFromUrl);
    }
}

function createListSection(parent, title, transactions) {
    const container = createAndAppendElement(parent, "div", "flexContainerColumn", "", { style: "width: 45%" });
    const header = createAndAppendElement(container, "div", "listContainerHeader");
    createAndAppendElement(header, "h2", "", title, { style: "margin: 10px" });
    createListContainer(header, transactions);
    return container;
}

function createListContainer(parent, transactions) {
    const listContainer = createAndAppendElement(parent, "div", "listContainerColumn",
        "",{style: "min-height: 420px; max-height: 420px;"});
    transactions.forEach(transaction => {
        createListElement(listContainer, transaction.counterParty.name, { id: transaction.id });
    });
    return listContainer;
}