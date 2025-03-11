async function buildCounterParties() {
    var _a, _b, _c, _d;
    const messages = await fetchLocalization("counterParties");
    if (!messages)
        return;
    counterPartiesHiddenToggle = false;
    await loadCounterParties(messages);
    splitDataIntoPages(messages, Type.COUNTERPARTY, counterPartyData);
    setUpSorting(true);
    (_a = document.getElementById("changeHiddenButton")) === null || _a === void 0 ? void 0 : _a.addEventListener("click", () => showChangeHiddenDialog(Type.COUNTERPARTY, messages));
    (_b = document.getElementById("mergeCounterParties")) === null || _b === void 0 ? void 0 : _b.addEventListener("click", () => showMergeDialog(Type.COUNTERPARTY, messages));
    (_c = document.getElementById("searchBarInput")) === null || _c === void 0 ? void 0 : _c.addEventListener("input", () => searchTable(messages, Type.COUNTERPARTY));
    (_d = document.getElementById("showHiddenRows")) === null || _d === void 0 ? void 0 : _d.addEventListener("change", () => changeRowVisibility(Type.COUNTERPARTY));
}
async function mergeCounterParties(model, messages, leftSide, updatedContainer) {
    try {
        const headerId = getCounterPartyIds(leftSide)[0];
        const counterPartyIds = getCounterPartyIds(updatedContainer);
        if (counterPartyIds.length === 0) {
            showAlert("INFO", messages["noCounterPartyToUpdate"], model);
            return;
        }
        const response = await fetch(`/counterParty/data/mergeCounterParties/${headerId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(counterPartyIds),
        });
        const responseBody = await response.json();
        showAlert(responseBody.alertType, responseBody.message, model);
        updateCounterParty(responseBody.data, counterPartyData);
        updateCounterParty(responseBody.data, filteredCounterPartyData);
        removeElements(updatedContainer);
        removeMergedCounterParties(counterPartyIds, messages);
    }
    catch (error) {
        console.error("There was an error merging the counterParties:", error);
        showAlert('error', messages["error_generic"], model);
    }
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
async function addSearchString(counterPartyId, newSearchString, messages, parent, toolTip) {
    try {
        const params = new URLSearchParams();
        params.append("searchString", newSearchString);
        const url = `/counterParty/data/${counterPartyId}/addSearchString?${params.toString()}`;
        const response = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
        });
        await showAlertFromResponse(response);
        if (response.ok) {
            createListElement(parent, newSearchString, {}, true, true, toolTip, () => removeSearchStringFromCounterParty(counterPartyId, newSearchString, messages), true);
        }
    }
    catch (error) {
        console.error(`There was an error adding a searchString to the counterParty:`, error);
        showAlert('error', messages["error_generic"]);
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
        }
    }
    catch (error) {
        console.error(`There was an error changing the ${field} of a counterParty:`, error);
        showAlert('error', messages["error_generic"]);
    }
}
async function updateCounterPartyVisibility(messages, model, updatedContainer, moveToContainer, hide) {
    try {
        // Get all counterParty IDs
        const counterPartyIds = getCounterPartyIds(updatedContainer);
        if (counterPartyIds.length === 0) {
            showAlert("INFO", messages["noCounterPartyToUpdate"], model);
            return;
        }
        const endpoint = hide ? "hideCounterParties" : "unHideCounterParties";
        const response = await fetch(`/counterParty/data/${endpoint}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(counterPartyIds),
        });
        const responseBody = await response.json();
        showAlert(responseBody.alertType, responseBody.message, model);
        if (responseBody.alertType === AlertType.SUCCESS) {
            // Animate and move elements
            moveElements(updatedContainer, moveToContainer);
            updateCachedDataAndUI(Type.COUNTERPARTY, messages, counterPartyIds);
        }
    }
    catch (error) {
        console.error("Unexpected error in updateCounterPartyVisibility:", error);
        showAlert("ERROR", messages["error_generic"], model);
    }
}
async function removeSearchStringFromCounterParty(counterPartyId, searchString, messages) {
    try {
        const params = new URLSearchParams();
        params.append("searchString", searchString);
        const url = `/counterParty/data/${counterPartyId}/removeSearchString?${params.toString()}`;
        const response = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
        });
        const responseBody = await response.json();
        showAlert(responseBody.alertType, responseBody.message);
        if (responseBody.alertType === AlertType.SUCCESS) {
            const updatedCounterParty = responseBody.data[0];
            if (responseBody.data.length == 2) {
                const createdCounterParty = responseBody.data[1];
                counterPartyData.push(createdCounterParty);
                filteredCounterPartyData.push(createdCounterParty);
                showAlert(AlertType.INFO, messages["counterPartyWasSplit"]);
            }
            else {
                showAlert(AlertType.INFO, messages["counterPartyWasUpdated"]);
            }
            updateCounterParty(updatedCounterParty, counterPartyData);
            updateCounterParty(updatedCounterParty, filteredCounterPartyData);
            splitDataIntoPages(messages, Type.COUNTERPARTY, filteredCounterPartyData);
        }
    }
    catch (error) {
        console.error("There was an error removing the search string form the counterParty:", error);
        showAlert('error', messages["error_generic"]);
    }
}
function filterCounterParties(messages, searchString) {
    try {
        filteredCounterPartyData = counterPartyData.filter(counterPartyDisplay => {
            var _a, _b, _c, _d, _e, _f;
            return ((_b = (_a = counterPartyDisplay.counterParty) === null || _a === void 0 ? void 0 : _a.name) === null || _b === void 0 ? void 0 : _b.toLowerCase().includes(searchString)) ||
                ((_c = counterPartyDisplay.transactionCount) === null || _c === void 0 ? void 0 : _c.toString().toLowerCase().includes(searchString)) ||
                ((_d = counterPartyDisplay.contractCount) === null || _d === void 0 ? void 0 : _d.toString().toLowerCase().includes(searchString)) ||
                ((_e = counterPartyDisplay.totalAmount) === null || _e === void 0 ? void 0 : _e.toString().toLowerCase().includes(searchString)) ||
                ((_f = counterPartyDisplay.counterParty.description) === null || _f === void 0 ? void 0 : _f.toString().toLowerCase().includes(searchString));
        });
        splitDataIntoPages(messages, Type.COUNTERPARTY, filteredCounterPartyData);
    }
    catch (error) {
        console.error("Unexpected error in filterCounterParties:", error);
        showAlert("ERROR", messages["error_generic"]);
    }
}
function counterPartyToListElementObjectArray(counterParties) {
    let listElementObjects = [];
    const currency = getCurrentCurrencySymbol();
    counterParties.forEach(counterParty => {
        const listElementObject = {
            id: counterParty.counterParty.id,
            text: counterParty.counterParty.name,
            toolTip: formatNumber(counterParty.totalAmount, currency).toString()
        };
        listElementObjects.push(listElementObject);
    });
    return listElementObjects;
}
function addRowsToCounterPartyTable(data, messages) {
    try {
        const tableBody = getCurrentTableBody();
        if (!tableBody)
            return;
        const currency = getCurrentCurrencySymbol();
        const toolTip = messages["removeSearchStringToolTip"];
        data.forEach(counterPartyDisplay => {
            createCounterPartyRow(tableBody, counterPartyDisplay, currency, toolTip, messages);
        });
    }
    catch (error) {
        console.error("Unexpected error in addRowsToCounterPartyTable:", error);
        showAlert("ERROR", messages["error_generic"]);
    }
}
function createCounterPartyRow(tableBody, counterPartyDisplay, currency, toolTip, messages) {
    if (!counterPartyDisplay || typeof counterPartyDisplay !== "object") {
        console.warn(`Warning: Skipping invalid counterParty:.`, counterPartyDisplay);
        return;
    }
    const counterParty = counterPartyDisplay.counterParty;
    let rowGroupClass = counterParty.hidden ? "rowGroup hiddenRow" : "rowGroup";
    if (counterParty.hidden && !counterPartiesHiddenToggle) {
        rowGroupClass += " hidden";
    }
    const rowGroup = createAndAppendElement(tableBody, "div", rowGroupClass);
    animateElement(rowGroup);
    const newRow = createAndAppendElement(rowGroup, "tr", "rowWithSubRow", "", { id: counterParty.id.toString() });
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
    const searchString = createAndAppendElement(searchStringRow, "td", "", "", { style: "width: 20%" });
    const searchStringContainer = createAndAppendElement(searchString, "div", "flexContainer");
    const searchStringInput = createInputBox(searchStringContainer, "", "name", "text", "", messages["addSearchStringPlaceHolder"]);
    const searchStringButton = createAndAppendElement(searchStringContainer, "button", "iconOnlyButton bi bi bi-plus-circle");
    searchStringButton.addEventListener("click", async () => {
        await addSearchString(counterParty.id, searchStringInput.value, messages, listContainer, toolTip);
        searchStringInput.value = "";
    });
    const searchStringCell = createAndAppendElement(searchStringRow, "td", "", "", { style: "width: 60%" });
    const listContainer = createAndAppendElement(searchStringCell, "div", "listContainer", "", { style: "justify-content: normal; overflow: visible;" });
    counterParty.counterPartySearchStrings.forEach(searchString => {
        createListElement(listContainer, searchString, {}, true, true, toolTip, () => removeSearchStringFromCounterParty(counterParty.id, searchString, messages));
    });
    addHoverToOtherElement(newRow, searchStringRow);
}
function getCounterPartyIds(updatedContainer) {
    return Array.from(updatedContainer.querySelectorAll(".normalText"))
        .map(span => Number(span.id))
        .filter(id => !isNaN(id) && id !== 0);
}
function updateCounterParty(counterPartyDisplay, data) {
    const item = data.find(item => item.counterParty.id === counterPartyDisplay.counterParty.id);
    if (item) {
        Object.assign(item, counterPartyDisplay);
    }
}
function removeMergedCounterParties(counterPartyIds, messages) {
    counterPartyData = counterPartyData.filter(item => !counterPartyIds.includes(item.counterParty.id));
    filteredCounterPartyData = filteredCounterPartyData.filter(item => !counterPartyIds.includes(item.counterParty.id));
    splitDataIntoPages(messages, Type.COUNTERPARTY, filteredCounterPartyData);
}
//# sourceMappingURL=counterParties.js.map