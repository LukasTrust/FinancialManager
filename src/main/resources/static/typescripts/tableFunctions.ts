function getCurrentTableBody(): HTMLElement | null {
    const currentTableBody = document.getElementById("tableBody");

    if (!currentTableBody) {
        console.error("Table body not found");
        return null;
    }

    return currentTableBody;
}

function getCheckedRows(): number[] {
    const currentTableBody = getCurrentTableBody();
    if (!currentTableBody) return [];

    return Array.from(currentTableBody.querySelectorAll<HTMLInputElement>("tr td input[type='checkbox']:checked"))
        .map(checkbox => Number(checkbox.closest("tr")?.id));
}

function searchTable(messages: Record<string, string>, type: Type): void {
    const searchBarInput = document.getElementById("searchBarInput") as HTMLInputElement | null;

    if (!searchBarInput) {
        console.error("Search bar input element not found!");
        return;
    }

    searchBarInput.addEventListener("input", debounce(() => {
        const inputText = searchBarInput.value.trim().toLowerCase();

        if (inputText.length <= 2) {
            if (type === Type.TRANSACTION) {
                filteredTransactionData = transactionData;
                splitDataIntoPages(messages, type, transactionData);
            } else if (type === Type.COUNTERPARTY) {
                filteredCounterPartyData = counterPartyData;
                splitDataIntoPages(messages, type, counterPartyData);
            }
            return;
        }

        if (type === Type.TRANSACTION) {
            filterTransactions(messages, inputText);
        } else if (type === Type.COUNTERPARTY) {
            filterCounterParties(messages, inputText);
        }
    }, 300));
}

