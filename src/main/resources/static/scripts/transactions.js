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
            createCheckBoxForTable(newRow, null, transaction.id, transaction.hidden);
            animateElement(newRow);
            // Counterparty cell
            const counterparty = createAndAppendElement(newRow, "td");
            const counterpartyWrapper = createAndAppendElement(counterparty, "div", "justifyContentCenter");
            createAndAppendElement(counterpartyWrapper, "span", "bold", transaction.counterParty.name);
            // Contract cell
            const contract = createAndAppendElement(newRow, "td");
            if ((_a = transaction.contract) === null || _a === void 0 ? void 0 : _a.name) {
                const contractWrapper = createAndAppendElement(contract, "div", "justifyContentCenter");
                createAndAppendElement(contractWrapper, "span", "highlightCell highlightCellPink", transaction.counterParty.name);
            }
            // Category cell
            const category = createAndAppendElement(newRow, "td");
            if ((_b = transaction.category) === null || _b === void 0 ? void 0 : _b.name) {
                const categoryWrapper = createAndAppendElement(category, "div", "justifyContentCenter");
                createAndAppendElement(categoryWrapper, "span", "highlightCell highlightCellOrange", transaction.counterParty.name);
            }
            // Date cell
            const date = createAndAppendElement(newRow, "td");
            const dateWrapper = createAndAppendElement(date, "div", "justifyContentCenter");
            createAndAppendElement(dateWrapper, "span", "", transaction.counterParty.name);
            // Amount before cell
            const amountBefore = createAndAppendElement(newRow, "td");
            const amountBeforeWrapper = createAndAppendElement(amountBefore, "div", "justifyContentCenter");
            createAndAppendElement(amountBeforeWrapper, "span", "", formatNumber(transaction.amountInBankBefore, currency));
            // Amount cell with positive/negative styling
            const amount = createAndAppendElement(newRow, "td");
            const amountClass = transaction.amount >= 0 ? "positive" : "negative";
            const amountWrapper = createAndAppendElement(amount, "div", `${amountClass}`);
            createAndAppendElement(amountWrapper, "span", "", formatNumber(transaction.amount, currency));
            // Amount after cell
            const amountInBankAfter = createAndAppendElement(newRow, "td");
            const amountInBankAfterWrapper = createAndAppendElement(amountInBankAfter, "div", "justifyContentCenter");
            createAndAppendElement(amountInBankAfterWrapper, "span", "tdMargin", formatNumber(transaction.amountInBankAfter, currency));
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