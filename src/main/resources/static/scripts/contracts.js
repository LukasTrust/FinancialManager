async function buildContracts() {
    var _a, _b, _c, _d, _e;
    const messages = await loadLocalization("contracts");
    if (!messages)
        return;
    setMonths(messages);
    const type = Type.CONTRACT;
    await loadData(type, messages);
    splitDataIntoPages(messages, type, contractData);
    setUpSorting(true);
    (_a = document.getElementById("searchBarInput")) === null || _a === void 0 ? void 0 : _a.addEventListener("input", () => searchTable(messages, type));
    (_b = document.getElementById("showHiddenRows")) === null || _b === void 0 ? void 0 : _b.addEventListener("change", () => changeRowVisibility(type));
    (_c = document.getElementById("changeHiddenButton")) === null || _c === void 0 ? void 0 : _c.addEventListener("click", () => showChangeHiddenDialog(type, messages));
    (_d = document.getElementById("mergeButton")) === null || _d === void 0 ? void 0 : _d.addEventListener("click", async () => await buildMergeContracts("/contracts", getCheckedData(Type.CONTRACT).map(contract => contract.contract)));
    (_e = document.getElementById("deleteButton")) === null || _e === void 0 ? void 0 : _e.addEventListener("click", () => showDeleteContractDialog(messages));
}
async function deleteContracts(dialog, listSection, messages) {
    try {
        const contracts = getCheckedData(Type.CONTRACT);
        const ids = contracts
            .map(contract => Number(contract.contract.id))
            .filter(id => !isNaN(id) && id !== 0);
        const response = await fetch(`/contracts/${bankAccountId}/data/deleteContracts`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(ids)
        });
        if (!response.ok) {
            await showAlertFromResponse(response);
            return;
        }
        const responseBody = await response.json();
        showAlert(responseBody.alertType, responseBody.message, dialog);
        if (response.ok) {
            removeElements(listSection);
            removeDeletedCategories(ids, messages);
        }
    }
    catch (error) {
        console.error("There was an error deleting the contracts", error);
        showAlert('error', messages["error_generic"]);
    }
}
function showDeleteContractDialog(messages) {
    const dialogContent = createDialogContent(messages["deleteHeader"], "bi bi bi-trash-fill", 0, 0);
    createAndAppendElement(dialogContent, "h2", "marginBottom marginLeftBig", messages["deleteInfo"]);
    const contracts = getCheckedData(Type.CONTRACT);
    const listSection = createListSection(dialogContent, messages["contractsToDelete"], Type.CONTRACT, contracts, false, true, false);
    if (!contracts || contracts.length === 0) {
        const childContainer = listSection.querySelector('div.flexGrow');
        createAndAppendElement(childContainer, "h2", "red marginTopBig", messages["noContractsToDelete"]);
    }
    const submitButton = createAndAppendElement(dialogContent, "button", "iconButton tooltip tooltipBottom marginTopBig");
    createAndAppendElement(submitButton, "i", "bi bi-trash-fill");
    createAndAppendElement(submitButton, "span", "normalText", messages["submitDelete"]);
    createAndAppendElement(submitButton, "span", "tooltipText", messages["submitDeleteTooltip"]);
    submitButton.addEventListener("click", async (event) => {
        event.preventDefault();
        await deleteContracts(dialogContent, listSection, messages);
    });
}
function addRowsToContractTable(data, messages) {
    try {
        const tableBody = getCurrentTableBody();
        if (!tableBody)
            return;
        const currency = getCurrentCurrencySymbol();
        data.forEach(contractDisplay => {
            createContractRow(tableBody, contractDisplay, currency, messages);
        });
    }
    catch (error) {
        console.error("Unexpected error in addRowsToCounterPartyTable:", error);
        showAlert("ERROR", messages["error_generic"]);
    }
}
function createContractRow(tableBody, contractDisplay, currency, messages) {
    if (!contractDisplay || typeof contractDisplay !== "object") {
        console.warn(`Warning: Skipping invalid counterParty:.`, contractDisplay);
        return;
    }
    const contract = contractDisplay.contract;
    let hiddenClass = contract.hidden ? " hiddenRow" : "";
    if (contract.hidden && !counterPartiesHiddenToggle) {
        hiddenClass += " hidden";
    }
    // Main row
    const newRow = createAndAppendElement(tableBody, "tr", "mainRow" + hiddenClass, "", {
        id: contract.id.toString(),
        "data-sort-key": contract.id.toString()
    });
    createCheckBoxForTable(newRow, null, contract.id, contract.hidden);
    // Name cell
    const name = createAndAppendElement(newRow, "td");
    const nameInput = createInputBox(name, "bi bi-pencil-fill", "name", "text", contract.name);
    debounceInputChange(nameInput, (id, newValue, messages) => updateField(id, "name", newValue, messages, Type.CONTRACT), contract.id, messages);
    // Description Cell
    const description = createAndAppendElement(newRow, "td");
    const descriptionInput = createInputBox(description, "bi bi-pencil-fill", "name", "text", contract.description);
    debounceInputChange(descriptionInput, (id, newValue, messages) => updateField(id, "description", newValue, messages, Type.CONTRACT), contract.id, messages);
    // Transaction count
    const transactionCount = createAndAppendElement(newRow, "td");
    const transactionCountWrapper = createAndAppendElement(transactionCount, "div", "justifyContentCenter");
    createAndAppendElement(transactionCountWrapper, "span", "tdMargin", contractDisplay.transactionCount.toString());
    // Total amount
    const totalAmount = createAndAppendElement(newRow, "td");
    const totalAmountWrapper = createAndAppendElement(totalAmount, "div", "justifyContentCenter");
    createAndAppendElement(totalAmountWrapper, "span", "tdMargin", formatNumber(contractDisplay.totalAmount, currency));
    // Amount
    const amount = createAndAppendElement(newRow, "td");
    const amountWrapper = createAndAppendElement(amount, "div", "justifyContentCenter");
    createAndAppendElement(amountWrapper, "span", "tdMargin", formatNumber(contract.amount, currency));
    // Start date
    const startDate = createAndAppendElement(newRow, "td");
    const startDateWrapper = createAndAppendElement(startDate, "div", "justifyContentCenter");
    createAndAppendElement(startDateWrapper, "span", "tdMargin", formatDateString(contract.startDate));
    // Last payment date
    const lastPaymentDate = createAndAppendElement(newRow, "td");
    const lastPaymentDateWrapper = createAndAppendElement(lastPaymentDate, "div", "justifyContentCenter");
    createAndAppendElement(lastPaymentDateWrapper, "span", "tdMargin", formatDateString(contract.lastPaymentDate));
    let current = 1;
    contractDisplay.contractHistories.forEach(contractHistory => {
        const subRow = createContractSubRow(tableBody, contractHistory, messages, currency, hiddenClass);
        addHoverToOtherElement(newRow, subRow);
        current++;
    });
}
function createContractSubRow(parent, contractHistory, messages, currency, hidden) {
    const subRow = createAndAppendElement(parent, "tr", hidden);
    // Changed at
    const changedAt = createAndAppendElement(subRow, "td", "", "", { colspan: "2" });
    const changedAtWrapper = createAndAppendElement(changedAt, "div", "justifyContentCenter");
    createAndAppendElement(changedAtWrapper, "span", "normalText marginRightBig", messages["changedAt"]);
    createAndAppendElement(changedAtWrapper, "span", "", formatDateString(contractHistory.changedAt));
    // Previous amount
    const previousAmount = createAndAppendElement(subRow, "td", "", "", { colspan: "3" });
    const previousAmountWrapper = createAndAppendElement(previousAmount, "div", "justifyContentCenter");
    createAndAppendElement(previousAmountWrapper, "span", "normalText marginRightBig", messages["previousAmount"]);
    createAndAppendElement(previousAmountWrapper, "span", "", formatNumber(contractHistory.previousAmount, currency));
    // Previous amount
    const newAmount = createAndAppendElement(subRow, "td", "", "", { colspan: "3" });
    const newAmountWrapper = createAndAppendElement(newAmount, "div", "justifyContentCenter");
    createAndAppendElement(newAmountWrapper, "span", "normalText marginRightBig", messages["newAmount"]);
    createAndAppendElement(newAmountWrapper, "span", "", formatNumber(contractHistory.newAmount, currency));
    return subRow;
}
function updateContract(contractDisplay, data) {
    const item = data.find(item => item.contract.id === contractDisplay.contract.id);
    if (item) {
        Object.assign(item, contractDisplay);
    }
}
function removeMergedContracts(contractIds, messages) {
    contractData = contractData.filter(item => !contractIds.includes(item.contract.id));
    filteredContractData = filteredContractData.filter(item => !contractIds.includes(item.contract.id));
    splitDataIntoPages(messages, Type.CONTRACT, filteredContractData);
}
function contractToListElementObjectArray(contracts) {
    let listElementObjects = [];
    const currency = getCurrentCurrencySymbol();
    contracts.forEach(contract => {
        const listElementObject = {
            id: contract.contract.id,
            text: contract.contract.name,
            toolTip: formatNumber(contract.totalAmount, currency).toString()
        };
        listElementObjects.push(listElementObject);
    });
    return listElementObjects;
}
function filterContracts(messages, searchString) {
    try {
        filteredContractData = contractData.filter(contractDisplay => {
            var _a, _b, _c, _d, _e, _f, _g, _h, _j;
            return ((_b = (_a = contractDisplay.contract) === null || _a === void 0 ? void 0 : _a.name) === null || _b === void 0 ? void 0 : _b.toLowerCase().includes(searchString)) ||
                ((_d = (_c = contractDisplay.contract) === null || _c === void 0 ? void 0 : _c.description) === null || _d === void 0 ? void 0 : _d.toLowerCase().includes(searchString)) ||
                ((_e = contractDisplay.contract) === null || _e === void 0 ? void 0 : _e.amount.toString().toLowerCase().includes(searchString)) ||
                ((_f = contractDisplay.contract) === null || _f === void 0 ? void 0 : _f.startDate.toLowerCase().includes(searchString)) ||
                ((_g = contractDisplay.contract) === null || _g === void 0 ? void 0 : _g.lastPaymentDate.toLowerCase().includes(searchString)) ||
                ((_h = contractDisplay.transactionCount) === null || _h === void 0 ? void 0 : _h.toString().toLowerCase().includes(searchString)) ||
                ((_j = contractDisplay.totalAmount) === null || _j === void 0 ? void 0 : _j.toString().toLowerCase().includes(searchString)) ||
                contractDisplay.contractHistories.some(contractHistory => contractHistory.previousAmount.toString().toLowerCase().includes(searchString) ||
                    contractHistory.newAmount.toString().toLowerCase().includes(searchString) ||
                    contractHistory.changedAt.toString().toLowerCase().includes(searchString));
        });
        splitDataIntoPages(messages, Type.CONTRACT, filteredContractData);
    }
    catch (error) {
        console.error("Unexpected error in filterCounterParties:", error);
        showAlert("ERROR", messages["error_generic"]);
    }
}
//# sourceMappingURL=contracts.js.map