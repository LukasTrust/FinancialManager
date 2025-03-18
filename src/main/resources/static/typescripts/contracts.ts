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

    document.getElementById("mergeButton")?.addEventListener("click", () => showMergeDialog(type, messages));
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

    let hidden: string = contract.hidden ? " hiddenRow" : "";

    if (contract.hidden && !counterPartiesHiddenToggle) {
        hidden += " hidden";
    }

    const rowGroup = createAndAppendElement(tableBody, "div", "rowGroup");
    animateElement(rowGroup);

    const newRow = createAndAppendElement(rowGroup, "tr", "rowWithSubRow" + hidden, "", {id: contract.id.toString()});
    createAndAppendElement(newRow, "td", contract.hidden ? "bi bi-eye-slash" : "", "", {style: "border-bottom: 1px solid rgba(255, 255, 255, 0.1); width: 20px"});

    createCheckBoxForRowGroup(rowGroup, newRow, contract.id);

    // Name cell
    const name = createAndAppendElement(newRow, "td", "", "", {style: "width: 20%; padding-right: 20px"});
    const nameInput = createInputBox(name, "bi bi-pencil-fill", "name", "text", contract.name);
    debounceInputChange(nameInput, (id, newValue, messages) =>
        updateField(id, "name", newValue, messages, Type.CONTRACT), contract.id, messages);

    // Description Cell
    const description = createAndAppendElement(newRow, "td" , "", "", {style: "width: 16%"});
    const descriptionInput = createInputBox(description, "bi bi-pencil-fill", "name", "text", contract.description);
    debounceInputChange(descriptionInput, (id, newValue, messages) =>
        updateField(id, "description", newValue, messages, Type.CONTRACT), contract.id, messages);

    // Transaction count
    const transactionCount = createAndAppendElement(newRow, "td", "rightAligned", "", {style: "width: 17%; padding-right: 20px"});
    createAndAppendElement(transactionCount, "span", "tdMargin", contractDisplay.transactionCount.toString());

    // Total amount
    const totalAmount = createAndAppendElement(newRow, "td", "rightAligned" , "", {style: "width: 10%"});
    createAndAppendElement(totalAmount, "span", "tdMargin", formatNumber(contractDisplay.totalAmount, currency));

    // Amount
    const amount = createAndAppendElement(newRow, "td", "rightAligned", "", {style: "width: 10%"});
    createAndAppendElement(amount, "span", "tdMargin", formatNumber(contract.amount, currency));

    // Start date
    const startDate = createAndAppendElement(newRow, "td", "rightAligned", "", {style: "width: 10%"});
    createAndAppendElement(startDate, "span", "tdMargin", formatDateString(contract.startDate));

    // Last payment date
    const lastPaymentDate = createAndAppendElement(newRow, "td", "rightAligned", "", {style: "width: 15%; padding-right: 10px"});
    createAndAppendElement(lastPaymentDate, "span", "tdMargin", formatDateString(contract.lastPaymentDate));

    const count = contractDisplay.contractHistories.length;
    let current = 1;
    contractDisplay.contractHistories.forEach(contractHistory => {
        createContractSubRow(rowGroup, contractHistory, messages, currency, count === current, hidden);
        current++;
    });

    if (count === 0) {
        rowGroup.style.borderBottom = "1px solid rgba(255, 255, 255, 0.1)";
    }

    addHoverToSiblings(newRow);
}

function createContractSubRow(parent: HTMLElement, contractHistory: ContractHistory, messages: Record<string, string>, currency: string, last: boolean, hidden: string): void {
    const subRow = createAndAppendElement(parent, "tr", last ? "subRow" + hidden: "middleRow" + hidden);

    // Changed at
    const changedAt = createAndAppendElement(subRow, "td", "", "", {style: "width: 30%; padding-left: 20px"});
    createAndAppendElement(changedAt, "span", "normalText", messages["changedAt"], {style: "padding-right: 30px"});
    createAndAppendElement(changedAt, "span", "", formatDateString(contractHistory.changedAt));

    // Previous amount
    const previousAmount = createAndAppendElement(subRow, "td", "", "", {style: "width: 30%"});
    createAndAppendElement(previousAmount, "span", "normalText", messages["previousAmount"], {style: "padding-right: 30px"});
    createAndAppendElement(previousAmount, "span", "", formatNumber(contractHistory.previousAmount, currency));

    // Previous amount
    const newAmount = createAndAppendElement(subRow, "td", "", "", {style: "width: 20%"});
    createAndAppendElement(newAmount, "span", "normalText", messages["newAmount"], {style: "padding-right: 30px"});
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