function splitDataIntoPages(messages: Record<string, string>, type: Type, data: any[]): void {
    const itemsPerPageSelection = document.getElementById("itemsPerPage") as HTMLSelectElement | null;
    const nextButton = document.getElementById("nextButton") as HTMLButtonElement | null;
    const previousButton = document.getElementById("previousButton") as HTMLButtonElement | null;
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

function calculateNumberOfPages(totalItems: number, itemsPerPage: number): number {
    return Math.ceil(totalItems / itemsPerPage);
}

function updateUI(data: any[], currentPageIndex: number, itemsPerPage: number, numberOfPages: number, messages: Record<string, string>,
                  type: Type, currentTableBody: HTMLElement): void {
    const startIndex = (currentPageIndex - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const paginatedData = data.slice(startIndex, endIndex);

    clearTable(currentTableBody);

    if (type === Type.TRANSACTION) {
        addRowsToTransactionTable(paginatedData, messages);
    } else if (type === Type.COUNTERPARTY) {
        addRowsToCounterPartyTable(paginatedData, messages);
    } else if (type === Type.CONTRACT) {
        addRowsToContractTable(paginatedData, messages)
    }

    const currentPage = document.getElementById("currentPage") as HTMLElement | null;
    const numberOfPagesElement = document.getElementById("numberOfPages") as HTMLElement | null;

    const lengthDifference = currentPageIndex - currentPageIndex;

    if (currentPage) currentPage.textContent = "&nbsp;".repeat(lengthDifference) + String(currentPageIndex);
    if (numberOfPagesElement) numberOfPagesElement.textContent = String(numberOfPages);

    const nextButton = document.getElementById("nextButton") as HTMLButtonElement | null;
    const previousButton = document.getElementById("previousButton") as HTMLButtonElement | null;

    if (nextButton) {
        nextButton.disabled = currentPageIndex >= numberOfPages;
        nextButton.classList.toggle("disabled", currentPageIndex >= numberOfPages);
    }

    if (previousButton) {
        previousButton.disabled = currentPageIndex <= 1;
        previousButton.classList.toggle("disabled", currentPageIndex <= 1);
    }
}

function clearTable(currentTableBody: HTMLElement): void {
    currentTableBody.innerHTML = "";
}

function setUpSorting(keepSubRowTogether: boolean = false): void {
    const currentTableBody = getCurrentTableBody();
    if (!currentTableBody) return;

    const columnIcons = document.querySelectorAll<HTMLElement>(".iconColor");

    columnIcons.forEach(columnIcon => {
        columnIcon.addEventListener("click", () => {
            const columnIndex = Number(columnIcon.getAttribute("data-index"));
            const dataTypeString = columnIcon.getAttribute("data-type") || "string";

            let dataType: DataTypeForSort;
            if (Object.values(DataTypeForSort).includes(dataTypeString as DataTypeForSort)) {
                dataType = dataTypeString as DataTypeForSort;
            }

            const isAscending = columnIcon.dataset.order === "asc";
            columnIcon.dataset.order = isAscending ? "desc" : "asc";

            sortTable(currentTableBody, columnIndex, dataType, isAscending, keepSubRowTogether);
        });
    });
}

function getCellValue(row: HTMLTableRowElement, columnIndex: number, dataType: DataTypeForSort): string {
    const cell = row.children[columnIndex];
    if (!cell) return "";

    if (dataType === DataTypeForSort.input) {
        const inputElement = cell.querySelector("input");
        return inputElement ? inputElement.value.trim() : "";
    }

    return cell.textContent?.trim() || "";
}

function sortTable(tableBody: HTMLElement, columnIndex: number, dataType: DataTypeForSort, isAscending: boolean, keepSubRowTogether: boolean): void {
    const fragment = document.createDocumentFragment();

    if (keepSubRowTogether) {
        const rowGroups = Array.from(tableBody.querySelectorAll<HTMLTableRowElement>(".rowGroup"));

        rowGroups.sort((groupA, groupB) => {
            const rowA = groupA.querySelector<HTMLTableRowElement>(".rowWithSubRow");
            const rowB = groupB.querySelector<HTMLTableRowElement>(".rowWithSubRow");

            if (!rowA || !rowB) return 0; // Keep order if any group lacks a main row

            const valueA = parseSortableValue(getCellValue(rowA, columnIndex, dataType), dataType);
            const valueB = parseSortableValue(getCellValue(rowB, columnIndex, dataType), dataType);

            return compareValues(valueA, valueB, isAscending);
        });

        rowGroups.forEach(group => fragment.appendChild(group));
    } else {
        const rows = Array.from(tableBody.querySelectorAll<HTMLTableRowElement>("tr"));

        rows.sort((rowA, rowB) => {
            const valueA = parseSortableValue(getCellValue(rowA, columnIndex, dataType), dataType);
            const valueB = parseSortableValue(getCellValue(rowB, columnIndex, dataType), dataType);

            return compareValues(valueA, valueB, isAscending);
        });

        rows.forEach(row => fragment.appendChild(row));
    }

    tableBody.replaceChildren(fragment);
}

function parseSortableValue(value: string, dataType: DataTypeForSort): string | number | Date {
    if (!value.trim()) return value; // Handle empty values gracefully

    try {
        switch (dataType) {
            case DataTypeForSort.number:
                return formatNumberForSort(value);
            case DataTypeForSort.date:
                return formatDateForSort(value);
            default:
                return value.toLowerCase();
        }
    } catch (error) {
        console.warn(`Error parsing value: "${value}"`, error);
        return value;
    }
}

function formatDateForSort(value: string): Date {
    const parsedDate = Date.parse(value);
    if (!isNaN(parsedDate)) return new Date(parsedDate);

    const dateParts = value.split(" ");
    const day = parseInt(dateParts[0], 10);
    const month = monthAbbreviations.indexOf(dateParts[1]);
    const year = parseInt(dateParts[2], 10);

    if (month === -1 || isNaN(day) || isNaN(year)) {
        throw new Error(`Invalid date format: "${value}"`);
    }

    return new Date(year, month, day);
}

function formatNumberForSort(value: string): number {
    const cleanedValue = value.replace(/[^\d.,-]/g, '').replace(',', '.');
    const numericValue = parseFloat(cleanedValue);

    if (!isNaN(numericValue)) return numericValue;

    throw new Error(`Invalid number format: "${value}"`);
}

function compareValues(valueA: string | number | Date, valueB: string | number | Date, isAscending: boolean): number {
    if (typeof valueA === "number" && typeof valueB === "number") {
        return isAscending ? valueA - valueB : valueB - valueA;
    }

    if (valueA instanceof Date && valueB instanceof Date) {
        return isAscending ? valueA.getTime() - valueB.getTime() : valueB.getTime() - valueA.getTime();
    }

    return isAscending ? valueA.toString().localeCompare(valueB.toString()) : valueB.toString().localeCompare(valueA.toString());
}

function classifyHiddenOrNot<T extends Transaction | CounterPartyDisplay>(
    type: Type
): { alreadyHidden: T[]; notHidden: T[] } {
    const data = getCheckedData(type) as T[];
    const alreadyHidden: T[] = [];
    const notHidden: T[] = [];

    data.forEach(item => {
        const isHidden = type === Type.TRANSACTION
            ? (item as Transaction).hidden
            : (item as CounterPartyDisplay).counterParty.hidden;

        isHidden ? alreadyHidden.push(item) : notHidden.push(item);
    });

    return { alreadyHidden, notHidden };
}

function getCheckedData(type: Type): Transaction[] | CounterPartyDisplay[] {
    const checkedRows = new Set(getCheckedRows());

    return (type === Type.TRANSACTION
        ? filteredTransactionData.filter(t => checkedRows.has(t.id))
        : filteredCounterPartyData.filter(c => checkedRows.has(c.counterParty.id))) as Transaction[] | CounterPartyDisplay[];
}

function updateRowStyle(newRow: HTMLElement, checkBox: HTMLInputElement): void {
    newRow.classList.toggle("selectedRow", checkBox.checked);
}

function updateRowGroupStyle(rowGroup: HTMLElement, checkBox: HTMLInputElement) {
    // Get the children of the rowGroup
    const children = rowGroup.children;

    // Loop through each child and add or remove the class
    for (let i = 0; i < children.length; i++) {
        if (checkBox.checked) {
            children[i].classList.add("selectedRow");
        } else {
            children[i].classList.remove("selectedRow");
        }
    }
}

function updateCachedDataAndUI(type: Type, messages: Record<string, string>, ids: number[]): void {
    const idSet = new Set(ids);

    if (type === Type.TRANSACTION) {
        filteredTransactionData.forEach(transaction => {
            if (idSet.has(transaction.id)) {
                transaction.hidden = !transaction.hidden;
            }
        });

        splitDataIntoPages(messages, Type.TRANSACTION, filteredTransactionData);
    } else {
        filteredCounterPartyData.forEach(counterParty => {
            if (idSet.has(counterParty.counterParty.id)) {
                counterParty.counterParty.hidden = !counterParty.counterParty.hidden;
            }
        });

        splitDataIntoPages(messages, Type.COUNTERPARTY, filteredCounterPartyData);
    }
}

function changeRowVisibility(type: Type): void {
    const currentTableBody = getCurrentTableBody();
    if (!currentTableBody) return;
    const rows = Array.from(currentTableBody.querySelectorAll(".hiddenRow"));

    if (type === Type.TRANSACTION) {
        transactionsHiddenToggle = !transactionsHiddenToggle;
    }
    else {
        counterPartiesHiddenToggle = !counterPartiesHiddenToggle;
    }

    rows.forEach(row => row.classList.toggle("hidden"));
}

function getIdsFromContainer(container: HTMLElement): number[] {
    return Array.from(
        container.querySelectorAll<HTMLElement>(".normalText")
    )
        .map(span => Number(span.id))
        .filter(id => !isNaN(id) && id !== 0);
}