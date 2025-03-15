async function buildTransactions() {
    var _a, _b, _c, _d;
    const messages = await loadLocalization("transactions");
    if (!messages)
        return;
    setMonths(messages);
    const type = Type.TRANSACTION;
    await loadData(type, messages);
    splitDataIntoPages(messages, type, transactionData);
    setUpSorting();
    (_a = document.getElementById("searchBarInput")) === null || _a === void 0 ? void 0 : _a.addEventListener("input", () => searchTable(messages, type));
    (_b = document.getElementById("changeHiddenButton")) === null || _b === void 0 ? void 0 : _b.addEventListener("click", () => showChangeHiddenDialog(type, messages));
    (_c = document.getElementById("changeContractButton")) === null || _c === void 0 ? void 0 : _c.addEventListener("click", async () => {
        await buildChangeContract("/transactions", getCheckedData(type));
    });
    (_d = document.getElementById("showHiddenRows")) === null || _d === void 0 ? void 0 : _d.addEventListener("change", () => changeRowVisibility(type));
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
            animateElement(newRow);
            // Counterparty cell
            const counterparty = createAndAppendElement(newRow, "td", "", "", { style: "width: 25%" });
            createAndAppendElement(counterparty, "span", "tdMargin", transaction.counterParty.name, {
                style: "font-weight: bold; width: 25%",
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
        splitDataIntoPages(messages, Type.TRANSACTION, filteredTransactionData);
    }
    catch (error) {
        console.error("Unexpected error in filterTransactions:", error);
        showAlert("ERROR", messages["error_generic"]);
    }
}
function transactionToListElementObjectArray(transactions) {
    const listElementObjects = [];
    const currency = getCurrentCurrencySymbol();
    transactions.forEach(transaction => {
        const listElementObject = {
            id: transaction.id,
            text: transaction.counterParty.name,
            toolTip: formatNumber(transaction.amount, currency).toString()
        };
        listElementObjects.push(listElementObject);
    });
    return listElementObjects;
}
//# sourceMappingURL=transactions.js.map