async function buildCounterParties() {
    var _a;
    const messages = await fetchLocalization("counterParties");
    if (!messages)
        return;
    counterPartiesHiddenToggle = false;
    await loadCounterParties(messages);
    splitDataIntoPages(messages, SortType.COUNTERPARTY, counterPartyData);
    setUpSorting(true);
    (_a = document.getElementById("searchBarInput")) === null || _a === void 0 ? void 0 : _a.addEventListener("input", () => searchTable(messages, SortType.COUNTERPARTY));
}
async function loadCounterParties(messages) {
    try {
        const response = await fetch(`/counterParty/data`, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        });
        if (!response.ok) {
            await showAlertFromResponse(response);
            return;
        }
        counterPartyData = await response.json();
        filteredCounterPartyData = counterPartyData;
    }
    catch (error) {
        console.error("There was an error loading the counterParties:", error);
        showAlert('error', messages["error_generic"]);
    }
}
function addRowsToCounterPartyTable(data, messages) {
    try {
        const tableBody = getCurrentTableBody();
        if (!tableBody)
            return;
        const currency = getCurrentCurrencySymbol();
        const toolTip = messages["removeSearchStringToolTip"];
        data.forEach((counterPartyDisplay, index) => {
            if (!counterPartyDisplay || typeof counterPartyDisplay !== "object") {
                console.warn(`Warning: Skipping invalid counterParty at index ${index}.`, counterPartyDisplay);
                return;
            }
            const counterParty = counterPartyDisplay.counterParty;
            let rowClass = counterParty.hidden ? "rowWithSubRow hiddenRow" : "rowWithSubRow";
            if (counterParty.hidden && !counterPartiesHiddenToggle) {
                rowClass += " hidden";
            }
            const rowGroup = createAndAppendElement(tableBody, "div", "rowGroup");
            const newRow = createAndAppendElement(rowGroup, "tr", rowClass, "", { id: counterParty.id.toString() });
            createAndAppendElement(newRow, "td", "", "", { style: "border-bottom: 1px solid rgba(255, 255, 255, 0.1); width: 20px" });
            createCheckBoxForRowGroup(rowGroup, newRow, counterParty.id, counterParty.hidden);
            // Name cell
            const name = createAndAppendElement(newRow, "td", "", "", { style: "width: 15%" });
            const nameInput = createInputBox(name, "bi bi-pencil-fill", "name", "text", counterParty.name);
            debounceInputChange(nameInput, (id, newValue, messages) => updateCounterPartyField(id, "name", newValue, messages), counterParty.id, messages);
            // Transaction count
            const transactionCount = createAndAppendElement(newRow, "td", "rightAligned", "", { style: "width: 20%" });
            createAndAppendElement(transactionCount, "span", "tdMargin", counterPartyDisplay.transactionCount.toString());
            // Contract count
            const contractCount = createAndAppendElement(newRow, "td", "rightAligned", "", { style: "width: 15%" });
            createAndAppendElement(contractCount, "span", "tdMargin", counterPartyDisplay.contractCount.toString());
            // Total amount
            const totalAmount = createAndAppendElement(newRow, "td", "rightAligned", "", { style: "width: 15%" });
            createAndAppendElement(totalAmount, "span", "tdMargin", formatNumber(counterPartyDisplay.totalAmount, currency), { style: "margin-right: 30px;" });
            // Description Cell
            const description = createAndAppendElement(newRow, "td", "", "", { style: "width: 30%; padding-right: 20px" });
            const descriptionInput = createInputBox(description, "bi bi-pencil-fill", "name", "text", counterParty.description);
            debounceInputChange(descriptionInput, (id, newValue, messages) => updateCounterPartyField(id, "description", newValue, messages), counterParty.id, messages);
            // Secondary Row for Search Strings
            const searchStringRow = createAndAppendElement(rowGroup, "tr", "subRow", "");
            const counterpartySearchStrings = createAndAppendElement(searchStringRow, "td", "", "", { style: "width: 20%" });
            createAndAppendElement(counterpartySearchStrings, "h3", "", messages["counterpartySearchStrings"]);
            const searchStringCell = createAndAppendElement(searchStringRow, "td", "", "", { style: "width: 80%" });
            const listContainer = createAndAppendElement(searchStringCell, "div", "listContainer", "", { style: "justify-content: normal; overflow: visible;" });
            counterParty.counterPartySearchStrings.forEach(searchString => {
                createListElement(listContainer, searchString, {}, true, true, toolTip, (element) => removeSearchStringFromCounterParty(counterParty.id, searchString, element, messages));
            });
            addHoverToOtherElement(newRow, searchStringRow);
        });
    }
    catch (error) {
        console.error("Unexpected error in addRowsToCounterPartyTable:", error);
        showAlert("ERROR", messages["error_generic"]);
    }
}
async function updateCounterPartyField(counterPartyId, field, newValue, messages) {
    try {
        const response = await fetch(`/counterParty/data/${counterPartyId}/change/${field}/${newValue}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
        });
        if (!response.ok) {
            await showAlertFromResponse(response);
            return;
        }
    }
    catch (error) {
        console.error(`There was an error changing the ${field} of a counterParty:`, error);
        showAlert('error', messages["error_generic"]);
    }
}
async function removeSearchStringFromCounterParty(counterPartyId, searchString, elementToRemove, messages) {
    try {
        const params = new URLSearchParams();
        params.append("searchString", searchString);
        const url = `/counterParty/data/${counterPartyId}/removeSearchString?${params.toString()}`;
        const response = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
        });
        if (!response.ok) {
            await showAlertFromResponse(response);
            return;
        }
        elementToRemove.parentElement.removeChild(elementToRemove);
    }
    catch (error) {
        console.error("There was an error removing the search string form the counterParty:", error);
        showAlert('error', messages["error_generic"]);
    }
}
function filterCounterParties(messages, searchString) {
    try {
        console.log(searchString);
        filteredCounterPartyData = counterPartyData.filter(counterPartyDisplay => {
            var _a, _b, _c, _d, _e, _f;
            return ((_b = (_a = counterPartyDisplay.counterParty) === null || _a === void 0 ? void 0 : _a.name) === null || _b === void 0 ? void 0 : _b.toLowerCase().includes(searchString)) ||
                ((_c = counterPartyDisplay.transactionCount) === null || _c === void 0 ? void 0 : _c.toString().toLowerCase().includes(searchString)) ||
                ((_d = counterPartyDisplay.contractCount) === null || _d === void 0 ? void 0 : _d.toString().toLowerCase().includes(searchString)) ||
                ((_e = counterPartyDisplay.totalAmount) === null || _e === void 0 ? void 0 : _e.toString().toLowerCase().includes(searchString)) ||
                ((_f = counterPartyDisplay.counterParty.description) === null || _f === void 0 ? void 0 : _f.toString().toLowerCase().includes(searchString));
        });
        console.log(filteredCounterPartyData);
        splitDataIntoPages(messages, SortType.COUNTERPARTY, filteredCounterPartyData);
    }
    catch (error) {
        console.error("Unexpected error in filterCounterParties:", error);
        showAlert("ERROR", messages["error_generic"]);
    }
}
//# sourceMappingURL=counterParties.js.map