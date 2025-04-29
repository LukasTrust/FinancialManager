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
    const resetDataMap = {
        [Type.TRANSACTION]: () => {
            filteredTransactionData = transactionData;
            splitDataIntoPages(messages, type, transactionData);
        },
        [Type.COUNTERPARTY]: () => {
            filteredCounterPartyData = counterPartyData;
            splitDataIntoPages(messages, type, counterPartyData);
        },
        [Type.CONTRACT]: () => {
            filteredContractData = contractData;
            splitDataIntoPages(messages, type, contractData);
        },
        [Type.CATEGORY]: () => {
            filteredCategoryData = categoryData;
            splitDataIntoPages(messages, type, categoryData);
        }
    };
    const filterDataMap = {
        [Type.TRANSACTION]: filterTransactions,
        [Type.COUNTERPARTY]: filterCounterParties,
        [Type.CONTRACT]: filterContracts,
        [Type.CATEGORY]: filterCategories
    };
    searchBarInput.addEventListener("input", debounce(() => {
        var _a, _b;
        const inputText = searchBarInput.value.trim().toLowerCase();
        if (inputText.length <= 2) {
            (_a = resetDataMap[type]) === null || _a === void 0 ? void 0 : _a.call(resetDataMap);
        }
        else {
            (_b = filterDataMap[type]) === null || _b === void 0 ? void 0 : _b.call(filterDataMap, messages, inputText);
        }
    }, 300));
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
    switch (type) {
        case Type.TRANSACTION:
            addRowsToTransactionTable(paginatedData, messages);
            break;
        case Type.COUNTERPARTY:
            addRowsToCounterPartyTable(paginatedData, messages);
            break;
        case Type.CONTRACT:
            addRowsToContractTable(paginatedData, messages);
            break;
        case Type.CATEGORY:
            addCategoriesTable(paginatedData, messages);
            break;
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
function setUpSorting(keepSubRowTogether = false) {
    const currentTableBody = getCurrentTableBody();
    if (!currentTableBody)
        return;
    const columnIcons = document.querySelectorAll(".iconColor");
    columnIcons.forEach(columnIcon => {
        columnIcon.addEventListener("click", () => {
            const columnIndex = Number(columnIcon.getAttribute("data-index"));
            const dataTypeString = columnIcon.getAttribute("data-type") || "string";
            let dataType;
            if (Object.values(DataTypeForSort).includes(dataTypeString)) {
                dataType = dataTypeString;
            }
            const isAscending = columnIcon.dataset.order === "asc";
            columnIcon.dataset.order = isAscending ? "desc" : "asc";
            sortTable(currentTableBody, columnIndex, dataType, isAscending, keepSubRowTogether);
        });
    });
}
function getCellValue(row, columnIndex, dataType) {
    var _a;
    const cell = row.children[columnIndex];
    if (!cell)
        return "";
    if (dataType === DataTypeForSort.input) {
        const inputElement = cell.querySelector("input");
        return inputElement ? inputElement.value.trim() : "";
    }
    return ((_a = cell.textContent) === null || _a === void 0 ? void 0 : _a.trim()) || "";
}
function sortTable(tableBody, columnIndex, dataType, isAscending, keepSubRowTogether) {
    const fragment = document.createDocumentFragment();
    if (keepSubRowTogether) {
        // Group rows by main row and sub-row pairs
        const rowGroups = Array.from(tableBody.querySelectorAll(".mainRow")).map(mainRow => {
            // Find the associated sub-row
            const pairId = mainRow.getAttribute("data-sort-key");
            const subRow = tableBody.querySelector(`tr[data-pair="${pairId}"]`);
            return { mainRow, subRow };
        });
        rowGroups.sort((groupA, groupB) => {
            const valueA = parseSortableValue(getCellValue(groupA.mainRow, columnIndex, dataType), dataType);
            const valueB = parseSortableValue(getCellValue(groupB.mainRow, columnIndex, dataType), dataType);
            return compareValues(valueA, valueB, isAscending);
        });
        // Append rows back to the fragment while keeping the main and sub-row together
        rowGroups.forEach(group => {
            fragment.appendChild(group.mainRow);
            if (group.subRow) {
                fragment.appendChild(group.subRow);
            }
        });
    }
    else {
        const rows = Array.from(tableBody.querySelectorAll("tr"));
        rows.sort((rowA, rowB) => {
            const valueA = parseSortableValue(getCellValue(rowA, columnIndex, dataType), dataType);
            const valueB = parseSortableValue(getCellValue(rowB, columnIndex, dataType), dataType);
            return compareValues(valueA, valueB, isAscending);
        });
        rows.forEach(row => fragment.appendChild(row));
    }
    tableBody.replaceChildren(fragment);
}
function parseSortableValue(value, dataType) {
    if (!value.trim())
        return value; // Handle empty values gracefully
    try {
        switch (dataType) {
            case DataTypeForSort.number:
                return formatNumberForSort(value);
            case DataTypeForSort.date:
                return formatDateForSort(value);
            default:
                return value.toLowerCase();
        }
    }
    catch (error) {
        console.warn(`Error parsing value: "${value}"`, error);
        return value;
    }
}
function formatDateForSort(value) {
    const parsedDate = Date.parse(value);
    if (!isNaN(parsedDate))
        return new Date(parsedDate);
    const dateParts = value.split(" ");
    const day = parseInt(dateParts[0], 10);
    const month = monthAbbreviations.indexOf(dateParts[1]);
    const year = parseInt(dateParts[2], 10);
    if (month === -1 || isNaN(day) || isNaN(year)) {
        throw new Error(`Invalid date format: "${value}"`);
    }
    return new Date(year, month, day);
}
function formatNumberForSort(value) {
    const cleanedValue = value.replace(/[^\d.,-]/g, '').replace(',', '.');
    const numericValue = parseFloat(cleanedValue);
    if (!isNaN(numericValue))
        return numericValue;
    throw new Error(`Invalid number format: "${value}"`);
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
function classifyHiddenOrNot(type) {
    const data = getCheckedData(type);
    const alreadyHidden = [];
    const notHidden = [];
    data.forEach(item => {
        let isHidden;
        switch (type) {
            case Type.TRANSACTION:
                isHidden = item.hidden;
                break;
            case Type.COUNTERPARTY:
                isHidden = item.counterParty.hidden;
                break;
            case Type.CONTRACT:
                isHidden = item.contract.hidden;
                break;
            default:
                isHidden = false;
        }
        isHidden ? alreadyHidden.push(item) : notHidden.push(item);
    });
    return { alreadyHidden, notHidden };
}
function getCheckedData(type) {
    const checkedRows = new Set(getCheckedRows());
    switch (type) {
        case Type.TRANSACTION:
            return filteredTransactionData.filter(t => checkedRows.has(t.id));
        case Type.COUNTERPARTY:
            return filteredCounterPartyData.filter(c => checkedRows.has(c.counterParty.id));
        case Type.CONTRACT:
            return filteredContractData.filter(c => checkedRows.has(c.contract.id));
        default:
            return [];
    }
}
function updateRowStyle(newRow, checkBox) {
    newRow.classList.toggle("selectedRow", checkBox.checked);
}
function updateRowGroupStyle(rowGroup, checkBox) {
    // Get the children of the rowGroup
    const children = rowGroup.children;
    // Loop through each child and add or remove the class
    for (let i = 0; i < children.length; i++) {
        if (checkBox.checked) {
            children[i].classList.add("selectedRow");
        }
        else {
            children[i].classList.remove("selectedRow");
        }
    }
}
function updateCachedDataAndUI(type, messages, ids) {
    const idSet = new Set(ids);
    switch (type) {
        case Type.TRANSACTION:
            filteredTransactionData = filteredTransactionData.map(transaction => idSet.has(transaction.id)
                ? Object.assign(Object.assign({}, transaction), { hidden: !transaction.hidden }) : transaction);
            transactionData = transactionData.map(transaction => idSet.has(transaction.id)
                ? Object.assign(Object.assign({}, transaction), { hidden: !transaction.hidden }) : transaction);
            splitDataIntoPages(messages, Type.TRANSACTION, filteredTransactionData);
            break;
        case Type.COUNTERPARTY:
            filteredCounterPartyData = filteredCounterPartyData.map(counterParty => idSet.has(counterParty.counterParty.id)
                ? Object.assign(Object.assign({}, counterParty), { counterParty: Object.assign(Object.assign({}, counterParty.counterParty), { hidden: !counterParty.counterParty.hidden }) }) : counterParty);
            counterPartyData = counterPartyData.map(counterParty => idSet.has(counterParty.counterParty.id)
                ? Object.assign(Object.assign({}, counterParty), { counterParty: Object.assign(Object.assign({}, counterParty.counterParty), { hidden: !counterParty.counterParty.hidden }) }) : counterParty);
            splitDataIntoPages(messages, Type.COUNTERPARTY, filteredCounterPartyData);
            break;
        case Type.CONTRACT:
            filteredContractData = filteredContractData.map(contract => idSet.has(contract.contract.id)
                ? Object.assign(Object.assign({}, contract), { contract: Object.assign(Object.assign({}, contract.contract), { hidden: !contract.contract.hidden }) }) : contract);
            contractData = contractData.map(contract => idSet.has(contract.contract.id)
                ? Object.assign(Object.assign({}, contract), { contract: Object.assign(Object.assign({}, contract.contract), { hidden: !contract.contract.hidden }) }) : contract);
            splitDataIntoPages(messages, Type.CONTRACT, filteredContractData);
            break;
    }
}
function changeRowVisibility(type) {
    const currentTableBody = getCurrentTableBody();
    if (!currentTableBody)
        return;
    const rows = Array.from(currentTableBody.querySelectorAll(".hiddenRow"));
    if (type === Type.TRANSACTION) {
        transactionsHiddenToggle = !transactionsHiddenToggle;
    }
    else if (type === Type.COUNTERPARTY) {
        counterPartiesHiddenToggle = !counterPartiesHiddenToggle;
    }
    else {
        contractsHiddenToggle = !contractsHiddenToggle;
    }
    rows.forEach(row => row.classList.toggle("hidden"));
}
function getIdsFromContainer(container) {
    return Array.from(container.querySelectorAll(".normalText, .listItem, .listItemSmall"))
        .map(span => Number(span.id))
        .filter(id => !isNaN(id) && id !== 0);
}
//# sourceMappingURL=tableFunctions.js.map