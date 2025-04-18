async function buildContracts(): Promise<void> {
    const messages = await loadLocalization("contracts");
    if (!messages) return;

    setMonths(messages);

    const type = Type.CONTRACT;

    await loadData(type, messages);
    splitDataIntoPages(messages, type, contractData);

    setUpSorting(true);

    document.getElementById("showHiddenRows")?.addEventListener("change", () => changeRowVisibility(type));

    document.getElementById("changeHiddenButton")?.addEventListener("click", () => showChangeHiddenDialog(type, messages));

    document.getElementById("mergeButton")?.addEventListener("click", async () =>
        await buildMergeContracts("/contracts", getCheckedData(Type.CONTRACT).map(c => c.contract)));

    document.getElementById("deleteButton")?.addEventListener("click", () => deleteContractsDialogs(messages));
}

async function deleteContracts(messages: Record<string, string>): Promise<void> {
    try {
        const contracts = getCheckedData(Type.CONTRACT) as ContractDisplay[];

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

        const responseBody: Response = await response.json();

        if (responseBody.alertType === AlertType.SUCCESS) {
            const idSet = new Set(ids);

            contractData = contractData.filter(contract => !idSet.has(contract.contract.id));
            filteredContractData = filteredContractData.filter(contract => !idSet.has(contract.contract.id));
            splitDataIntoPages(messages, Type.CONTRACT, filteredContractData);
        }
    } catch (error) {
        console.error("There was an error deleting the contracts", error);
        showAlert('error', messages["error_generic"]);
    }
}

function deleteContractsDialogs(messages: Record<string, string>): void {
    const selectedContracts = getCheckedData(Type.CONTRACT) as ContractDisplay[];

    if (!selectedContracts || selectedContracts.length === 0) {
        return;
    }

    showMessageBox(messages["deleteButton"], "bi bi-trash-fill", messages["deleteText"], messages["yes"], "bi bi-trash-fill", messages["no"],
        "bi bi-x-circle-fill", (async () => await deleteContracts(messages)), (() => closeDialog()),
        messages["yesTooltip"], messages["noTooltip"]);
}

function addRowsToContractTable(data: ContractDisplay[], messages: Record<string, string>): void {
    try {
        const tableBody = getCurrentTableBody();
        if (!tableBody) return;
        const currency = getCurrentCurrencySymbol();

        data.forEach(contractDisplay => {
            createContractRow(tableBody, contractDisplay, currency, messages);
        });
    } catch (error) {
        console.error("Unexpected error in addRowsToCounterPartyTable:", error);
        showAlert("ERROR", messages["error_generic"]);
    }
}

function createContractRow(tableBody: HTMLElement, contractDisplay: ContractDisplay,
                               currency: string, messages: Record<string, string>) {
    if (!contractDisplay || typeof contractDisplay !== "object") {
        console.warn(`Warning: Skipping invalid counterParty:.`, contractDisplay);
        return;
    }

    const contract = contractDisplay.contract;

    let hiddenClass: string = contract.hidden ? " hiddenRow" : "";

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
    debounceInputChange(nameInput, (id, newValue, messages) =>
        updateField(id, "name", newValue, messages, Type.CONTRACT), contract.id, messages);

    // Description Cell
    const description = createAndAppendElement(newRow, "td");
    const descriptionInput = createInputBox(description, "bi bi-pencil-fill", "name", "text", contract.description);
    debounceInputChange(descriptionInput, (id, newValue, messages) =>
        updateField(id, "description", newValue, messages, Type.CONTRACT), contract.id, messages);

    // Transaction count
    const transactionCount = createAndAppendElement(newRow, "td");
    createAndAppendElement(transactionCount, "span", "tdMargin", contractDisplay.transactionCount.toString());

    // Total amount
    const totalAmount = createAndAppendElement(newRow, "td");
    createAndAppendElement(totalAmount, "span", "tdMargin", formatNumber(contractDisplay.totalAmount, currency));

    // Amount
    const amount = createAndAppendElement(newRow, "td");
    createAndAppendElement(amount, "span", "tdMargin", formatNumber(contract.amount, currency));

    // Start date
    const startDate = createAndAppendElement(newRow, "td");
    createAndAppendElement(startDate, "span", "tdMargin", formatDateString(contract.startDate));

    // Last payment date
    const lastPaymentDate = createAndAppendElement(newRow, "td");
    createAndAppendElement(lastPaymentDate, "span", "tdMargin", formatDateString(contract.lastPaymentDate));

    let current = 1;
    contractDisplay.contractHistories.forEach(contractHistory => {
        createContractSubRow(tableBody, contractHistory, messages, currency, hiddenClass);
        current++;
    });

    addHoverToSiblings(newRow);
}

function createContractSubRow(parent: HTMLElement, contractHistory: ContractHistory, messages: Record<string, string>, currency: string, hidden: string): void {
    const subRow = createAndAppendElement(parent, "tr", hidden);

    // Changed at
    const changedAt = createAndAppendElement(subRow, "td");
    createAndAppendElement(changedAt, "span", "normalText", messages["changedAt"]);
    createAndAppendElement(changedAt, "span", "", formatDateString(contractHistory.changedAt));

    // Previous amount
    const previousAmount = createAndAppendElement(subRow, "td");
    createAndAppendElement(previousAmount, "span", "normalText", messages["previousAmount"]);
    createAndAppendElement(previousAmount, "span", "", formatNumber(contractHistory.previousAmount, currency));

    // Previous amount
    const newAmount = createAndAppendElement(subRow, "td");
    createAndAppendElement(newAmount, "span", "normalText", messages["newAmount"]);
    createAndAppendElement(newAmount, "span", "", formatNumber(contractHistory.newAmount, currency));
}

function updateContract(contractDisplay: ContractDisplay, data: ContractDisplay[]): void {
    const item = data.find(item => item.contract.id === contractDisplay.contract.id);
    if (item) {
        Object.assign(item, contractDisplay);
    }
}

function removeMergedContracts(contractIds: number[], messages: Record<string, string>): void {
    contractData = contractData.filter(item => !contractIds.includes(item.contract.id));
    filteredContractData = filteredContractData.filter(item => !contractIds.includes(item.contract.id));

    splitDataIntoPages(messages, Type.CONTRACT, filteredContractData);
}

function contractToListElementObjectArray(contracts: ContractDisplay[]): ListElementObject[] {
    let listElementObjects: ListElementObject[] = [];

    const currency = getCurrentCurrencySymbol();

    contracts.forEach(contract => {
        const listElementObject: ListElementObject = {
            id: contract.contract.id,
            text: contract.contract.name,
            toolTip: formatNumber(contract.totalAmount, currency).toString()
        };

        listElementObjects.push(listElementObject);
    });

    return listElementObjects;
}