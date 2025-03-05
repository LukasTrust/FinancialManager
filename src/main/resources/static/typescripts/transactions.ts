async function buildTransactions(): Promise<void> {
    const messages = await fetchLocalization("transactions");
    if (!messages) return;

    monthAbbreviations = messages["monthAbbreviations"]
        .split("', '")
        .map((month: string) => month.replace(/'/g, ''));
    transactionsHiddenToggle = false;

    await loadTransactions(messages);
    splitDataIntoPages(messages, "transaction", transactionData);
    setUpSorting();

    document.getElementById("searchBarInput")?.addEventListener("input", () => searchTable(messages, "transaction"));

    document.getElementById("changeHiddenButton")?.addEventListener("click", () => showChangeHiddenDialog(messages));

    document.getElementById("changeContractButton")?.addEventListener("click", async () => {
        await buildChangeContract("/transactions", getCheckedTransactions());
    });

    document.getElementById("showHiddenRows")?.addEventListener("change", () => changeRowVisibility());
}

async function loadTransactions(messages: Record<string, string>): Promise<void> {
    try {
        const response = await fetch(`/transactions/${bankAccountId}/data`, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
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

            const newRow = createAndAppendElement(tableBody, "tr", rowClass, null, { id: transaction.id.toString() });
            createCheckBoxForTable(newRow, transaction.id, transaction.hidden);

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

function updateRowStyle(newRow: HTMLElement, checkBox: HTMLInputElement): void {
    newRow.classList.toggle("selectedRow", checkBox.checked);
}

function updateRowGroupStyle(rowGroup: HTMLElement, checkBox: HTMLInputElement) {
    if (checkBox.checked) {
        rowGroup.classList.add("selectedRow");
    } else {
        rowGroup.classList.remove("selectedRow");
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

        splitDataIntoPages(messages, "transaction", filteredTransactionData);
    } catch (error) {
        console.error("Unexpected error in filterTransactions:", error);
        showAlert("ERROR", messages["error_generic"]);
    }
}

function getCheckedTransactions(): Transaction[] {
    const checkedRows = new Set(getCheckedRows());
    return filteredTransactionData.filter(transaction => checkedRows.has(transaction.id));
}

function changeRowVisibility(): void {
    const currentTableBody = getCurrentTableBody();
    if (!currentTableBody) return;
    const rows = Array.from(currentTableBody.querySelectorAll("tr.hiddenRow"));
    transactionsHiddenToggle = !transactionsHiddenToggle;
    rows.forEach(row => row.classList.toggle("hidden"));
}

function showChangeHiddenDialog(messages: Record<string, string>): void {
    const { alreadyHidden, notHidden } = classifyHiddenTransactions();

    const dialogContent = createDialogContent(messages["changeHiddenHeader"], "bi bi-eye");
    const listContainer = createAndAppendElement(dialogContent, "div", "flexContainerSpaced");

    // Hidden transactions
    const leftSide = createListSection(listContainer, messages["alreadyHiddenHeader"], alreadyHidden);
    const rightSide = createListSection(listContainer, messages["notHiddenHeader"], notHidden);

    createDialogButton(leftSide, "bi bi-eye", messages["unHide"], "left", () =>
        updateTransactionVisibility(messages, dialogContent, leftSide, rightSide.querySelector(".listContainerColumn"), false)
    );

    // Not hidden transactions
    createDialogButton(rightSide, "bi bi-eye-slash", messages["hide"], "right", () =>
        updateTransactionVisibility(messages, dialogContent, rightSide, leftSide.querySelector(".listContainerColumn"), true)
    );
}

function classifyHiddenTransactions(): { alreadyHidden: Transaction[]; notHidden: Transaction[] } {
    const alreadyHidden: Transaction[] = [];
    const notHidden: Transaction[] = [];

    const transactions: Transaction[] = getCheckedTransactions();

    transactions.forEach(transaction => {
        transaction.hidden ? alreadyHidden.push(transaction) : notHidden.push(transaction);
    });

    return { alreadyHidden, notHidden };
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
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(transactionIds),
        });

        const responseBody: Response = await response.json();

        showAlert(responseBody.alertType, responseBody.message, model);

        if (responseBody.alertType === AlertType.SUCCESS) {
            // Animate and move elements
            moveElements(updatedContainer, moveToContainer);
            updateCachedTransactionsAndUI(messages, transactionIds);
        }
    } catch (error) {
        console.error("Unexpected error in updateTransactionVisibility:", error);
        showAlert("ERROR", messages["error_generic"], model);
    }
}

function updateCachedTransactionsAndUI(messages: Record<string, string>, transactionIds: number[]): void {
    filteredTransactionData.forEach((transaction: Transaction) => {
        if (transactionIds.includes(transaction.id)) {
            transaction.hidden = !transaction.hidden;
        }
    });

    splitDataIntoPages(messages, "transaction", filteredTransactionData);
}