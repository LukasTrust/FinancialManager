async function buildTransactions() {
    const messages = await fetchLocalization("transactions");

    monthAbbreviations = messages["monthAbbreviations"]
        .split("', '") // Split by ', ' to separate the months
        .map(month => month.replace(/'/g, ''));
    transactionsHiddenToggle = false;

    await loadTransactions(messages);

    setUpSorting();

    const searchBarInput = document.getElementById("searchBarInput");
    searchBarInput.addEventListener("input", () => searchTable(messages, "transaction"));

    const changeHiddenButton = document.getElementById("changeHiddenButton");
    changeHiddenButton.addEventListener("click", () => showChangeHiddenDialog(messages));

    const showHiddenRows = document.getElementById("showHiddenRows");
    showHiddenRows.addEventListener("change", () => changeRowVisibility());

    const selectAll = document.getElementById("selectAll");
    selectAll.addEventListener("change", () => {
        const tableBody = getCurrentTableBody();

        const checkboxes = Array.from(tableBody.querySelectorAll('tr:not(.hidden) td input[type="checkbox"]'));

        // Check or uncheck all based on the state of the 'showHiddenRows' checkbox
        checkboxes.forEach(checkbox => {
            checkbox.checked = selectAll.checked;
        });
    });
}

async function loadTransactions(messages) {

    try {
        const response = await fetch(`/transactions/${bankAccountId}/data`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            const responseBody = await response.json();
            showAlert(responseBody.alertType, responseBody.message);

            showAlert("ERROR", messages["error_loadingTransactions"]);
            return;
        }

        transactionData = await response.json();

        filteredTransactionData = transactionData;

        splitDataIntoPages(messages, "transaction", transactionData);
    } catch (error) {
        console.error("There was a error the create bank request:", error);
        showAlert('error', messages["error_generic"]);
    }
}

function addRowsToTransactionTable(data, messages) {
    try {
        const tableBody = getCurrentTableBody();

        const currency = getCurrentCurrencySymbol();

        data.forEach((transaction, index) => {
            if (!transaction || typeof transaction !== "object") {
                console.warn(`Warning: Skipping invalid transaction at index ${index}.`, transaction);
                return;
            }

            let rowClass = null;

            if (transaction.hidden) {
                rowClass = "hiddenRow";
                if (transactionsHiddenToggle === false){
                    rowClass += " hidden";
                }
            }

            const newRow = createAndAppendElement(tableBody, "tr", rowClass, null, {id: transaction.id});

            // Checkbox cell
            const trCheckBox = createAndAppendElement(newRow, "td", null, "", {style: "width: 5%"});
            const checkBox = createAndAppendElement(trCheckBox, "input", "tableCheckbox", "", {
                type: "checkbox",
                id: transaction.id,
                style: "margin-left: 10px;",
            });

            checkBox.addEventListener("change", () => updateRowStyle(newRow, checkBox));

            newRow.addEventListener("click", (event) => {
                if (event.target.type === "checkbox") return; // Don't toggle when clicking checkbox
                checkBox.checked = !checkBox.checked;
                updateRowStyle(newRow, checkBox);
            });

            // Counterparty cell
            let counterparty = createAndAppendElement(newRow, "td", "", "", {style: "width: 25%"});
            createAndAppendElement(counterparty, "span", "tdMargin", transaction.counterParty.name, {
                style: "font-weight: bold;",
            });

            // Contract cell
            let contract = createAndAppendElement(newRow, "td", "", "", {style: "width: 15%"});
            if (transaction.contract?.name) {
                createAndAppendElement(contract, "span", "tdMargin highlightCell highlightCellPink",
                    transaction.contract.name);
            }

            // Category cell
            let category = createAndAppendElement(newRow, "td", "", "", {style: "width: 15%"});
            if (transaction.category?.name) {
                createAndAppendElement(category, "span", "tdMargin highlightCell highlightCellOrange", transaction.category.name);
            }

            // Date cell
            let date = createAndAppendElement(newRow, "td", "rightAligned", "", {style: "width: 10%"});
            createAndAppendElement(date, "span", "tdMargin", formatDateString(transaction.date));

            // Amount before cell
            let amountBefore = createAndAppendElement(newRow, "td", "rightAligned", "", {style: "width: 10%"});
            createAndAppendElement(amountBefore, "span", "tdMargin", formatNumber(transaction.amountInBankBefore, currency));

            // Amount cell with positive/negative styling
            const amount = createAndAppendElement(newRow, "td", "rightAligned", "", {style: "width: 10%"});
            const amountClass = transaction.amount >= 0 ? "positive" : "negative";
            createAndAppendElement(amount, "span", `tdMargin rightAligned ${amountClass}`,
                formatNumber(transaction.amount, currency)
            );

            // Amount after cell
            let amountInBankAfter = createAndAppendElement(newRow, "td", "rightAligned", "", {style: "width: 10%"});

            createAndAppendElement(amountInBankAfter, "span", "tdMargin",
                formatNumber(transaction.amountInBankAfter, currency), {style: "margin-right: 30px;"});
        });
    } catch (error) {
        console.error("Unexpected error in addRowsToTransactionTable:", error);
        showAlert("ERROR", messages["error_generic"]);
    }
}

function updateRowStyle(newRow, checkBox) {
    newRow.classList.toggle("selectedRow", checkBox.checked);
}

function filterTransactions(messages, searchString) {
    try {
        filteredTransactionData = transactionData.filter(transaction =>
            transaction.counterParty?.name?.toLowerCase().includes(searchString) ||
            transaction.contract?.name?.toLowerCase().includes(searchString) ||
            transaction.category?.name?.toLowerCase().includes(searchString) ||
            transaction.date?.toLowerCase().includes(searchString) ||
            transaction.amountInBankBefore?.toString().toLowerCase().includes(searchString) ||
            transaction.amount?.toString().toLowerCase().includes(searchString) ||
            transaction.amountInBankAfter?.toString().toLowerCase().includes(searchString)
        );

        splitDataIntoPages(messages, "transaction", filteredTransactionData);
    } catch (error) {
        console.error("Unexpected error in filterTransactions:", error);
        showAlert("ERROR", messages["error_generic"]);
    }
}

function showChangeHiddenDialog(messages) {
    const checkedRows = new Set(getCheckedRows());
    const alreadyHiddenTransactions = [];
    const notHiddenTransactions = [];

    filteredTransactionData.forEach(transaction => {
        if (checkedRows.has(transaction.id)) {
            transaction.hidden
                ? alreadyHiddenTransactions.push(transaction)
                : notHiddenTransactions.push(transaction);
        }
    });

    const flexContainerColumn = createAndAppendElement("", "div", "flexContainerColumn");
    const header = createDialogHeader(flexContainerColumn, messages["changeHiddenHeader"], "bi bi-eye");
    const listContainer = createAndAppendElement(flexContainerColumn, "div", "flexContainerSpaced");

    // Close Button
    const closeButton = createAndAppendElement(header, "button", "iconButton", "",
        {style: "margin-left: auto; margin-right: -10px; margin-top: -20px"});
    createAndAppendElement(closeButton, "i", "bi bi-x-lg", "", {
        style: "color: red; font-size: 1.5rem"
    });

    const model = createModal(flexContainerColumn, closeButton);

    // Left Side: Already Hidden Transactions
    const leftContainer = createListSection(
        listContainer,
        messages["alreadyHiddenHeader"],
        alreadyHiddenTransactions
    );

    createDialogButton(
        leftContainer,
        "bi bi-eye",
        messages["unHide"],
        "left",
        async () => await updateTransactionVisibility(messages, model, leftContainer, false)
    );

    // Right Side: Not Hidden Transactions
    const rightContainer = createListSection(
        listContainer,
        messages["notHiddenHeader"],
        notHiddenTransactions
    );

    createDialogButton(
        rightContainer,
        "bi bi-eye-slash",
        messages["hide"],
        "right",
        async () => await updateTransactionVisibility(messages, model, rightContainer, true)
    );
}

async function updateTransactionVisibility(messages, model, listContainer, hide) {
    try {
        const ids = Array.from(listContainer.querySelectorAll("div span"))
            .map(span => Number(span.closest("span").id));

        if (ids.length === 0) return;

        const endpoint = hide ? "hideTransactions" : "unHideTransactions";
        const response = await fetch(`/transactions/${bankAccountId}/data/${endpoint}`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(ids)
        });

        const responseBody = await response.json();

        showAlert(responseBody.alertType, responseBody.message, model);

        updateCashedTransactionsAndUI(messages, ids);
    } catch (error) {
        console.error("Unexpected error in updateTransactionVisibility:", error);
        showAlert("ERROR", messages["error_generic"], model);
    }
}

function updateCashedTransactionsAndUI(messages, ids) {
    filteredTransactionData.forEach(transaction => {
        if (ids.includes(transaction.id)) {
            transaction.hidden = !transaction.hidden;
        }
    });

    splitDataIntoPages(messages, "transaction", filteredTransactionData);
}

function changeRowVisibility() {
    const currentTableBody = getCurrentTableBody();

    const rows = Array.from(currentTableBody.querySelectorAll("tr.hiddenRow"));

    transactionsHiddenToggle = !transactionsHiddenToggle;
    rows.forEach(row => row.classList.toggle("hidden"));
}