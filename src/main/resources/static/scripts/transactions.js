async function buildTransactions() {
    const messages = await fetchLocalization("transactions");

    monthAbbreviations = messages["monthAbbreviations"]
        .split("', '") // Split by ', ' to separate the months
        .map(month => month.replace(/'/g, ''));
    transactionsHiddenToggle = false;

    await loadTransactions(messages);
    splitDataIntoPages(messages, "transaction", transactionData);
    setUpSorting();

    document.getElementById("searchBarInput")
        .addEventListener("input", () => searchTable(messages, "transaction"));

    document.getElementById("changeHiddenButton")
        .addEventListener("click", () => showChangeHiddenDialog(messages));

    document.getElementById("changeContractButton")
        .addEventListener("click", async () => {
            await buildChangeContract("/transactions", getCheckedTransactions());
        });

    document.getElementById("showHiddenRows")
        .addEventListener("change", () => changeRowVisibility());
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
    } catch (error) {
        console.error("There was an error loading the transactions:", error);
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
                if (transactionsHiddenToggle === false) {
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

            if (rowClass !== null) {
                createAndAppendElement(trCheckBox, "span", "bi bi-eye-slashh");
            }

            checkBox.addEventListener("change", () => updateRowStyle(newRow, checkBox));

            newRow.addEventListener("click", (event) => {
                if (event.target.type === "checkbox") return;
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

function getCheckedTransactions() {
    const checkedRows = new Set(getCheckedRows());
    let transactions = [];

    filteredTransactionData.forEach(transaction => {
        if (checkedRows.has(transaction.id))
            transactions.push(transaction);
    });

    return transactions;
}

function classifyHiddenTransactions() {
    const alreadyHidden = [];
    const notHidden = [];

    const transactions = getCheckedTransactions();

    transactions.forEach(transaction => {
        transaction.hidden
            ? alreadyHidden.push(transaction)
            : notHidden.push(transaction);
    });

    return {alreadyHidden, notHidden};
}

function showChangeHiddenDialog(messages) {
    const {alreadyHidden, notHidden} = classifyHiddenTransactions();

    const dialogContent = createDialogContent(messages["changeHiddenHeader"], "bi bi-eye");
    const listContainer = createAndAppendElement(dialogContent, "div", "flexContainerSpaced");

    // Hidden transactions
    const leftSide = createListSection(listContainer, messages["alreadyHiddenHeader"], alreadyHidden);
    createDialogButton(leftSide, "bi bi-eye", messages["unHide"], "left", () => updateTransactionVisibility(messages, dialogContent, leftSide, false));

    // Not hidden transactions
    const rightSide = createListSection(listContainer, messages["notHiddenHeader"], notHidden);
    createDialogButton(rightSide, "bi bi-eye-slash", messages["hide"], "right", () => updateTransactionVisibility(messages, dialogContent, rightSide, true));
}

async function updateTransactionVisibility(messages, model, listContainer, hide) {
    try {
        const ids = Array.from(listContainer.querySelectorAll("div span"))
            .map(span => Number(span.id)) // Assuming the ID is directly on the span
            .filter(id => id !== 0); // Remove 0 if present

        if (ids.length === 0) {
            showAlert("INFO", messages["noTransactionsUpdated"], model);
            return;
        }

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