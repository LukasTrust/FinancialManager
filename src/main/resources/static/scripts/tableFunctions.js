function searchTable(messages, type) {
    const searchBarInput = document.getElementById("searchBarInput");

    if (!searchBarInput) {
        console.error("Search bar input element not found!");
        return;
    }

    // Add debounced event listener for input
    searchBarInput.addEventListener("input", debounce(() => {
        const inputText = searchBarInput.value.trim().toLowerCase();

        if (inputText.length <= 2) {
            // Reset to original data if search input is too short
            if (type === "transaction") {
                filteredTransactionData = transactionData;
                splitDataIntoPages(messages, type, transactionData);
            }
            return;
        }

        // Filter transactions based on search input
        if (type === "transaction") {
            filterTransactions(messages, inputText);
        }
    }, 300)); // 300ms debounce delay
}

function debounce(func, delay) {
    let timeoutId;
    return function (...args) {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(() => func.apply(this, args), delay);
    };
}

function splitDataIntoPages(messages, type, data) {
    const itemsPerPageSelection = document.getElementById("itemsPerPage");
    const nextButton = document.getElementById("nextButton");
    const previousButton = document.getElementById("previousButton");
    const currentTableBody = document.getElementById("tableBody");

    let currentPageIndex = 1; // Track the current page index
    let itemsPerPage = parseInt(itemsPerPageSelection.value); // Initial items per page
    let numberOfPages = calculateNumberOfPages(data.length, itemsPerPage); // Calculate total pages

    // Initialize the UI
    updateUI(data, currentPageIndex, itemsPerPage, numberOfPages, messages, type, currentTableBody);

    // Event listener for items per page change
    itemsPerPageSelection.addEventListener("change", () => {
        itemsPerPage = parseInt(itemsPerPageSelection.value);
        numberOfPages = calculateNumberOfPages(data.length, itemsPerPage);
        currentPageIndex = 1; // Reset to the first page
        updateUI(data, currentPageIndex, itemsPerPage, numberOfPages, messages, type, currentTableBody);
    });

    // Event listener for next button
    nextButton.addEventListener("click", () => {
        if (currentPageIndex < numberOfPages) {
            currentPageIndex++;
            updateUI(data, currentPageIndex, itemsPerPage, numberOfPages, messages, type, currentTableBody);
        }
    });

    // Event listener for previous button
    previousButton.addEventListener("click", () => {
        if (currentPageIndex > 1) {
            currentPageIndex--;
            updateUI(data, currentPageIndex, itemsPerPage, numberOfPages, messages, type, currentTableBody);
        }
    });
}

// Helper function to calculate the number of pages
function calculateNumberOfPages(totalItems, itemsPerPage) {
    return Math.ceil(totalItems / itemsPerPage);
}

// Helper function to update the UI
function updateUI(data, currentPageIndex, itemsPerPage, numberOfPages, messages, type, currentTableBody) {
    const startIndex = (currentPageIndex - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const paginatedData = data.slice(startIndex, endIndex);

    // Clear the existing table rows
    clearTable(currentTableBody);

    // Update the table with paginated data
    switch (type) {
        case "transaction":
            addRowsToTransactionTable(paginatedData, messages);
            break;
    }

    const lengthDifference = currentPageIndex.length - currentPageIndex.length;

    // Update the current page display
    const currentPage = document.getElementById("currentPage");
    console.log("&nbsp;".repeat(lengthDifference) + currentPageIndex);
    currentPage.textContent = "&nbsp;".repeat(lengthDifference) + currentPageIndex;

    const numberOfPagesElement = document.getElementById("numberOfPages");
    numberOfPagesElement.textContent = numberOfPages;

    // Enable/disable next and previous buttons
    const nextButton = document.getElementById("nextButton");
    const previousButton = document.getElementById("previousButton");

    // Add or remove the 'disabled' class based on conditions
    if (currentPageIndex >= numberOfPages) {
        nextButton.classList.add("disabled");
        nextButton.disabled = true; // Disable the button functionally
    } else {
        nextButton.classList.remove("disabled");
        nextButton.disabled = false; // Enable the button functionally
    }

    if (currentPageIndex <= 1) {
        previousButton.classList.add("disabled");
        previousButton.disabled = true; // Disable the button functionally
    } else {
        previousButton.classList.remove("disabled");
        previousButton.disabled = false; // Enable the button functionally
    }
}

// Helper function to clear the table
function clearTable(currentTableBody) {
    if (currentTableBody) {
        currentTableBody.innerHTML = ""; // Clear all rows
    }
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