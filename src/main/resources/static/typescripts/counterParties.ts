async function buildCounterParties() : Promise<void> {
    const messages = await fetchLocalization("counterParties");
    if (!messages) return;

    counterPartiesHiddenToggle = false;

    await loadCounterParties(messages);
    splitDataIntoPages(messages, "counterParties", counterParties);
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

        counterParties = await response.json();
        filteredCounterParties = counterParties;
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
            const name = createAndAppendElement(newRow, "td", "", "", {style: "width: 25%"});
            createAndAppendElement(name, "span", "tdMargin", counterParty.name);

            // Transaction count
            const transactionCount = createAndAppendElement(newRow, "td", "rightAligned", "", {style: "width: 10%"});
            createAndAppendElement(transactionCount, "span", "tdMargin", counterPartyDisplay.transactionCount.toString());

            // Contract count
            const contractCount = createAndAppendElement(newRow, "td", "rightAligned", "", {style: "width: 10%"});
            createAndAppendElement(contractCount, "span", "tdMargin", counterPartyDisplay.contractCount.toString());

            // Total amount
            const totalAmount = createAndAppendElement(newRow, "td", "rightAligned", "", {style: "width: 20%"});
            createAndAppendElement(totalAmount, "span", "tdMargin", formatNumber(counterPartyDisplay.totalAmount, currency),
                {style: "margin-right: 30px;"});

            // Description Cell
            const description = createAndAppendElement(newRow, "td", "rightAligned", "", {style: "width: 30%"});
            if (counterParty.description) {
                createAndAppendElement(description, "span", "tdMargin", counterParty.description);
            }

            // Secondary Row for Search Strings
            const searchStringRow = createAndAppendElement(rowGroup, "tr", "subRow", "");

            const counterpartySearchStrings = createAndAppendElement(searchStringRow, "td", "", "", {style: "width: 20%"});
            createAndAppendElement(counterpartySearchStrings, "h3", "", messages["counterpartySearchStrings"], {style: "margin-bottom: 15px"});

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

async function removeSearchStringFromCounterParty(counterPartyId: number, searchString: string, elementToRemove: HTMLElement, messages: Record<string, string>): Promise<void> {
    try {
        const params = new URLSearchParams();
        params.append("searchString", searchString);
        const url = `/counterParty/data/${counterPartyId}/removeSearchString?${params.toString()}`;

        const response = await fetch(url, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(searchString),
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