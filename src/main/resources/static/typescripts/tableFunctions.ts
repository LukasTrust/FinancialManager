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

function searchTable(messages: Record<string, string>, type: string): void {
    const searchBarInput = document.getElementById("searchBarInput") as HTMLInputElement | null;

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


function splitDataIntoPages(messages: Record<string, string>, type: string, data: any[]): void {
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

function updateUI(
    data: any[],
    currentPageIndex: number,
    itemsPerPage: number,
    numberOfPages: number,
    messages: Record<string, string>,
    type: string,
    currentTableBody: HTMLElement
): void {
    const startIndex = (currentPageIndex - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const paginatedData = data.slice(startIndex, endIndex);

    clearTable(currentTableBody);

    if (type === "transaction") {
        addRowsToTransactionTable(paginatedData, messages);
    }

    const currentPage = document.getElementById("currentPage") as HTMLElement | null;
    const numberOfPagesElement = document.getElementById("numberOfPages") as HTMLElement | null;

    if (currentPage) currentPage.textContent = String(currentPageIndex);
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

function setUpSorting(): void {
    const currentTableBody = getCurrentTableBody();
    if (!currentTableBody) return;

    const columnIcons = document.querySelectorAll<HTMLElement>(".iconColor");

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

function sortTable(tableBody: HTMLElement, columnIndex: number, dataType: string, isAscending: boolean): void {
    const rows = Array.from(tableBody.querySelectorAll<HTMLTableRowElement>("tr"));

    rows.sort((rowA, rowB) => {
        const cellA = rowA.children[columnIndex]?.textContent?.trim() || "";
        const cellB = rowB.children[columnIndex]?.textContent?.trim() || "";

        const valueA = parseSortableValue(cellA, dataType);
        const valueB = parseSortableValue(cellB, dataType);

        return compareValues(valueA, valueB, isAscending);
    });

    const fragment = document.createDocumentFragment();
    rows.forEach(row => fragment.appendChild(row));

    tableBody.innerHTML = "";
    tableBody.appendChild(fragment);
}

function parseSortableValue(value: string, dataType: string): string | number | Date {
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
        return value;
    }
}

function formatDateForSort(value: string): Date {
    const dateParts = value.split(" ");
    const day = parseInt(dateParts[0], 10);
    const month = monthAbbreviations.indexOf(dateParts[1]);
    const year = parseInt(dateParts[2], 10);

    if (month === -1 || isNaN(day) || isNaN(year)) {
        throw new Error("Invalid date format");
    }

    return new Date(year, month, day);
}

function formatNumberForSort(value: string): number {
    const numericValue = parseFloat(value.replace(/[^0-9.,-]/g, '').replace(',', '.'));
    if (!isNaN(numericValue)) {
        return numericValue;
    }

    throw new Error("Invalid number format");
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
