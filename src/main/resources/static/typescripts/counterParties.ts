async function buildCounterParties(): Promise<void> {
    const messages = await loadLocalization("counterParties");
    if (!messages) return;

    const type = Type.COUNTERPARTY;

    await loadData(type, messages);
    splitDataIntoPages(messages, type, counterPartyData);

    setUpSorting(true);

    document.getElementById("changeHiddenButton")?.addEventListener("click", () => showChangeHiddenDialog(type, messages));

    document.getElementById("mergeButton")?.addEventListener("click", () => showMergeDialog(type, messages));

    document.getElementById("searchBarInput")?.addEventListener("input", () => searchTable(messages, type));

    document.getElementById("showHiddenRows")?.addEventListener("change", () => changeRowVisibility(type));
}

async function addSearchString(
    counterPartyId: number,
    newSearchString: string,
    messages: Record<string, string>,
    parent: HTMLElement,
    toolTip: string
): Promise<void> {
    try {
        const params = new URLSearchParams();
        params.append("searchString", newSearchString);
        const url = `/counterParties/data/${counterPartyId}/addSearchString?${params.toString()}`;

        const response = await fetch(url, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
        });

        await showAlertFromResponse(response);

        if (response.ok) {
            createListElement(
                parent,
                newSearchString,
                {},
                true,
                true,
                toolTip,
                () => removeSearchStringFromCounterParty(counterPartyId, newSearchString, messages),
                true
            );
        }
    } catch (error) {
        console.error(`There was an error adding a searchString to the counterParty:`, error);
        showAlert('error', messages["error_generic"]);
    }
}

async function removeSearchStringFromCounterParty(counterPartyId: number, searchString: string, messages: Record<string, string>): Promise<void> {
    try {
        const params = new URLSearchParams();
        params.append("searchString", searchString);
        const url = `/counterParties/data/${counterPartyId}/removeSearchString?${params.toString()}`;

        const response = await fetch(url, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
        });

        const responseBody: Response = await response.json();

        showAlert(responseBody.alertType, responseBody.message);
        if (responseBody.alertType === AlertType.SUCCESS) {
            const updatedCounterParty: CounterPartyDisplay = responseBody.data[0];

            if (responseBody.data.length == 2) {
                const createdCounterParty: CounterPartyDisplay = responseBody.data[1];
                counterPartyData.push(createdCounterParty);
                filteredCounterPartyData.push(createdCounterParty);

                showAlert(AlertType.INFO, messages["counterPartyWasSplit"])
            } else {
                showAlert(AlertType.INFO, messages["counterPartyWasUpdated"])
            }

            updateCounterParty(updatedCounterParty, counterPartyData);
            updateCounterParty(updatedCounterParty, filteredCounterPartyData);

            splitDataIntoPages(messages, Type.COUNTERPARTY, filteredCounterPartyData);
        }
    } catch (error) {
        console.error("There was an error removing the search string form the counterParty:", error);
        showAlert('error', messages["error_generic"]);
    }
}

function filterCounterParties(messages: Record<string, string>, searchString: string): void {
    try {
        filteredCounterPartyData = counterPartyData.filter(counterPartyDisplay =>
            counterPartyDisplay.counterParty?.name?.toLowerCase().includes(searchString) ||
            counterPartyDisplay.transactionCount?.toString().toLowerCase().includes(searchString) ||
            counterPartyDisplay.contractCount?.toString().toLowerCase().includes(searchString) ||
            counterPartyDisplay.totalAmount?.toString().toLowerCase().includes(searchString) ||
            counterPartyDisplay.counterParty.description?.toString().toLowerCase().includes(searchString)
        );

        splitDataIntoPages(messages, Type.COUNTERPARTY, filteredCounterPartyData);
    } catch (error) {
        console.error("Unexpected error in filterCounterParties:", error);
        showAlert("ERROR", messages["error_generic"]);
    }
}

function counterPartyToListElementObjectArray(counterParties: CounterPartyDisplay[]): ListElementObject[] {
    let listElementObjects: ListElementObject[] = [];

    const currency = getCurrentCurrencySymbol();

    counterParties.forEach(counterParty => {
        const listElementObject: ListElementObject = {
            id: counterParty.counterParty.id,
            text: counterParty.counterParty.name,
            toolTip: formatNumber(counterParty.totalAmount, currency).toString()
        };

        listElementObjects.push(listElementObject);
    });

    return listElementObjects;
}

function addRowsToCounterPartyTable(data: CounterPartyDisplay[], messages: Record<string, string>): void {
    try {
        const tableBody = getCurrentTableBody();
        if (!tableBody) return;
        const currency = getCurrentCurrencySymbol();
        const toolTip = messages["removeSearchStringToolTip"];

        data.forEach(counterPartyDisplay => {
            createCounterPartyRow(tableBody, counterPartyDisplay, currency, toolTip, messages);
        });
    } catch (error) {
        console.error("Unexpected error in addRowsToCounterPartyTable:", error);
        showAlert("ERROR", messages["error_generic"]);
    }
}

