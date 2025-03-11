async function buildTransactions(): Promise<void> {
    const messages = await fetchLocalization("transactions");
    if (!messages) return;

    monthAbbreviations = messages["monthAbbreviations"]
        .split("', '")
        .map((month: string) => month.replace(/'/g, ''));
    transactionsHiddenToggle = false;

    await loadTransactions(messages);
    splitDataIntoPages(messages, Type.TRANSACTION, transactionData);
    setUpSorting();

    document.getElementById("searchBarInput")?.addEventListener("input", () => searchTable(messages, Type.TRANSACTION));

    document.getElementById("changeHiddenButton")?.addEventListener("click", () => showChangeHiddenDialog(Type.TRANSACTION, messages));

    document.getElementById("changeContractButton")?.addEventListener("click", async () => {
        await buildChangeContract("/transactions", getCheckedData(Type.TRANSACTION) as Transaction[]);
    });

    document.getElementById("showHiddenRows")?.addEventListener("change", () => changeRowVisibility(Type.TRANSACTION));
}

async function loadTransactions(messages: Record<string, string>): Promise<void> {
    try {
        const response = await fetch(`/transactions/${bankAccountId}/data`, {
            method: 'GET',
            headers: {'Content-Type': 'application/json'}
        });

        if (!response.ok) {
            await showAlertFromResponse(response);
            return;
        }

        transactionData = await response.json();
        filteredTransactionData = transactionData;
    } catch (error) {
        console.error("There was an error loading the transactions:", error);
        showAlert('error', messages["error_generic"]);
    }
}

async function updateTransactionVisibility(
    messages: Record<string, string>,
    model: HTMLElement,
    updatedContainer: HTMLElement,
    moveToContainer: HTMLElement,
    hide: boolean
): Promise<void> {
    try {
        // Get all transaction IDs
        const transactionIds: number[] = Array.from(
            updatedContainer.querySelectorAll<HTMLElement>(".normalText")
        )
            .map(span => Number(span.id))
            .filter(id => !isNaN(id) && id !== 0); // Ensure valid IDs

        if (transactionIds.length === 0) {
            showAlert("INFO", messages["noTransactionsUpdated"], model);
            return;
        }

        const endpoint = hide ? "hideTransactions" : "unHideTransactions";
        const response = await fetch(`/transactions/${bankAccountId}/data/${endpoint}`, {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(transactionIds),
        });

        const responseBody: Response = await response.json();

        showAlert(responseBody.alertType, responseBody.message, model);

        if (responseBody.alertType === AlertType.SUCCESS) {
            // Animate and move elements
            moveElements(updatedContainer, moveToContainer);
            updateCachedDataAndUI(Type.TRANSACTION, messages, transactionIds);
        }
    } catch (error) {
        console.error("Unexpected error in updateTransactionVisibility:", error);
        showAlert("ERROR", messages["error_generic"], model);
    }
}

function addRowsToTransactionTable(data: Transaction[], messages: Record<string, string>): void {
    try {
        const tableBody = getCurrentTableBody();
        if (!tableBody) return;
        const currency = getCurrentCurrencySymbol();

        data.forEach((transaction, index) => {
            if (!transaction || typeof transaction !== "object") {
                console.warn(`Warning: Skipping invalid transaction at index ${index}.`, transaction);
                return;
            }

            let rowClass: string | null = transaction.hidden ? "hiddenRow" : null;
            if (rowClass && !transactionsHiddenToggle) {
                rowClass += " hidden";
            }

            const newRow = createAndAppendElement(tableBody, "tr", rowClass, null, {id: transaction.id.toString()});
            createCheckBoxForTable(newRow, transaction.id, transaction.hidden);
            animateElement(newRow);

            // Counterparty cell
            const counterparty = createAndAppendElement(newRow, "td", "", "", {style: "width: 25%"});
            createAndAppendElement(counterparty, "span", "tdMargin", transaction.counterParty.name, {
                style: "font-weight: bold;",
            });

            // Contract cell
            const contract = createAndAppendElement(newRow, "td", "", "", {style: "width: 15%"});
            if (transaction.contract?.name) {
                createAndAppendElement(contract, "span", "tdMargin highlightCell highlightCellPink",
                    transaction.contract.name);
            }

            // Category cell
            const category = createAndAppendElement(newRow, "td", "", "", {style: "width: 15%"});
            if (transaction.category?.name) {
                createAndAppendElement(category, "span", "tdMargin highlightCell highlightCellOrange", transaction.category.name);
            }

            // Date cell
            const date = createAndAppendElement(newRow, "td", "rightAligned", "", {style: "width: 10%"});
            createAndAppendElement(date, "span", "tdMargin", formatDateString(transaction.date));

            // Amount before cell
            const amountBefore = createAndAppendElement(newRow, "td", "rightAligned", "", {style: "width: 10%"});
            createAndAppendElement(amountBefore, "span", "tdMargin", formatNumber(transaction.amountInBankBefore, currency));

            // Amount cell with positive/negative styling
            const amount = createAndAppendElement(newRow, "td", "rightAligned", "", {style: "width: 10%"});
            const amountClass = transaction.amount >= 0 ? "positive" : "negative";
            createAndAppendElement(amount, "span", `tdMargin rightAligned ${amountClass}`,
                formatNumber(transaction.amount, currency)
            );

            // Amount after cell
            const amountInBankAfter = createAndAppendElement(newRow, "td", "rightAligned", "", {style: "width: 10%"});
            createAndAppendElement(amountInBankAfter, "span", "tdMargin",
                formatNumber(transaction.amountInBankAfter, currency), {style: "margin-right: 30px;"});
        });
    } catch (error) {
        console.error("Unexpected error in addRowsToTransactionTable:", error);
        showAlert("ERROR", messages["error_generic"]);
    }
}

function filterTransactions(messages: Record<string, string>, searchString: string): void {
    try {
        filteredTransactionData = transactionData.filter(transaction =>
            transaction.counterParty?.name?.toLowerCase().includes(searchString) ||
            transaction.contract?.name?.toLowerCase().includes(searchString) ||
            transaction.category?.name?.toLowerCase().includes(searchString) ||
            transaction.date?.toLowerCase().includes(searchString) ||
            transaction.amountInBankBefore?.toString().toLowerCase().includes(searchString) ||
            transaction.amount?.toString().toLowerCase().includes(searchString) ||
            transaction.amountInBankAfter?.toString().toLowerCase().includes(searchString)
        );

        splitDataIntoPages(messages, Type.TRANSACTION, filteredTransactionData);
    } catch (error) {
        console.error("Unexpected error in filterTransactions:", error);
        showAlert("ERROR", messages["error_generic"]);
    }
}


function transactionToListElementObjectArray(transactions: Transaction[]): ListElementObject[] {
    const listElementObjects: ListElementObject[] = [];

    const currency = getCurrentCurrencySymbol();

    transactions.forEach(transaction => {
        const listElementObject: ListElementObject = {
            id: transaction.id,
            text: transaction.counterParty.name,
            toolTip: formatNumber(transaction.amount, currency).toString()
        };

        listElementObjects.push(listElementObject);
    });

    return listElementObjects;
}