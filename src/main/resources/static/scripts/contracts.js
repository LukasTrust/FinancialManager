async function buildContracts() {
    const messages = await loadLocalization("contracts");
    if (!messages)
        return;
    setMonths(messages);
    const type = Type.CONTRACT;
    await loadData(type, messages);
    splitDataIntoPages(messages, type, contractData);
    setUpSorting(true);
}
async function updateContractField(contractId, field, newValue, messages) {
    try {
        const response = await fetch(`/contract/data/${contractId}/change/${field}/${newValue}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
        });
        if (!response.ok) {
            await showAlertFromResponse(response);
        }
    }
    catch (error) {
        console.error(`There was an error changing the ${field} of a contract:`, error);
        showAlert('error', messages["error_generic"]);
    }
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
    let rowGroupClass = contract.hidden ? "rowGroup hiddenRow" : "rowGroup";
    if (contract.hidden && !counterPartiesHiddenToggle) {
        rowGroupClass += " hidden";
    }
    const rowGroup = createAndAppendElement(tableBody, "tbody", rowGroupClass);
    animateElement(rowGroup);
    const newRow = createAndAppendElement(rowGroup, "tr", "rowWithSubRow", "", { id: contract.id.toString() });
    createAndAppendElement(newRow, "td", "", "", { style: "border-bottom: 1px solid rgba(255, 255, 255, 0.1); width: 20px" });
    createCheckBoxForRowGroup(rowGroup, newRow, contract.id, contract.hidden);
    // Name cell
    const name = createAndAppendElement(newRow, "td", "", "", { style: "width: 20%; padding-right: 20px" });
    const nameInput = createInputBox(name, "bi bi-pencil-fill", "name", "text", contract.name);
    debounceInputChange(nameInput, (id, newValue, messages) => updateContractField(id, "name", newValue, messages), contract.id, messages);
    // Description Cell
    const description = createAndAppendElement(newRow, "td", "", "", { style: "width: 18%" });
    const descriptionInput = createInputBox(description, "bi bi-pencil-fill", "name", "text", contract.description);
    debounceInputChange(descriptionInput, (id, newValue, messages) => updateContractField(id, "description", newValue, messages), contract.id, messages);
    // Transaction count
    const transactionCount = createAndAppendElement(newRow, "td", "rightAligned", "", { style: "width: 15%; padding-right: 20px" });
    createAndAppendElement(transactionCount, "span", "tdMargin", contractDisplay.transactionCount.toString());
    // Total amount
    const totalAmount = createAndAppendElement(newRow, "td", "rightAligned", "", { style: "width: 10%" });
    createAndAppendElement(totalAmount, "span", "tdMargin", formatNumber(contractDisplay.totalAmount, currency));
    // Amount
    const amount = createAndAppendElement(newRow, "td", "rightAligned", "", { style: "width: 10%" });
    createAndAppendElement(amount, "span", "tdMargin", formatNumber(contract.amount, currency));
    // Start date
    const startDate = createAndAppendElement(newRow, "td", "rightAligned", "", { style: "width: 10%" });
    createAndAppendElement(startDate, "span", "tdMargin", formatDateString(contract.startDate));
    // Last payment date
    const lastPaymentDate = createAndAppendElement(newRow, "td", "rightAligned", "", { style: "width: 15%; padding-right: 10px" });
    createAndAppendElement(lastPaymentDate, "span", "tdMargin", formatDateString(contract.lastPaymentDate));
}
//# sourceMappingURL=contracts.js.map