function createModal(contentHTML, closeButton) {
    // Create the dialog element
    const modal = document.createElement("dialog");
    modal.appendChild(contentHTML);
    document.body.appendChild(modal);

    // Add event listener to close the modal
    if (closeButton) {
        closeButton.addEventListener("click", () => modal.close());
    } else {
        console.error("Close button not found in createModal!");
    }

    // Show the modal
    modal.showModal();
}

function createListElement(parent, text, attributes = {}) {
    // Create a new list item
    const item = createAndAppendElement(parent,"div", "listItem");

    createAndAppendElement(item,"span", "item", text, attributes);

    // Create a remove button
    createAndAppendElement(item,"button", "removeButton bi bi-x-lg",
        null, {}, {click: () => parent.removeChild(item)});

    return item;
}

function createListContainer(parent, transactions) {
    const listContainer = createAndAppendElement(parent, "div", "listContainerColumn");
    transactions.forEach(transaction => {
        createListElement(listContainer, transaction.counterParty.name, { id: transaction.id });
    });
    return listContainer;
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