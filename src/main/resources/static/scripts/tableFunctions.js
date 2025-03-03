function getCurrentTableBody() {
    const currentTableBody = document.getElementById("tableBody");
    if (!currentTableBody) {
        console.error("Table body not found");
        return null;
    }
    return currentTableBody;
}
function getCheckedRows() {
    const currentTableBody = getCurrentTableBody();
    if (!currentTableBody)
        return [];
    return Array.from(currentTableBody.querySelectorAll("tr td input[type='checkbox']:checked"))
        .map(checkbox => { var _a; return Number((_a = checkbox.closest("tr")) === null || _a === void 0 ? void 0 : _a.id); });
}
function searchTable(messages, type) {
    const searchBarInput = document.getElementById("searchBarInput");
    if (!searchBarInput) {
        console.error("Search bar input element not found!");
        return;
    }
    searchBarInput.addEventListener("input", debounce(() => {
        const inputText = searchBarInput.value.trim().toLowerCase();
        if (inputText.length <= 2) {
            if (type === "transaction") {
                filteredTransactionData = transactionData;
                splitDataIntoPages(messages, type, transactionData);
            }
            return;
        }
        if (type === "transaction") {
            filterTransactions(messages, inputText);
        }
    }, 300));
}
function debounce(func, delay) {
    let timeoutId;
    return function (...args) {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(() => func(...args), delay);
    };
}
function splitDataIntoPages(messages, type, data) {
    const itemsPerPageSelection = document.getElementById("itemsPerPage");
    const nextButton = document.getElementById("nextButton");
    const previousButton = document.getElementById("previousButton");
    const currentTableBody = getCurrentTableBody();
    if (!itemsPerPageSelection || !nextButton || !previousButton || !currentTableBody) {
        console.error("Pagination elements not found!");
        return;
    }
    let currentPageIndex = 1;
    let itemsPerPage = parseInt(itemsPerPageSelection.value) || data.length;
    let numberOfPages = calculateNumberOfPages(data.length, itemsPerPage);
    updateUI(data, currentPageIndex, itemsPerPage, numberOfPages, messages, type, currentTableBody);
    itemsPerPageSelection.addEventListener("change", () => {
        itemsPerPage = parseInt(itemsPerPageSelection.value) || data.length;
        numberOfPages = calculateNumberOfPages(data.length, itemsPerPage);
        currentPageIndex = 1;
        updateUI(data, currentPageIndex, itemsPerPage, numberOfPages, messages, type, currentTableBody);
    });
    nextButton.addEventListener("click", () => {
        if (currentPageIndex < numberOfPages) {
            currentPageIndex++;
            updateUI(data, currentPageIndex, itemsPerPage, numberOfPages, messages, type, currentTableBody);
        }
    });
    previousButton.addEventListener("click", () => {
        if (currentPageIndex > 1) {
            currentPageIndex--;
            updateUI(data, currentPageIndex, itemsPerPage, numberOfPages, messages, type, currentTableBody);
        }
    });
}
function calculateNumberOfPages(totalItems, itemsPerPage) {
    return Math.ceil(totalItems / itemsPerPage);
}
function updateUI(data, currentPageIndex, itemsPerPage, numberOfPages, messages, type, currentTableBody) {
    const startIndex = (currentPageIndex - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const paginatedData = data.slice(startIndex, endIndex);
    clearTable(currentTableBody);
    if (type === "transaction") {
        addRowsToTransactionTable(paginatedData, messages);
    }
    const currentPage = document.getElementById("currentPage");
    const numberOfPagesElement = document.getElementById("numberOfPages");
    const lengthDifference = currentPageIndex - currentPageIndex;
    if (currentPage)
        currentPage.textContent = "&nbsp;".repeat(lengthDifference) + String(currentPageIndex);
    if (numberOfPagesElement)
        numberOfPagesElement.textContent = String(numberOfPages);
    const nextButton = document.getElementById("nextButton");
    const previousButton = document.getElementById("previousButton");
    if (nextButton) {
        nextButton.disabled = currentPageIndex >= numberOfPages;
        nextButton.classList.toggle("disabled", currentPageIndex >= numberOfPages);
    }
    if (previousButton) {
        previousButton.disabled = currentPageIndex <= 1;
        previousButton.classList.toggle("disabled", currentPageIndex <= 1);
    }
}
function clearTable(currentTableBody) {
    currentTableBody.innerHTML = "";
}
function setUpSorting() {
    const currentTableBody = getCurrentTableBody();
    if (!currentTableBody)
        return;
    const columnIcons = document.querySelectorAll(".iconColor");
    columnIcons.forEach(columnIcon => {
        columnIcon.addEventListener("click", () => {
            const columnIndex = Number(columnIcon.getAttribute("data-index"));
            const dataType = columnIcon.getAttribute("data-type") || "string";
            const isAscending = columnIcon.dataset.order === "asc";
            columnIcon.dataset.order = isAscending ? "desc" : "asc";
            sortTable(currentTableBody, columnIndex, dataType, isAscending);
        });
    });
}
function sortTable(tableBody, columnIndex, dataType, isAscending) {
    const rows = Array.from(tableBody.querySelectorAll("tr"));
    rows.sort((rowA, rowB) => {
        var _a, _b, _c, _d;
        const cellA = ((_b = (_a = rowA.children[columnIndex]) === null || _a === void 0 ? void 0 : _a.textContent) === null || _b === void 0 ? void 0 : _b.trim()) || "";
        const cellB = ((_d = (_c = rowB.children[columnIndex]) === null || _c === void 0 ? void 0 : _c.textContent) === null || _d === void 0 ? void 0 : _d.trim()) || "";
        const valueA = parseSortableValue(cellA, dataType);
        const valueB = parseSortableValue(cellB, dataType);
        return compareValues(valueA, valueB, isAscending);
    });
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
    }
    catch (error) {
        console.error(`Error parsing value: ${value}`, error);
        return value;
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
    const numericValue = parseFloat(value.replace(/[^0-9.,-]/g, '').replace(',', '.'));
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
        return isAscending ? valueA.getTime() - valueB.getTime() : valueB.getTime() - valueA.getTime();
    }
    return isAscending ? valueA.toString().localeCompare(valueB.toString()) : valueB.toString().localeCompare(valueA.toString());
}
//# sourceMappingURL=tableFunctions.js.map