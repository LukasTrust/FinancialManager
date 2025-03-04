async function buildCounterParties() {
    const messages = await fetchLocalization("counterParties");
    if (!messages)
        return;
    counterPartiesHiddenToggle = false;
    await loadCounterParties(messages);
    splitDataIntoPages(messages, "counterParties", counterParties);
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
        counterParties = await response.json();
        filteredCounterParties = counterParties;
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
            createCheckBoxForRowGroup(rowGroup, newRow, counterParty.id, counterParty.hidden);
            // Name cell
            let name = createAndAppendElement(newRow, "td", "", "", { style: "width: 25%" });
            createAndAppendElement(name, "span", "tdMargin highlightCell highlightCellTeal", counterParty.name);
            // Description Cell
            let description = createAndAppendElement(newRow, "td", "", "", { style: "width: 30%" });
            if (counterParty.description) {
                createAndAppendElement(description, "span", "tdMargin", counterParty.description);
            }
            // Contract count
            let contractCount = createAndAppendElement(newRow, "td", "rightAligned", "", { style: "width: 15%" });
            createAndAppendElement(contractCount, "span", "tdMargin", counterPartyDisplay.contractCount.toString());
            // Total amount
            let totalAmount = createAndAppendElement(newRow, "td", "rightAligned", "", { style: "width: 25%" });
            createAndAppendElement(totalAmount, "span", "tdMargin", formatNumber(counterPartyDisplay.totalAmount, currency), { style: "margin-right: 30px;" });
            // Secondary Row for Search Strings
            const searchStringRow = createAndAppendElement(rowGroup, "tr", "subRow", "", { id: counterParty.id.toString() });
            const searchStringCell = createAndAppendElement(searchStringRow, "td", "", "", { colspan: "2" });
            counterParty.counterPartySearchStrings.forEach(searchString => {
                createListElement(searchStringCell, searchString, {}, true, true);
            });
            addHoverToOtherElement(newRow, searchStringRow);
        });
    }
    catch (error) {
        console.error("Unexpected error in addRowsToCounterPartyTable:", error);
        showAlert("ERROR", messages["error_generic"]);
    }
}
//# sourceMappingURL=counterParties.js.map