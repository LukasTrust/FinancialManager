async function buildCounterParties(): Promise<void> {
    const messages = await fetchLocalization("counterParties");
    if (!messages) return;

    counterPartiesHiddenToggle = false;

    await loadCounterParties(messages);
    splitDataIntoPages(messages, SortType.COUNTERPARTY, counterPartyData);

    setUpSorting(true);

    document.getElementById("searchBarInput")?.addEventListener("input", () => searchTable(messages, SortType.COUNTERPARTY));
}

async function loadCounterParties(messages: Record<string, string>): Promise<void> {
    try {
        const response = await fetch(`/counterParty/data`, {
            method: 'GET',
            headers: {'Content-Type': 'application/json'}
        });

        if (!response.ok) {
            await showAlertFromResponse(response);
            return;
        }

        counterPartyData = await response.json();
        filteredCounterPartyData = counterPartyData;
    } catch (error) {
        console.error("There was an error loading the counterParties:", error);
        showAlert('error', messages["error_generic"]);
    }
}

function addRowsToCounterPartyTable(data: CounterPartyDisplay[], messages: Record<string, string>): void {
    try {
        const tableBody = getCurrentTableBody();
        if (!tableBody) return;
        const currency = getCurrentCurrencySymbol();
        const toolTip = messages["removeSearchStringToolTip"];

        data.forEach((counterPartyDisplay, index) => {
            if (!counterPartyDisplay || typeof counterPartyDisplay !== "object") {
                console.warn(`Warning: Skipping invalid counterParty at index ${index}.`, counterPartyDisplay);
                return;
            }

            const counterParty = counterPartyDisplay.counterParty;

            let rowClass: string = counterParty.hidden ? "rowWithSubRow hiddenRow" : "rowWithSubRow";

            if (counterParty.hidden && !counterPartiesHiddenToggle) {
                rowClass += " hidden";
            }

            const rowGroup = createAndAppendElement(tableBody, "div", "rowGroup");

            const newRow = createAndAppendElement(rowGroup, "tr", rowClass, "", {id: counterParty.id.toString()});
            createAndAppendElement(newRow, "td", "", "", {style: "border-bottom: 1px solid rgba(255, 255, 255, 0.1); width: 20px"});

            createCheckBoxForRowGroup(rowGroup, newRow, counterParty.id, counterParty.hidden);

            // Name cell
            const name = createAndAppendElement(newRow, "td", "", "", {style: "width: 15%"});
            const nameInput = createInputBox(name, "bi bi-pencil-fill", "name", "text", counterParty.name);
            debounceInputChange(nameInput, (id, newValue, messages) =>
                updateCounterPartyField(id, "name", newValue, messages), counterParty.id, messages);

            // Transaction count
            const transactionCount = createAndAppendElement(newRow, "td", "rightAligned", "", {style: "width: 20%"});
            createAndAppendElement(transactionCount, "span", "tdMargin", counterPartyDisplay.transactionCount.toString());

            // Contract count
            const contractCount = createAndAppendElement(newRow, "td", "rightAligned", "", {style: "width: 15%"});
            createAndAppendElement(contractCount, "span", "tdMargin", counterPartyDisplay.contractCount.toString());

            // Total amount
            const totalAmount = createAndAppendElement(newRow, "td", "rightAligned", "", {style: "width: 15%"});
            createAndAppendElement(totalAmount, "span", "tdMargin", formatNumber(counterPartyDisplay.totalAmount, currency),
                {style: "margin-right: 30px;"});

            // Description Cell
            const description = createAndAppendElement(newRow, "td", "", "",
                {style: "width: 30%; padding-right: 20px"});
            const descriptionInput = createInputBox(description, "bi bi-pencil-fill", "name", "text", counterParty.description);
            debounceInputChange(descriptionInput, (id, newValue, messages) =>
                updateCounterPartyField(id, "description", newValue, messages), counterParty.id, messages);

            // Secondary Row for Search Strings
            const searchStringRow = createAndAppendElement(rowGroup, "tr", "subRow", "");

            const counterpartySearchStrings = createAndAppendElement(searchStringRow, "td", "", "", {style: "width: 20%"});
            createAndAppendElement(counterpartySearchStrings, "h3", "", messages["counterpartySearchStrings"]);

            const searchStringCell = createAndAppendElement(searchStringRow, "td", "", "", {style: "width: 80%"});
            const listContainer = createAndAppendElement(searchStringCell, "div", "listContainer", "", {style: "justify-content: normal; overflow: visible;"});

            counterParty.counterPartySearchStrings.forEach(searchString => {
                createListElement(
                    listContainer,
                    searchString,
                    {},
                    true,
                    true,
                    toolTip,
                    (element) => removeSearchStringFromCounterParty(counterParty.id, searchString, element, messages)
                );
            });

            addHoverToOtherElement(newRow, searchStringRow);
        });
    } catch (error) {
        console.error("Unexpected error in addRowsToCounterPartyTable:", error);
        showAlert("ERROR", messages["error_generic"]);
    }
}

async function updateCounterPartyField(
    counterPartyId: number,
    field: "name" | "description",
    newValue: string,
    messages: Record<string, string>
): Promise<void> {
    try {
        const response = await fetch(`/counterParty/data/${counterPartyId}/change/${field}/${newValue}`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
        });

        if (!response.ok) {
            await showAlertFromResponse(response);
            return;
        }
    } catch (error) {
        console.error(`There was an error changing the ${field} of a counterParty:`, error);
        showAlert('error', messages["error_generic"]);
    }
}

async function removeSearchStringFromCounterParty(counterPartyId: number, searchString: string, elementToRemove: HTMLElement, messages: Record<string, string>): Promise<void> {
    try {
        const params = new URLSearchParams();
        params.append("searchString", searchString);
        const url = `/counterParty/data/${counterPartyId}/removeSearchString?${params.toString()}`;

        const response = await fetch(url, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
        });

        if (!response.ok) {
            await showAlertFromResponse(response);
            return;
        }

        elementToRemove.parentElement.removeChild(elementToRemove);
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

        splitDataIntoPages(messages, SortType.COUNTERPARTY, filteredCounterPartyData);
    } catch (error) {
        console.error("Unexpected error in filterCounterParties:", error);
        showAlert("ERROR", messages["error_generic"]);
    }
}