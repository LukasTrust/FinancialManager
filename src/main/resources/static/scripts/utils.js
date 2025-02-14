function createAndAppendElement(parent, type, className = null, textContent = null, attributes = {}, eventListeners = {}) {
    const element = document.createElement(type);
    if (className) element.className = className;
    if (textContent) element.textContent = textContent;
    Object.entries(attributes).forEach(([key, value]) => element.setAttribute(key, value));
    Object.entries(eventListeners).forEach(([event, handler]) => element.addEventListener(event, handler));
    parent.appendChild(element);
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

function setUpSorting() {
    const currentTableBody = document.getElementById("tableBody");
    const columnIcons = document.querySelectorAll(".iconColor");

    columnIcons.forEach(columnIcon => {
        columnIcon.addEventListener("click", () => {
            const columnIndex = columnIcon.getAttribute("data-index");
            const dataType = columnIcon.getAttribute("data-type");
            const isAscending = columnIcon.dataset.order === "asc";
            columnIcon.dataset.order = isAscending ? "desc" : "asc";

            sortTable(currentTableBody, columnIndex, dataType, isAscending);
        });
    });
}

function sortTable(tableBody, columnIndex, dataType, isAscending) {
    const rows = Array.from(tableBody.querySelectorAll("tr"));

    rows.sort((rowA, rowB) => {
        const cellA = rowA.children[columnIndex].innerText.trim();
        const cellB = rowB.children[columnIndex].innerText.trim();

        const valueA = parseSortableValue(cellA, dataType);
        const valueB = parseSortableValue(cellB, dataType);

        return compareValues(valueA, valueB, isAscending);
    });

    // Use DocumentFragment to minimize DOM reflows
    const fragment = document.createDocumentFragment();
    rows.forEach(row => fragment.appendChild(row));

    tableBody.innerHTML = "";
    tableBody.appendChild(fragment);
}

function parseSortableValue(value, dataType) {
    try {
        switch (dataType) {
            case "number":
                return formatNumberForSort(value);
            case "date":
                return formatDateForSort(value);
            case "string":
            default:
                return value;
        }
    } catch (error) {
        console.error(`Error parsing value: ${value}`, error);
        return value; // Fallback to original value if parsing fails
    }
}

function formatDateForSort(value) {
    const dateParts = value.split(" ");
    const day = parseInt(dateParts[0], 10);
    const month = monthAbbreviations.indexOf(dateParts[1]);
    const year = parseInt(dateParts[2], 10);

    if (month === -1 || isNaN(day) || isNaN(year)) {
        throw new Error("Invalid date format");
    }

    return new Date(year, month, day);
}

function formatNumberForSort(value) {
    value = value.trim();

    // Remove non-numeric characters except commas, dots, and minus signs
    const numericPart = value.replace(/[^0-9.,-]/g, '');
    const numericValue = parseFloat(numericPart.replace(',', '.'));

    if (!isNaN(numericValue)) {
        return numericValue;
    }

    throw new Error("Invalid number format");
}

function compareValues(valueA, valueB, isAscending) {
    if (typeof valueA === "number" && typeof valueB === "number") {
        return isAscending ? valueA - valueB : valueB - valueA;
    }

    if (valueA instanceof Date && valueB instanceof Date) {
        return isAscending ? valueA - valueB : valueB - valueA;
    }

    // Compare strings lexicographically
    return isAscending ? valueA.toString().localeCompare(valueB.toString()) : valueB.toString().localeCompare(valueA.toString());
}