function createCounterPartyRow(tableBody: HTMLElement, counterPartyDisplay: CounterPartyDisplay,
                               currency: string, toolTip: string, messages: Record<string, string>) {
    if (!counterPartyDisplay || typeof counterPartyDisplay !== "object") {
        console.warn(`Warning: Skipping invalid counterParty:`, counterPartyDisplay);
        return;
    }

    const counterParty = counterPartyDisplay.counterParty;

    let hiddenClass: string = counterParty.hidden ? " hiddenRow" : "";

    if (counterParty.hidden && !counterPartiesHiddenToggle) {
        hiddenClass += " hidden";
    }

    // Main row
    const newRow = createAndAppendElement(tableBody, "tr", "mainRow" + hiddenClass, "", {
        id: counterParty.id.toString(),
        "data-sort-key": counterParty.id.toString()
    });

    // Sub Row for Search Strings
    const searchStringRow = createAndAppendElement(tableBody, "tr", "" + hiddenClass, "", {
        "data-pair": counterParty.id.toString(),
    });

    createCheckBoxForTable(newRow, searchStringRow, counterParty.id, counterParty.hidden);

    // Name cell
    const name = createAndAppendElement(newRow, "td");
    const nameInput = createInputBox(name, "bi bi-pencil-fill", "name", "text", counterParty.name);
    debounceInputChange(nameInput, (id, newValue, messages) =>
        updateField(id, "name", newValue, messages, Type.COUNTERPARTY), counterParty.id, messages);

    // Transaction count
    const transactionCount = createAndAppendElement(newRow, "td");
    const transactionCountWrapper = createAndAppendElement(transactionCount, "div", "justifyContentCenter");
    createAndAppendElement(transactionCountWrapper, "span", "", counterPartyDisplay.transactionCount.toString());

    // Contract count
    const contractCount = createAndAppendElement(newRow, "td");
    const contractCountWrapper = createAndAppendElement(contractCount, "div", "justifyContentCenter");
    createAndAppendElement(contractCountWrapper, "span", "", counterPartyDisplay.contractCount.toString());

    // Total amount
    const totalAmount = createAndAppendElement(newRow, "td");
    const totalAmountWrapper = createAndAppendElement(totalAmount, "div", "justifyContentCenter");
    createAndAppendElement(totalAmountWrapper, "span", "", formatNumber(counterPartyDisplay.totalAmount, currency));

    // Description Cell
    const description = createAndAppendElement(newRow, "td");
    const descriptionInput = createInputBox(description, "bi bi-pencil-fill", "name", "text", counterParty.description);
    debounceInputChange(descriptionInput, (id, newValue, messages) =>
        updateField(id, "description", newValue, messages, Type.COUNTERPARTY), counterParty.id, messages);

    const searchStringCell = createAndAppendElement(searchStringRow, "td", "", "", {
        colspan: "6"
    });

    const cellWrapper = createAndAppendElement(searchStringCell, "div", "marginLeftBig alignItemsCenter horizontalContainer");

    // Add search strings header text
    createAndAppendElement(cellWrapper, "h3", "", messages["counterpartySearchStrings"]);

    // Search string input and button
    const searchStringContainer = createAndAppendElement(cellWrapper, "div", "horizontalContainer");
    const searchStringButton = createAndAppendElement(searchStringContainer, "button", "iconOnlyButton bi bi-plus-circle");
    const searchStringInput = createInputBox(searchStringContainer, "", "name", "text", "", messages["addSearchStringPlaceHolder"]);
    searchStringInput.classList.add("width300");

    searchStringButton.addEventListener("click", async () => {
        await addSearchString(counterParty.id, searchStringInput.value, messages, listContainer, toolTip);
        searchStringInput.value = "";
    });

    // List of counterparty search strings
    const listContainer = createAndAppendElement(cellWrapper, "div", "widthFull listContainer");

    counterParty.counterPartySearchStrings.forEach(searchString => {
        createListElement(
            listContainer,
            searchString,
            {},
            true,
            true,
            toolTip,
            () => removeSearchStringFromCounterParty(counterParty.id, searchString, messages)
        );
    });

    // Handle hover effect for the main row and sub-row
    addHoverToOtherElement(newRow, searchStringRow);
}

function updateCounterParty(counterPartyDisplay: CounterPartyDisplay, data: CounterPartyDisplay[]): void {
    const item = data.find(item => item.counterParty.id === counterPartyDisplay.counterParty.id);
    if (item) {
        Object.assign(item, counterPartyDisplay);
    }
}

function removeMergedCounterParties(counterPartyIds: number[], messages: Record<string, string>): void {
    counterPartyData = counterPartyData.filter(item => !counterPartyIds.includes(item.counterParty.id));
    filteredCounterPartyData = filteredCounterPartyData.filter(item => !counterPartyIds.includes(item.counterParty.id));

    splitDataIntoPages(messages, Type.COUNTERPARTY, filteredCounterPartyData);
}