async function buildTransactions() {
    var _a, _b, _c, _d;
    const messages = await fetchLocalization("transactions");
    if (!messages)
        return;
    monthAbbreviations = messages["monthAbbreviations"]
        .split("', '")
        .map((month) => month.replace(/'/g, ''));
    transactionsHiddenToggle = false;
    await loadTransactions(messages);
    splitDataIntoPages(messages, "transaction", transactionData);
    setUpSorting();
    (_a = document.getElementById("searchBarInput")) === null || _a === void 0 ? void 0 : _a.addEventListener("input", () => searchTable(messages, "transaction"));
    (_b = document.getElementById("changeHiddenButton")) === null || _b === void 0 ? void 0 : _b.addEventListener("click", () => showChangeHiddenDialog(messages));
    (_c = document.getElementById("changeContractButton")) === null || _c === void 0 ? void 0 : _c.addEventListener("click", async () => {
        await buildChangeContract("/transactions", getCheckedTransactions());
    });
    (_d = document.getElementById("showHiddenRows")) === null || _d === void 0 ? void 0 : _d.addEventListener("change", () => changeRowVisibility());
}
async function loadTransactions(messages) {
    try {
        const response = await fetch(`/transactions/${bankAccountId}/data`, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        });
        if (!response.ok) {
            await showAlertFromResponse(response);
            return;
        }
        transactionData = await response.json();
        filteredTransactionData = transactionData;
    }
    catch (error) {
        console.error("There was an error loading the transactions:", error);
        showAlert('error', messages["error_generic"]);
    }
}
function addRowsToTransactionTable(data, messages) {
    try {
        const tableBody = getCurrentTableBody();
        if (!tableBody)
            return;
        const currency = getCurrentCurrencySymbol();
        data.forEach((transaction, index) => {
            var _a, _b;
            if (!transaction || typeof transaction !== "object") {
                console.warn(`Warning: Skipping invalid transaction at index ${index}.`, transaction);
                return;
            }
            let rowClass = transaction.hidden ? "hiddenRow" : null;
            if (rowClass && !transactionsHiddenToggle) {
                rowClass += " hidden";
            }
            const newRow = createAndAppendElement(tableBody, "tr", rowClass, null, { id: transaction.id.toString() });
            createCheckBoxForTable(newRow, transaction.id, transaction.hidden);
            // Counterparty cell
            const counterparty = createAndAppendElement(newRow, "td", "", "", { style: "width: 25%" });
            createAndAppendElement(counterparty, "span", "tdMargin", transaction.counterParty.name, {
                style: "font-weight: bold;",
            });
            // Contract cell
            const contract = createAndAppendElement(newRow, "td", "", "", { style: "width: 15%" });
            if ((_a = transaction.contract) === null || _a === void 0 ? void 0 : _a.name) {
                createAndAppendElement(contract, "span", "tdMargin highlightCell highlightCellPink", transaction.contract.name);
            }
            // Category cell
            const category = createAndAppendElement(newRow, "td", "", "", { style: "width: 15%" });
            if ((_b = transaction.category) === null || _b === void 0 ? void 0 : _b.name) {
                createAndAppendElement(category, "span", "tdMargin highlightCell highlightCellOrange", transaction.category.name);
            }
            // Date cell
            const date = createAndAppendElement(newRow, "td", "rightAligned", "", { style: "width: 10%" });
            createAndAppendElement(date, "span", "tdMargin", formatDateString(transaction.date));
            // Amount before cell
            const amountBefore = createAndAppendElement(newRow, "td", "rightAligned", "", { style: "width: 10%" });
            createAndAppendElement(amountBefore, "span", "tdMargin", formatNumber(transaction.amountInBankBefore, currency));
            // Amount cell with positive/negative styling
            const amount = createAndAppendElement(newRow, "td", "rightAligned", "", { style: "width: 10%" });
            const amountClass = transaction.amount >= 0 ? "positive" : "negative";
            createAndAppendElement(amount, "span", `tdMargin rightAligned ${amountClass}`, formatNumber(transaction.amount, currency));
            // Amount after cell
            const amountInBankAfter = createAndAppendElement(newRow, "td", "rightAligned", "", { style: "width: 10%" });
            createAndAppendElement(amountInBankAfter, "span", "tdMargin", formatNumber(transaction.amountInBankAfter, currency), { style: "margin-right: 30px;" });
        });
    }
    catch (error) {
        console.error("Unexpected error in addRowsToTransactionTable:", error);
        showAlert("ERROR", messages["error_generic"]);
    }
}
function updateRowStyle(newRow, checkBox) {
    newRow.classList.toggle("selectedRow", checkBox.checked);
}
function updateRowGroupStyle(rowGroup, checkBox) {
    if (checkBox.checked) {
        rowGroup.classList.add("selectedRow");
    }
    else {
        rowGroup.classList.remove("selectedRow");
    }
}
function filterTransactions(messages, searchString) {
    try {
        filteredTransactionData = transactionData.filter(transaction => {
            var _a, _b, _c, _d, _e, _f, _g, _h, _j, _k;
            return ((_b = (_a = transaction.counterParty) === null || _a === void 0 ? void 0 : _a.name) === null || _b === void 0 ? void 0 : _b.toLowerCase().includes(searchString)) ||
                ((_d = (_c = transaction.contract) === null || _c === void 0 ? void 0 : _c.name) === null || _d === void 0 ? void 0 : _d.toLowerCase().includes(searchString)) ||
                ((_f = (_e = transaction.category) === null || _e === void 0 ? void 0 : _e.name) === null || _f === void 0 ? void 0 : _f.toLowerCase().includes(searchString)) ||
                ((_g = transaction.date) === null || _g === void 0 ? void 0 : _g.toLowerCase().includes(searchString)) ||
                ((_h = transaction.amountInBankBefore) === null || _h === void 0 ? void 0 : _h.toString().toLowerCase().includes(searchString)) ||
                ((_j = transaction.amount) === null || _j === void 0 ? void 0 : _j.toString().toLowerCase().includes(searchString)) ||
                ((_k = transaction.amountInBankAfter) === null || _k === void 0 ? void 0 : _k.toString().toLowerCase().includes(searchString));
        });
        splitDataIntoPages(messages, "transaction", filteredTransactionData);
    }
    catch (error) {
        console.error("Unexpected error in filterTransactions:", error);
        showAlert("ERROR", messages["error_generic"]);
    }
}
function getCheckedTransactions() {
    const checkedRows = new Set(getCheckedRows());
    return filteredTransactionData.filter(transaction => checkedRows.has(transaction.id));
}
function changeRowVisibility() {
    const currentTableBody = getCurrentTableBody();
    if (!currentTableBody)
        return;
    const rows = Array.from(currentTableBody.querySelectorAll("tr.hiddenRow"));
    transactionsHiddenToggle = !transactionsHiddenToggle;
    rows.forEach(row => row.classList.toggle("hidden"));
}
function showChangeHiddenDialog(messages) {
    const { alreadyHidden, notHidden } = classifyHiddenTransactions();
    const dialogContent = createDialogContent(messages["changeHiddenHeader"], "bi bi-eye");
    const listContainer = createAndAppendElement(dialogContent, "div", "flexContainerSpaced");
    // Hidden transactions
    const leftSide = createListSection(listContainer, messages["alreadyHiddenHeader"], alreadyHidden);
    const rightSide = createListSection(listContainer, messages["notHiddenHeader"], notHidden);
    createDialogButton(leftSide, "bi bi-eye", messages["unHide"], "left", () => updateTransactionVisibility(messages, dialogContent, leftSide, rightSide.querySelector(".listContainerColumn"), false));
    // Not hidden transactions
    createDialogButton(rightSide, "bi bi-eye-slash", messages["hide"], "right", () => updateTransactionVisibility(messages, dialogContent, rightSide, leftSide.querySelector(".listContainerColumn"), true));
}
function classifyHiddenTransactions() {
    const alreadyHidden = [];
    const notHidden = [];
    const transactions = getCheckedTransactions();
    transactions.forEach(transaction => {
        transaction.hidden ? alreadyHidden.push(transaction) : notHidden.push(transaction);
    });
    return { alreadyHidden, notHidden };
}
async function updateTransactionVisibility(messages, model, updatedContainer, moveToContainer, hide) {
    try {
        // Get all transaction IDs
        const transactionIds = Array.from(updatedContainer.querySelectorAll(".normalText"))
            .map(span => Number(span.id))
            .filter(id => !isNaN(id) && id !== 0); // Ensure valid IDs
        if (transactionIds.length === 0) {
            showAlert("INFO", messages["noTransactionsUpdated"], model);
            return;
        }
        const endpoint = hide ? "hideTransactions" : "unHideTransactions";
        const response = await fetch(`/transactions/${bankAccountId}/data/${endpoint}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(transactionIds),
        });
        const responseBody = await response.json();
        showAlert(responseBody.alertType, responseBody.message, model);
        if (responseBody.alertType === AlertType.SUCCESS) {
            // Animate and move elements
            moveElements(updatedContainer, moveToContainer);
            updateCachedTransactionsAndUI(messages, transactionIds);
        }
    }
    catch (error) {
        console.error("Unexpected error in updateTransactionVisibility:", error);
        showAlert("ERROR", messages["error_generic"], model);
    }
}
function updateCachedTransactionsAndUI(messages, transactionIds) {
    filteredTransactionData.forEach((transaction) => {
        if (transactionIds.includes(transaction.id)) {
            transaction.hidden = !transaction.hidden;
        }
    });
    splitDataIntoPages(messages, "transaction", filteredTransactionData);
}
//# sourceMappingURL=transactions.js.map