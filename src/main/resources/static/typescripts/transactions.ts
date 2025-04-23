async function buildTransactions(): Promise<void> {
    const messages = await loadLocalization("transactions");
    if (!messages) return;

    setMonths(messages);

    const type = Type.TRANSACTION;

    await loadData(type, messages);
    splitDataIntoPages(messages, type, transactionData);
    setUpSorting();

    document.getElementById("searchBarInput")?.addEventListener("input", () => searchTable(messages, type));

    document.getElementById("changeHiddenButton")?.addEventListener("click", () => showChangeHiddenDialog(type, messages));

    document.getElementById("changeContractButton")?.addEventListener("click", async () => {
        await buildChangeContract("/transactions", getCheckedData(type) as Transaction[]);
    });

    document.getElementById("showHiddenRows")?.addEventListener("change", () => changeRowVisibility(type));
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
            createCheckBoxForTable(newRow, null, transaction.id, transaction.hidden);
            animateElement(newRow);

            // Counterparty cell
            const counterparty = createAndAppendElement(newRow, "td");
            const counterpartyWrapper = createAndAppendElement(counterparty, "div", "justifyContentCenter");
            createAndAppendElement(counterpartyWrapper, "span", "bold", transaction.counterParty.name);

            // Contract cell
            const contract = createAndAppendElement(newRow, "td");
            if (transaction.contract?.name) {
                const contractWrapper = createAndAppendElement(contract, "div", "justifyContentCenter");
                createAndAppendElement(contractWrapper, "span", "highlightCell highlightCellPink", transaction.counterParty.name);
            }

            // Date cell
            const date = createAndAppendElement(newRow, "td");
            const dateWrapper = createAndAppendElement(date, "div", "justifyContentCenter");
            createAndAppendElement(dateWrapper, "span", "", transaction.counterParty.name);

            // Amount before cell
            const amountBefore = createAndAppendElement(newRow, "td");
            const amountBeforeWrapper = createAndAppendElement(amountBefore, "div", "justifyContentCenter");
            createAndAppendElement(amountBeforeWrapper, "span", "", formatNumber(transaction.amountInBankBefore, currency));

            // Amount cell with positive/negative styling
            const amount = createAndAppendElement(newRow, "td");
            const amountClass = transaction.amount >= 0 ? "positive" : "negative";
            const amountWrapper = createAndAppendElement(amount, "div", `${amountClass}`);
            createAndAppendElement(amountWrapper, "span", "", formatNumber(transaction.amount, currency));

            // Amount after cell
            const amountInBankAfter = createAndAppendElement(newRow, "td");
            const amountInBankAfterWrapper = createAndAppendElement(amountInBankAfter, "div", "justifyContentCenter");
            createAndAppendElement(amountInBankAfterWrapper, "span", "tdMargin", formatNumber(transaction.amountInBankAfter, currency));
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